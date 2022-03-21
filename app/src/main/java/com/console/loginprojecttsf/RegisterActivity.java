package com.console.loginprojecttsf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextView alreadyHaveAccount;
    private EditText inputEmail, inputPassword, inputConfirmPassword;
    private Button btnRegister;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private boolean isRegInProcess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performAuth();
            }
        });
    }

    private void init() {
        inputEmail = findViewById(R.id.register_id_email);
        inputPassword = findViewById(R.id.register_id_password);
        inputConfirmPassword = findViewById(R.id.register_id_confirm_password);
        btnRegister = findViewById(R.id.register_id_register);
        alreadyHaveAccount = findViewById(R.id.register_id_login);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    private void performAuth() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();
        if (!email.matches(emailPattern)) {
            inputEmail.setError("Invalid email.");
            inputEmail.requestFocus();
        } else if (password.isEmpty()) {
            inputPassword.setError("Invalid password.");
            inputPassword.requestFocus();
        } else if (password.length() < 6) {
            inputPassword.setError("Password length cannot be less than 6.");
            inputPassword.requestFocus();
        } else if (!password.equals(confirmPassword)) {
            inputConfirmPassword.setError("Passwords didn't match");
            inputConfirmPassword.requestFocus();
        } else {
            isRegInProcess = true;
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        showProgress(false);
                        sendUser();
                    } else {
                        isRegInProcess = false;
                        showProgress(false);
                        Toast.makeText(RegisterActivity.this, task.getException().getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUser() {
        isRegInProcess = false;
        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
        intent.putExtra("token", "firebase");
        intent.putExtra("email", FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showProgress(boolean value) {
        if (value) {
            progressDialog.setMessage("Registering");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isRegInProcess) {
            super.onBackPressed();
        }
    }
}