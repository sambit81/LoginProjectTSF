package com.console.loginprojecttsf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView dp;
    private TextView name, email;
    private ImageView menu;
    String token, profile_pic, profile_name, profile_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        if(token.equals("google")) {
            profile_pic = intent.getStringExtra("photoUrl");
            profile_name = intent.getStringExtra("name");
            profile_email = intent.getStringExtra("email");
            Picasso.get()
                    .load(profile_pic)
                    .placeholder(R.drawable.user)
                    .into(dp);
            name.setText(profile_name);
            email.setText(profile_email);

        } else if(token.equals("firebase")) {
            profile_email = intent.getStringExtra("email");
            dp.setImageDrawable(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.user));
            email.setText(profile_email);
        } else {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            try {
                                profile_name = object.getString("name");
                                profile_pic = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                name.setText(profile_name);
                                Picasso.get()
                                        .load(profile_pic)
                                        .placeholder(R.drawable.user)
                                        .into(dp);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
        }

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(menu);
            }
        });
    }

    private void init() {
        dp = findViewById(R.id.profile_id_dp);
        name = findViewById(R.id.profile_id_name);
        email = findViewById(R.id.profile_id_email);
        menu = findViewById(R.id.profile_id_menu);
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.profile_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.logout) {
                    if(token.equals("google")) {
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ProfileActivity.this);
                        if (acct != null) {
                            FirebaseAuth.getInstance().signOut();
                            LoginActivity.mGoogleSignInClient.signOut();
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else if(token.equals("firebase")){
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        LoginManager.getInstance().logOut();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                return true;
            }
        });
    }
}