package com.console.loginprojecttsf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.console.loginproject_tsf.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private TextView newAccount;
    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private ImageView btnGoogle, btnFacebook;
    CallbackManager callbackManager;
    public static GoogleSignInClient mGoogleSignInClient;
    String id = "291345232836-v3dogdfvqdh5k51uc64kgr2p2pmku5gn.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();

        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Configure Google Sign In
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(id)
                        .requestEmail()
                        .build();

                mAuth = FirebaseAuth.getInstance();
                showProgress(true);

                mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                callbackManager = CallbackManager.Factory.create();
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                if (isLoggedIn) {
                    LoginManager.getInstance().logOut();
                }
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                showProgress(false);
                                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                intent.putExtra("token", "facebook");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancel() {
                            }

                            @Override
                            public void onError(FacebookException exception) {
                            }
                        });
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
            }
        });
    }

    private void init() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        inputEmail = findViewById(R.id.login_id_email);
        inputPassword = findViewById(R.id.login_id_password);
        btnLogin = findViewById(R.id.login_id_login);
        newAccount = findViewById(R.id.login_id_new_account);
        btnGoogle = findViewById(R.id.btnGoogle);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        btnFacebook = findViewById(R.id.btnFacebook);
    }

    private void performLogin() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if (!email.matches(emailPattern)) {
            inputEmail.setError("Invalid email.");
            inputEmail.requestFocus();
        } else if (password.isEmpty()) {
            inputPassword.setError("Invalid password.");
            inputPassword.requestFocus();
        } else if (password.length() < 6) {
            inputPassword.setError("Password length cannot be less than 6.");
            inputPassword.requestFocus();
        } else {
            showProgress(true);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        showProgress(false);
                        sendUser();
                    } else {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, task.getException().getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUser() {
        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        intent.putExtra("token", "firebase");
        intent.putExtra("email", FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            super.onActivityResult(requestCode, resultCode, data);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                showProgress(false);
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            showProgress(false);
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            showProgress(false);
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        String photoUrl = firebaseUser.getPhotoUrl().toString();
        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("token", "google");
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("photoUrl", photoUrl);
        startActivity(intent);
    }

    private void showProgress(boolean value) {
        if (value) {
            progressDialog.setMessage("Loading");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }
}