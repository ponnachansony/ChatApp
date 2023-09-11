package com.example.chatapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Adapters.UserAdapter;
import com.example.chatapp.Model.FirebaseUser;
import com.example.chatapp.databinding.ActivityGroupBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Group_Activity extends AppCompatActivity {
    private RecyclerView recyclerView;

    ActivityGroupBinding binding;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        recyclerView = findViewById(R.id.group_recyc_user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this);
        recyclerView.setAdapter(userAdapter);

        validation();

        Button createGroupButton = findViewById(R.id.create_group_button);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        fetchUsersFromFirebase();


    }

    private void validation() {
        binding.groupGrpNameField.setFilters(new InputFilter[]{new AlphanumericInputFilter4()});
        binding.grpChatToolbar.setNavigationOnClickListener(v-> onBackPressed());

        binding.groupGrpNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.groupGrpNameField.length() >= 15|| binding.groupGrpNameField.length()<1) {
                    binding.groupGrpNameLayout.setError("enter the name within 1-20 ");
                } else if (binding.groupGrpNameField.length() <= 14) {
                    binding.groupGrpNameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


    }

    private void createGroup() {
        // Handle group creation logic here
        // You can access the selected users from the UserAdapter if needed
    }

    private void fetchUsersFromFirebase() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("logged_in_user_cred");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<FirebaseUser> userList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FirebaseUser user = snapshot.getValue(FirebaseUser.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // Update the user list in the RecyclerView
                userAdapter.setItems(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Group_Activity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
class AlphanumericInputFilter4 implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // Define the allowed characters (alphanumeric and space)
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";

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


    class AlphanumericInputFilter1 implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // Define the allowed characters (alphanumeric and space)
            String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";

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
}