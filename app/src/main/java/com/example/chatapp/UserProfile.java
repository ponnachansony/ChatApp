package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.chatapp.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    ImageView prof_image;
    EditText prof_name, prof_mail, prof_mobile;
    TextView add_img;
    DatabaseReference reference;
    FirebaseUser fUser;
    Button update_prof;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.profNameField.setFilters(new InputFilter[]{new AlphanumericInputFilter2()});


        validation();

        initeditnamemobile();


    }

    private void initeditnamemobile() {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(fUser.getUid());
        binding.profToolbar.setNavigationOnClickListener(v -> onBackPressed());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                com.example.chatapp.Model.FirebaseUser user = snapshot.getValue(com.example.chatapp.Model.FirebaseUser.class);
                binding.profNameField.setText(user.getUsername());
                binding.profMailField.setText(user.getMail());
                binding.profPhoneField.setText(user.getId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        binding.profBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = binding.profNameField.getText().toString();
                String newMail = binding.profMailField.getText().toString();
                String newMobile = binding.profPhoneField.getText().toString();

                updateProfileData(newName, newMail, newMobile);
            }
        });

//        add_img.setOnClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            startActivityForResult(intent, 1);
//

    }

    private void validation() {
        setSupportActionBar(binding.profToolbar);
        binding.profToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24));

        binding.profNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.profNameField.length() >= 15 || binding.profNameField.length() < 1) {
                    binding.profNameLayout.setError("enter the name within 1-15 ");
                } else if (binding.profNameField.length() <= 14) {
                    binding.profNameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        binding.profPhoneField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.profPhoneField.length() > 10 || binding.profPhoneField.length() < 10) {
                    binding.profPhoneLayout.setError("enter the valid mobile no. ");
                } else if (binding.profPhoneField.length() == 10) {
                    binding.profPhoneLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void updateProfileData(String newName, String newMail, String newMobile) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("username", newName);
        updates.put("mail", newMail);
        updates.put("id", newMobile);

        reference.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            prof_image.setImageURI(imageUri); // Display the selected image in ImageView

            // Now you can upload the image to Firebase Storage and update the URL in the database
            // Refer to the next step for uploading the image to Firebase Storage.
            // Inside the onActivityResult() method
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + fUser.getUid());

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Get the download URL of the uploaded image
                            String imageUrl = uri.toString();
                            updateProfileImage(imageUrl); // Update the image URL in the database
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle the image upload failure
                        Toast.makeText(this, "image upload failed", Toast.LENGTH_SHORT).show();
                    });

        }
    }

    private void updateProfileImage(String imageUrl) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("profileImage", imageUrl); // Update the image URL field in your FirebaseUser model

        reference.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserProfile.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfile.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                });
    }



}
class AlphanumericInputFilter2 implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // Define the allowed characters (alphanumeric and space)
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ";

        // Create a StringBuilder to hold the filtered text
        StringBuilder filteredText = new StringBuilder();

        // Loop through each character in the source text
        for (int i = start; i < end; i++) {
            char currentChar = source.charAt(i);

            // Check if the character is in the list of allowed characters
            if (allowedChars.indexOf(currentChar) != -1) {
                filteredText.append(currentChar);
            }
        }

        // If the filtered text is different from the source, return the filtered text
        if (!TextUtils.equals(source, filteredText.toString())) {
            return filteredText.toString();
        }

        // If no changes were made, return null to indicate that no filtering is necessary
        return null;
    }
}