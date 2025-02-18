package com.example.touchalyticsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private final String authServer = "http://localhost:5000/login";
    private DatabaseReference database;

    public void attemptLogin() {
        String username = ((EditText)findViewById(R.id.inputUsername)).getText().toString().trim();

        // temp
        int USERID = username.hashCode();

        Intent intent = new Intent(LoginActivity.this, NewsActivity.class);
        intent.putExtra("USERID", USERID);
        startActivity(intent);
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

        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(e -> {
            attemptLogin();
        });

//        FirebaseApp.initializeApp(this);
//        database = FirebaseDatabase.getInstance().getReference();
    }
}