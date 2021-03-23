package com.omarolmedoferrer.firebaselogin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    EditText email, password;
    TextView recover;
    Button login, signup, google, facebook;
    FirebaseAuth firebase;
    String provider;
    GoogleSignInOptions gso;
    GoogleSignInClient googleClient;
    int RC_SIGN_IN = 100;
    CallbackManager callbackManager = CallbackManager.Factory.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // selecting views
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        recover = findViewById(R.id.recover);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);

        // initializing firebase
        firebase = FirebaseAuth.getInstance();

        // initializing google sing in
        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // check if logged in
        if (firebase.getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, Profile.class));
            finish();
        }

        // login with email and password
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString().trim();
                String userPass = password.getText().toString().trim();

                // fields validation
                if(TextUtils.isEmpty(userEmail)){
                    email.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(userPass)){
                    password.setError("Password is required");
                    return;
                }

                // login to firebase
                firebase.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebase.getCurrentUser();
                            Log.d("user", user.toString());

                            startActivity(new Intent(getApplicationContext(), com.omarolmedoferrer.firebaselogin.Profile.class).putExtra("provider", "firebase"));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // password recovery
        recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder passwordReset = new AlertDialog.Builder(com.omarolmedoferrer.firebaselogin.MainActivity.this);
                final EditText resetEmail = new EditText(v.getContext());

                passwordReset.setTitle("Password recovery");
                passwordReset.setMessage("Enter your email to receive a recovering email:");
                passwordReset.setView(resetEmail);

                passwordReset.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // sends recovery message
                        String email = resetEmail.getText().toString();
                        firebase.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "A reset link has been sent to your email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "There was an error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordReset.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                passwordReset.create().show();
            }
        });

        // validation and registration
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString().trim();
                String userPass = password.getText().toString().trim();

                // fields validation
                if(TextUtils.isEmpty(userEmail)){
                    email.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(userPass)){
                    password.setError("Password is required");
                    return;
                }

                if(userPass.length() < 6){
                    password.setError("Password must be at least 6 characters long");
                    return;
                }

                // registration
                firebase.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // email verification
                            firebase.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "An verification email has been sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "There was an error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            // log message
                            Log.d("firebase", "User created successfully");

                            // change to logged in
                            startActivity(new Intent(getApplicationContext(), com.omarolmedoferrer.firebaselogin.Profile.class).putExtra("provider", "firebase"));
                            finish();
                        } else {
                            Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "There was an error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // login with google
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // opens up google authentication activity
                Intent signInIntent = googleClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // login with facebook
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(com.omarolmedoferrer.firebaselogin.MainActivity.this, Arrays.asList("email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // nothing
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "There was an error logging in", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // gets result of login activities
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // for google
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("firebase", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("firebase", "Google sign in failed", e);
                // ...
            }
        }
    }

    // sends Google credentials to Firebase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebase.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("firebase", "signInWithCredential:success");

                    startActivity(new Intent(getApplicationContext(), com.omarolmedoferrer.firebaselogin.MainActivity.class).putExtra("provider", "google"));
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("firebase", "signInWithCredential:failure", task.getException());
                    Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // sends Facebook credentials to firebase
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("facebook", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebase.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("facebook", "signInWithCredential:success");

                    startActivity(new Intent(getApplicationContext(), com.omarolmedoferrer.firebaselogin.MainActivity.class).putExtra("provider", "facebook"));
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("facebook", "signInWithCredential:failure", task.getException());
                    Toast.makeText(com.omarolmedoferrer.firebaselogin.MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}