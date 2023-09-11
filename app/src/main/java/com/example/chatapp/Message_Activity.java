package com.example.chatapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Adapters.MessageAdapter;
import com.example.chatapp.Model.MessageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class Message_Activity extends AppCompatActivity {


    public static final String TAG = "Log: " + Message_Activity.class.getSimpleName();
    private static final int FILE_PICKER_REQUEST_CODE = 123;
    TextView chat_username, chat_online;
    com.example.chatapp.Model.FirebaseUser receiverFirebaseUser;
    EditText chat_typing;
    ImageButton sendbtn;
    FirebaseUser fuser;
    DatabaseReference reference, referenceSender, referenceReceiver;
    String senderRoom;
    String receiverRoom;
    String senderUUID, receiverUUID;
    RecyclerView recycler;
    MessageAdapter messageAdapter;
    ImageButton docTagBtn;
    private DatabaseReference receiverDatabaseReference;

    public static String extractFirstPart(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex != -1) {
            String firstPart = email.substring(0, atIndex);
            return firstPart.replaceAll("[^a-zA-Z0-9]", "-");
        }
        return email;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Initialize Firebase


        senderUUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUUID = getIntent().getStringExtra("uuid");

        receiverDatabaseReference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(receiverUUID);
        receiverDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receiverFirebaseUser = com.example.chatapp.Model.FirebaseUser.fromDatabase(snapshot);
                setContentView(R.layout.activity_message);

                doRemainingStuffs();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void doRemainingStuffs() {
        docTagBtn = findViewById(R.id.doc_tag_btn);
        recycler = findViewById(R.id.chat_recycler);


        senderRoom = senderUUID + "-room-" + receiverUUID;
        receiverRoom = receiverUUID + "-room-" + senderUUID;

        messageAdapter = new MessageAdapter(this);
        recycler.setAdapter(messageAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        referenceSender = FirebaseDatabase.getInstance().getReference("messages").child(senderRoom);
        referenceReceiver = FirebaseDatabase.getInstance().getReference("messages").child(receiverRoom);


        referenceSender.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageAdapter.clear(); // Clear existing messages before adding new ones
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    Log.d(TAG, "Sender Message: " + messageModel.getMessage());
                    messageAdapter.add(messageModel);
                    recycler.scrollToPosition(messageAdapter.getLastItemPosition());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Message_Activity", "Sender data retrieval cancelled: " + error.getMessage());
            }
        });

        referenceReceiver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageAdapter.clear(); // Clear existing messages before adding new ones
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    Log.d(TAG, "Receiver Message: " + messageModel.getMessage());
                    messageAdapter.add(messageModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Message_Activity", "Receiver data retrieval cancelled: " + error.getMessage());
            }
        });

        docTagBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // this is for all file types
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
            String fileContentType = "image/jpeg"; // Replace with the actual content type of the file
            String fileContentUrl = "https://example.com/audio.mp3"; // Replace with the actual content URL of the file
//                sendFileMessage(fileContentType, fileContentUrl);

        });


        sendbtn = findViewById(R.id.send_btn);
        sendbtn.setOnClickListener(view -> {
            String message = chat_typing.getText().toString();
            if (message.trim().length() > 0) {
                sendMessage(message);
                chat_typing.setText("");
            }
        });

        chat_typing = findViewById(R.id.chat_typing);

        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.baseline_more_vert_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24));


        chat_username = findViewById(R.id.username_item_name);
        chat_online = findViewById(R.id.msg_online);
        String userName = receiverFirebaseUser.getUsername();
        TextView userNameView = findViewById(R.id.home_toolbarUsername);
        userNameView.setText(userName);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(receiverUUID).child("is_online");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isOnline = snapshot.getValue(Boolean.class);
                userNameView.setText(userName);
                if (isOnline != null) {
                    chat_online.setText(isOnline ? "Online" : "Offline");
                    messageAdapter.setReceiverOnline(isOnline);

//                        Intent intent = new Intent(Message_Activity.this,Home_chat_List.class);
//                        intent.putExtra("isOnlineStatus", isOnline);
//                        startActivity(intent);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public void sendFCMNotification(final String userToken, final String message) {
        // Use AsyncTask for network operations

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                try {
                    // Replace with your own Firebase Cloud Messaging Server Key
                    String serverKey = "AAAAPJrLIvA:APA91bE-GE0o4bgaGwJgpwWjYN2YcsUENMNh1lyLh7Z6SpmxG-ZrEvUcWR5OtAApXS3Dp8VYGcPpGw29C5hjMZ1XN3qaFZtIFO6Gv-oNf_Cfu8hP_N17e0Kmgm5JGKVsKv9UhIUwp-Y8";

                    // Create the JSON payload for the FCM notification
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("to", userToken);

                     //Notification in grouping -9/11/2023

                    //JSONObject notification = new JSONObject();
////                    notification.put("title", fuser.getEmail());
//                    Log.d(TAG, "Sender's Name: " + "getDisplayName");
//                    notification.put("title", fuser.getDisplayName());
//                    notification.put("body", message);
                    JSONObject notificationData = new JSONObject();
                    notificationData.put("title", fuser.getDisplayName());
                    notificationData.put("body", message);
                    notificationData.put("sender_id", senderUUID);

                    jsonBody.put("data", notificationData);
//                    jsonBody.put("notification", notification);

                    // Create URL and HttpURLConnection
                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set the request method to POST
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    // Set headers
                    connection.setRequestProperty("Authorization", "key=" + serverKey);
                    connection.setRequestProperty("Content-Type", "application/json");

                    // Send the JSON payload
                    try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                        wr.writeBytes(jsonBody.toString());
                        wr.flush();
                    }

                    // Get the response
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Handle the response result (e.g., check if the notification was sent successfully or if there were errors)
                    } else {
                        // Handle the HTTP error response
                    }

                    connection.disconnect();

                    return responseCode;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "sendFCMNotification: ", e);
                    return -1; // Return an error code
                }
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                super.onPostExecute(responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Notification sent successfully
                    Toast.makeText(Message_Activity.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the error
                    Toast.makeText(Message_Activity.this, "Failed to send notification (ResponseCode: " + responseCode + ")", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

    }

    private void sendMessage(String message) {
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        MessageModel messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), message, timestamp);
        referenceSender.child(messageId).setValue(messageModel);
        referenceReceiver.child(messageId).setValue(messageModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Write code to send notification
                DatabaseReference receiverUserRef = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(receiverUUID);
                receiverUserRef.child("fcm_token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String fcmToken = dataSnapshot.getValue(String.class);
//                            Toast.makeText(Message_Activity.this, fcmToken, Toast.LENGTH_SHORT).show();
                            //Send Notification
//                            RemoteMessage remoteMessage = new RemoteMessage.Builder(fcmToken)
//                                    .setMessageId(receiverUUID + "-" + senderUUID)
//                                    .addData("title", "Message from " + fuser.getEmail())
//                                    .addData("body", message)
//                                    .build();
//                            FirebaseMessaging.getInstance().send(remoteMessage);
                            sendFCMNotification(fcmToken, message);

                        } else {
                            Toast.makeText(Message_Activity.this, "Not able to send message", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Message_Activity.this, "Unable to get the token", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.chat_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        // Set query listener
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                messageAdapter.getFilter().filter(newText); // Use messageAdapter here
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedFileUri = data.getData();
                String contentType = getContentResolver().getType(selectedFileUri);
                if (contentType != null) {
                    String fileName = getFileName(selectedFileUri);
                    uploadFileToFirebase(selectedFileUri, contentType, fileName);
                }
            }
        }
    }

    private void uploadFileToFirebase(Uri fileUri, String contentType, String fileName) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference fileReference = storageReference.child("files/" + fileName);

        UploadTask uploadTask = fileReference.putFile(fileUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL of the uploaded file
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Send a file message with the download URL
                sendFileMessage("", contentType, uri.toString(), fileName);
            });
        }).addOnFailureListener(exception -> {
            // Handle failure
            Toast.makeText(this, "File upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    //  Method to send a file message to Firebase Realtime Database
    private void sendFileMessage(String content, String mediaType, String mediaUrl, String mediaName) {
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        MessageModel messageModel = new MessageModel(messageId, FirebaseAuth.getInstance().getUid(), content, timestamp, mediaType, mediaUrl, mediaName);
        referenceSender.child(messageId).setValue(messageModel);
        referenceReceiver.child(messageId).setValue(messageModel);
    }


    // Method to get the file name from the file URI
    private String getFileName(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String fileName = cursor.getString(nameIndex);
            cursor.close();
            return fileName;
        }
        return "unknown_file";
    }


}