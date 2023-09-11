package com.example.chatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(firebaseUser.getUid());
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcm_token", token);
            databaseReference.updateChildren(tokenData);
        }
    }


    //foreground
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Log: ", "onMessageReceived: Received Successfully");
        //Notification in grouping -9/11/2023
        // RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            Log.d("Log: ", "onMessageReceived: " + remoteMessage.getData());
            String senderId = remoteMessage.getData().get("sender_id");
            showNotification(title, body, senderId);
        }
    }

    // Method to display the notifications

    public void showNotification(String title, String message, String senderId) {
        // Create a new unique notification ID for each new notification
        int notificationId = generateUniqueNotificationId(senderId);

        // Create an intent to open the MainActivity when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("sender_id", senderId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create a notification channel for Android Oreo and above
        String channel_id = "chat_messages";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "Chat Messages", importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Get the previous message from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("notification_data", MODE_PRIVATE);
        String previousMessage = sharedPreferences.getString(senderId, "");
        String updatedMessage = message;
        if (!previousMessage.isEmpty()) {
            updatedMessage = previousMessage + "\n" + message;
        }

        // Store the updated message in SharedPreferences for future use
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(senderId, updatedMessage);
        editor.apply();

        // Build the new notification
       /* NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(updatedMessage)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
//                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(message).setBigContentTitle(title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(updatedMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);*/
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(updatedMessage).setGroup(senderId).setStyle(new NotificationCompat.BigTextStyle().bigText(updatedMessage))
//                .setGroupSummary(true)
//                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_HIGH)

                .setContentIntent(pendingIntent);

       /* NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(updatedMessage)  // Set your updated message here
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(updatedMessage))
                .setContentIntent(pendingIntent);*/

        // Notify the updated notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, builder.build());

        // Clear the previous notification (if it exists) with the old ID
        int previousNotificationId = getPreviousNotificationId(senderId);
        if (previousNotificationId != -1) {
            notificationManager.cancel(previousNotificationId);
        }

        // Store the new notification ID for the sender
        storeNotificationId(senderId, notificationId);
    }

    // Generate a unique notification ID for each sender
    private int generateUniqueNotificationId(String senderId) {
        return senderId.hashCode() + (int) System.currentTimeMillis();
    }

    // Retrieve the previous notification ID for the sender
    private int getPreviousNotificationId(String senderId) {
        SharedPreferences sharedPreferences = getSharedPreferences("notification_data", MODE_PRIVATE);
        return sharedPreferences.getInt(senderId + "_nid", -1);
    }

    // Store the new notification ID for the sender
    private void storeNotificationId(String senderId, int notificationId) {
        SharedPreferences sharedPreferences = getSharedPreferences("notification_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(senderId + "_nid", notificationId);
        editor.apply();
    }


}

