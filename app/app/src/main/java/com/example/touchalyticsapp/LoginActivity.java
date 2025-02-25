package com.example.touchalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private final String authServer = "http://localhost:5000/login";
    private DatabaseReference database;
    private int currentUserID = -1;
    private boolean userProfiled = false;
    private int nextUserID;

    public void promptCreateAccount(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new account?");
        final TextView text = new TextView(this);
        text.setText(String.format("No account for %s exists. Create one?", username));

        builder.setPositiveButton("OK", (dialog, i) -> {
            DatabaseReference newUser = database.child("Users").push();

            currentUserID = nextUserID;
            newUser.child("username").setValue(username);
            newUser.child("userID").setValue(nextUserID);
            newUser.child("modelComplete").setValue(false);
            database.child("NumUsers").setValue(nextUserID+1);

            launchNewsActivity(currentUserID);
        });
        builder.setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss());
        builder.show();
    }

    private void launchNewsActivity(int userID) {
        launchNewsActivity(userID, false);
    }

    private void launchNewsActivity(int userID, boolean profileLearned) {
        Intent intent = new Intent(LoginActivity.this, NewsActivity.class);
        intent.putExtra("USERID", currentUserID);
        intent.putExtra("USERPROFILED", profileLearned);
        startActivity(intent);
    }

    public void attemptLogin() {
        String username = ((EditText)findViewById(R.id.inputUsername)).getText().toString().trim();

        DatabaseReference usersRef = database.child("Users");
        Query userQuery = usersRef.orderByChild("username").equalTo(username);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot.getKey());
                User currentUser = new User();
                if(!snapshot.hasChildren()) {
                    promptCreateAccount(username);
                    System.out.println(currentUserID);
                } else {
                    System.out.println("Children:");
                    for (DataSnapshot result : snapshot.getChildren()) {
                        System.out.println(result.getKey());
                        currentUser = result.getValue(User.class);
                        System.out.println(currentUser.userID);
                    }
                    currentUserID = currentUser.userID;
                    userProfiled = currentUser.modelComplete;
                }

                if(currentUserID != -1) {
                    launchNewsActivity(currentUserID, userProfiled);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void popupLoggedOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log in again");
        final TextView text = new TextView(this);
        text.setText("You were logged out due to unrecognized behavior");

        builder.setPositiveButton("Ok", (dialog, i) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if(intent.getBooleanExtra("UNAUTHORIZED", false)) {
            popupLoggedOut();
        }

        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(e -> {
            attemptLogin();
        });

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance().getReference();

        database.child("/NumUsers/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nextUserID = snapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Failed to retrieve number of users");
            }
        });
    }
}