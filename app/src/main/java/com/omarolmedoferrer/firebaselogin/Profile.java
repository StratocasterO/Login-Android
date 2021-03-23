package com.omarolmedoferrer.firebaselogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {
    Button logout;
    FirebaseAuth firebase;
    FirebaseUser user;
    boolean emailVerified;
    TextView userEmail, provider;
    ImageView verification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // select views
        logout = findViewById(R.id.logout);
        userEmail = findViewById(R.id.userEmail);
        provider = findViewById(R.id.userProvider);
        verification = findViewById(R.id.verification);

        // firebase
        firebase = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // setting user info
        if (user != null) {
            emailVerified = user.isEmailVerified();

            userEmail.setText(user.getEmail());

            // check for mail verification
            if (emailVerified) {
                verification.setVisibility(View.VISIBLE);
            } else {
                verification.setVisibility(View.INVISIBLE);
            }

            provider.setText(getIntent().getStringExtra("provider"));
        }

        // logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }
}