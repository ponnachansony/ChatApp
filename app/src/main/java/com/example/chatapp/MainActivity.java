package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
   FirebaseUser firebaseUser;

    ActivityMainBinding binding;
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!=null){

            Intent receivedIntent = getIntent();
            Bundle bundle = receivedIntent.getExtras();

            String senderId = null;
            if(bundle != null && bundle.containsKey("sender_id")) {
                senderId = bundle.getString("sender_id");
                Log.d("Log: ", "onStart(senderid): " + senderId);
            }
            Log.d("Log: ", "onStart: " + senderId);

               //Notification in grouping -9/11/2023
            if(senderId != null) {

                SharedPreferences sharedPreferences = getSharedPreferences("notification_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(senderId, "");
                editor.commit();


                Intent intent=new Intent(this,Message_Activity.class);
                intent.putExtra("uuid", senderId);
                startActivity(intent);
                finish();
                return;
            }

            Intent intent=new Intent(this,Home_chat_List.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initSignUPButton();
        binding.loginPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password =  binding.loginPasswordField.getText().toString();
                if (password.length() >= 15 || password.length() < 6) {
                    binding.loginPasswordLayout.setError("Enter a password within 6-15 characters");
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
                            binding.loginPasswordLayout.setError(null);
                            return;
                        }
                    }
                    binding.loginPasswordLayout.setError("Password must contain at least one number and one special character");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        binding.loginEmailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!Patterns.EMAIL_ADDRESS.matcher( binding.loginEmailField.getText().toString()).matches()) {
                    binding.loginEmailLayout.setError("Please enter valid mail");
                } else if (Patterns.EMAIL_ADDRESS.matcher( binding.loginEmailField.getText().toString()).matches()) {
                    binding.loginEmailLayout.setError(null);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.loginBtnSubmit.setOnClickListener(view -> {
            String mail,password;
            mail=String.valueOf(binding.loginEmailField.getText());
            password=String.valueOf(binding.loginPasswordField.getText());

            if (TextUtils.isEmpty(mail)||TextUtils.isEmpty(password)){
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Login successfull", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,Home_chat_List.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Invalid password/mail,check your password and mail-id again and try", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

    }
    private void initSignUPButton() {
        SpannableString spannableString = new SpannableString(binding.loginSignupText.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(MainActivity.this, Registration.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        int startIndex = spannableString.toString().indexOf("Sign up");
        int endIndex = startIndex + "Sign up".length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.primary)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.loginSignupText.setText(spannableString);
        binding.loginSignupText.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private void onlinestatus() {
        if(firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(firebaseUser.getUid());
            Map<String, Object> data = new HashMap<>();
            data.put("is_online", true);
            databaseReference.updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.e("Log: ", "onComplete: ", task.getException());
//                        Toast.makeText(MyApp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}