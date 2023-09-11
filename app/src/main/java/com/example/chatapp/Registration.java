//package com.example.chatapp;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.InputFilter;
//import android.text.InputType;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.Spanned;
//import android.text.TextPaint;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.text.method.LinkMovementMethod;
//import android.text.style.ClickableSpan;
//import android.text.style.ForegroundColorSpan;
//import android.text.style.StyleSpan;
//import android.text.style.UnderlineSpan;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.chatapp.databinding.ActivityRegistrationBinding;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
//import com.google.firebase.auth.FirebaseAuthUserCollisionException;
//import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserProfileChangeRequest;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class Registration extends AppCompatActivity {
//
//    ActivityRegistrationBinding binding;
//
//    EditText sign_mail,sign_password,sign_re_username,sign_id;
//    TextView goto_login;
//
//
//    DatabaseReference databaseReference;
//    Button signup_btn;
//
//
//
//    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
//
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding=ActivityRegistrationBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//
//        binding.signupNameField.setFilters(new InputFilter[]{new AlphanumericInputFilter()});
//
//
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        textchangeError();
//        initSignUp();
//
//        binding.signupBtnSubmit.setOnClickListener(view -> {
//            String username,mail,password,id;
//            username=String.valueOf(binding.signupNameField.getText());
//            mail=String.valueOf(binding.signupEmailField.getText());
//            password=String.valueOf(binding.signupPasswordField.getText());
//            id=String.valueOf(binding.signupPhoneField.getText());
//
//            if (TextUtils.isEmpty(mail)||TextUtils.isEmpty(password)){
//                Toast.makeText(this, "please fill both fields", Toast.LENGTH_SHORT).show();
//                return;
//            } if (binding.signupPasswordField.getText().toString().equals(binding.signupNameField.getText().toString())) {
//                Toast.makeText(this, "password do not match", Toast.LENGTH_SHORT).show();
//                return;
//            }else
//                firebaseAuth.createUserWithEmailAndPassword(mail, password)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    FirebaseUser user = task.getResult().getUser();
//                                    if (user != null) {
//                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                                                .setDisplayName(username)
//                                                .build();
//                                        user.updateProfile(profileUpdates)
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        if (task.isSuccessful()) {
//                                                            // Check if the ID is already in use in the database
//                                                            checkIfIdExistsInDatabase(id, new IdCheckCallback() {
//                                                                @Override
//                                                                public void onIdCheckComplete(boolean idExists) {
//                                                                    if (idExists) {
//                                                                        // ID is already in use
//                                                                        Toast.makeText(Registration.this, "ID is already in use. Enter a new ID.", Toast.LENGTH_SHORT).show();
//                                                                        // You can also handle this case by clearing the ID field or taking other actions as needed.
//                                                                    } else {
//                                                                        // ID is not in use, save user data to the database
//                                                                        saveUserDataToDatabase(username, mail, password, id);
//                                                                        Toast.makeText(Registration.this, "Registration Successfully", Toast.LENGTH_SHORT).show();
//                                                                        finish();
//                                                                    }
//                                                                }
//                                                            });
//                                                        } else {
//                                                            Toast.makeText(Registration.this, "Failed to update user profile", Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    }
//                                                });
//                                    }
//
//                                } else {
//                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
//                                        // Email is already in use
//                                        Toast.makeText(Registration.this, "Email is already in use. Enter a new email.", Toast.LENGTH_SHORT).show();
//                                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                        // Invalid email or password format
//                                        Toast.makeText(Registration.this, "Invalid email or password format. Please check and try again.", Toast.LENGTH_SHORT).show();
//                                    } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
//                                        // Weak password
//                                        Toast.makeText(Registration.this, "Weak password. Please choose a stronger password.", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(Registration.this, "Registration/Authentication failed", Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(Registration.this, "Try again with valid credentials", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            }
//                        });
//
//
//
//
//        });
//    }
//    private void textchangeError() {
//
//        binding.signupNameField.setFilters(new InputFilter[]{new AlphanumericInputFilter()});
//
//        binding.signupNameField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (binding.signupNameField.length() >= 15 || binding.signupNameField.length() < 1) {
//                    binding.signupNameLayout.setError("enter the name within 1-15 ");
//                } else if (binding.signupNameField.length() <= 14) {
//                    binding.signupNameLayout.setError(null);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//            }
//        });
//
//        binding.signupPhoneField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (binding.signupPhoneField.length() > 10 || binding.signupPhoneField.length() < 10) {
//                    binding.signupPhoneLayout.setError("enter the valid mobile no. ");
//                } else if (binding.signupPhoneField.length() == 10) {
//                    binding.signupPhoneLayout.setError(null);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//            }
//        });
//
//        binding.signupEmailField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (!Patterns.EMAIL_ADDRESS.matcher(binding.signupEmailField.getText().toString()).matches()) {
//                    binding.signupEmailLayout.setError("Please enter valid email");
//                } else if (Patterns.EMAIL_ADDRESS.matcher(binding.signupEmailField.getText().toString()).matches()) {
//                    binding.signupEmailLayout.setError(null);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//            }
//        });
//
//        binding.signupPasswordField.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                String password = binding.signupPasswordField.getText().toString();
//                if (password.length() >= 15 || password.length() < 6) {
//                    binding.signupPasswordLayout.setError("Enter a password within 6-15 characters");
//                } else if (password.length() <= 14) {
//                    boolean hasNumber = false;
//                    boolean hasSpecialChar = false;
//                    for (int j = 0; j < password.length(); j++) {
//                        char c = password.charAt(j);
//                        if (Character.isDigit(c)) {
//                            hasNumber = true;
//                        } else if (!Character.isLetterOrDigit(c)) {
//                            hasSpecialChar = true;
//                        }
//                        if (hasNumber && hasSpecialChar) {
//                            binding.signupPasswordLayout.setError(null);
//                            return;
//                        }
//                    }
//                    binding.signupPasswordLayout.setError("Password must contain at least one number and one special character");
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//            }
//        });
//
//    }
//    private void initSignUp() {
//        SpannableString spannableString = new SpannableString(binding.signupLoginText.getText());
//        ClickableSpan clickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                Intent intent = new Intent(Registration.this, MainActivity.class);
//                startActivity(intent);
//            }
//
//            @Override
//            public void updateDrawState(@NonNull TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setUnderlineText(false);
//            }
//        };
//
//        int startIndex = spannableString.toString().indexOf("login");
//        int endIndex = startIndex + "login".length();
//        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        binding.signupLoginText.setText(spannableString);
//        binding.signupLoginText.setMovementMethod(LinkMovementMethod.getInstance());
//    }
//
//
//    private void saveUserDataToDatabase(String username,String mail,String password,String id){
//        String Id=firebaseAuth.getCurrentUser().getUid();
//        User user=new User(username,mail,password,id);
//        user.setUuid(Id);
//        databaseReference.child("logged_in_user_cred").child(Id).setValue(user);
//    }
//    public static String date() {
//        Calendar calendar = Calendar.getInstance();
//        Date currentDate = calendar.getTime();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        return dateFormat.format(currentDate);
//    }
//}
//class User{
//    public String username;
//    public  String mail;
//    public  String password;
//    public  String id;
//
//    public String getUuid() {
//        return uuid;
//    }
//
//    public void setUuid(String uuid) {
//        this.uuid = uuid;
//    }
//
//    public String uuid;
//
//
//    public User() {
//    }
//
//    public User(String username,String mail,String password,String id) {
//        this.username=username;
//        this.mail = mail;
//        this.password=password;
//        this.id=id;
//
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getMail() {
//        return mail;
//    }
//
//    public void setMail(String mail) {
//        this.mail = mail;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//}
//
//
//
//
//class AlphanumericInputFilter implements InputFilter {
//
//    @Override
//    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//        // Define the allowed characters (alphanumeric and space)
//        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
//
//        // Create a StringBuilder to hold the filtered text
//        StringBuilder filteredText = new StringBuilder();
//
//        // Loop through each character in the source text
//        for (int i = start; i < end; i++) {
//            char currentChar = source.charAt(i);
//
//            // Check if the character is in the list of allowed characters
//            if (allowedChars.indexOf(currentChar) != -1) {
//                filteredText.append(currentChar);
//            }
//        }
//
//        // If the filtered text is different from the source, return the filtered text
//        if (!TextUtils.equals(source, filteredText.toString())) {
//            return filteredText.toString();
//        }
//
//        // If no changes were made, return null to indicate that no filtering is necessary
//        return null;
//    }
//}
package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivityRegistrationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    ActivityRegistrationBinding binding;

    DatabaseReference databaseReference;

    FirebaseUser firebaseUser;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.signupNameField.setFilters(new InputFilter[]{new AlphanumericInputFilter()});

        databaseReference = FirebaseDatabase.getInstance().getReference();
        textchangeError();
        initSignUp();
        signButtonFirebse();

    }


    private void signButtonFirebse() {
        binding.signupBtnSubmit.setOnClickListener(view -> {
            String username, mail, password, id;
            username = String.valueOf(binding.signupNameField.getText());
            mail = String.valueOf(binding.signupEmailField.getText());
            password = String.valueOf(binding.signupPasswordField.getText());
            id = String.valueOf(binding.signupPhoneField.getText());

            // Check if the device is connected to the internet
            if (!isNetworkConnected()) {
                Toast.makeText(this, "You are offline. Please try again with an internet connection.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (binding.signupPasswordField.getText().toString().equals(binding.signupNameField.getText().toString())) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the ID is already in use in the database
            checkIfIdExistsInDatabase(id, new IdCheckCallback() {
                @Override
                public void onIdCheckComplete(boolean idExists) {
                    if (idExists) {
                        // ID is already in use
                        Toast.makeText(Registration.this, "Mobile number is already in use. Enter a new Number.", Toast.LENGTH_SHORT).show();
                    } else {
                        // ID is not in use, proceed with user registration
                        firebaseAuth.createUserWithEmailAndPassword(mail, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = task.getResult().getUser();
                                            if (user != null) {
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(username)
                                                        .build();
                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    saveUserDataToDatabase(username, mail, password, id);
                                                                    Toast.makeText(Registration.this, "Registration Successfully", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Registration.this, "Failed to update user profile", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                // Email is already in use
                                                Toast.makeText(Registration.this, "Email is already in use. Enter a new email.", Toast.LENGTH_SHORT).show();
                                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                                // Invalid email or password format
                                                Toast.makeText(Registration.this, "Invalid email or password format. Please check and try again.", Toast.LENGTH_SHORT).show();
                                            } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                                // Weak password
                                                Toast.makeText(Registration.this, "Weak password. Please choose a stronger password.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Registration.this, "Registration/Authentication failed", Toast.LENGTH_SHORT).show();
                                                Toast.makeText(Registration.this, "Try again with valid credentials", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                    }
                }
            });
        });
    }

    // this is a method to check network connectivity
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void textchangeError() {
        binding.signupNameField.setFilters(new InputFilter[]{new AlphanumericInputFilter()});

        binding.signupNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.signupNameField.length() >= 15 || binding.signupNameField.length() < 1) {
                    binding.signupNameLayout.setError("Enter the name within 1-15 characters");
                } else if (binding.signupNameField.length() <= 14) {
                    binding.signupNameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.signupPhoneField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.signupPhoneField.length() > 10 || binding.signupPhoneField.length() < 10) {
                    binding.signupPhoneLayout.setError("Enter a valid mobile number");
                } else if (binding.signupPhoneField.length() == 10) {
                    binding.signupPhoneLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.signupEmailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.signupEmailField.getText().toString()).matches()) {
                    binding.signupEmailLayout.setError("Please enter a valid email");
                } else if (Patterns.EMAIL_ADDRESS.matcher(binding.signupEmailField.getText().toString()).matches()) {
                    binding.signupEmailLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.signupPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = binding.signupPasswordField.getText().toString();
                if (password.length() >= 15 || password.length() < 6) {
                    binding.signupPasswordLayout.setError("Enter a password within 6-15 characters");
                } else if (password.length() <= 14) {
                    boolean hasNumber = false;
                    boolean hasSpecialChar = false;
                    for (int j = 0; j < password.length(); j++) {
                        char c = password.charAt(j);
                        if (Character.isDigit(c)) {
                            hasNumber = true;
                        } else if (!Character.isLetterOrDigit(c)) {
                            hasSpecialChar = true;
                        }
                        if (hasNumber && hasSpecialChar) {
                            binding.signupPasswordLayout.setError(null);
                            return;
                        }
                    }
                    binding.signupPasswordLayout.setError("Password must contain at least one number and one special character");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void initSignUp() {
        SpannableString spannableString = new SpannableString(binding.signupLoginText.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Registration.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        int startIndex = spannableString.toString().indexOf("Login");
        int endIndex = startIndex + "login".length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.signupLoginText.setText(spannableString);
        binding.signupLoginText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void saveUserDataToDatabase(String username, String mail, String password, String id) {
        String Id = firebaseAuth.getCurrentUser().getUid();
        User user = new User(username, mail, password, id);
        user.setUuid(Id);
        databaseReference.child("logged_in_user_cred").child(Id).setValue(user);
    }

    private void checkIfIdExistsInDatabase(String id, final IdCheckCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("logged_in_user_cred");

        usersRef.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean idExists = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // If there's a match, the ID exists in the database
                    idExists = true;
                    break;
                }
                callback.onIdCheckComplete(idExists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error if the query is canceled
                callback.onIdCheckComplete(false); // Assume the ID doesn't exist in case of an error
            }
        });
    }

    interface IdCheckCallback {
        void onIdCheckComplete(boolean idExists);
    }
}

class User {
    public String username;
    public String mail;
    public String password;
    public String id;
    public String uuid;

    public User() {
    }

    public User(String username, String mail, String password, String id) {
        this.username = username;
        this.mail = mail;
        this.password = password;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

class AlphanumericInputFilter implements InputFilter {

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
