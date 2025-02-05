package com.example.touchalyticsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class NewsActivity extends AppCompatActivity {

    private WebView newsView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        FirebaseApp.initializeApp(this);
//        database = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
//        userID = intent.getIntExtra("USERID", -1);
//        Log.i("UserID", String.valueOf(userID));
//        setUser(userID);

//        checkIfUserExists(userID, result -> userExists = "exists".equals(result));

        EdgeToEdge.enable(this);
        setContentView(R.layout.news_view);

        newsView = findViewById(R.id.NewsWebView);
        initializeWebView();

//        newsView.setOnTouchListener((v, event) -> {
//            handleTouchEvent(event);
//            return false;
//        });

//        getCountofSwipes(userID, count -> {
//            this.count = count;
//            Log.i("Count", String.valueOf(count));
//        });
//
//        Log.i("Count", String.valueOf(count));


//        Button homeButton = findViewById(R.id.Home);
//        homeButton.setOnClickListener(v -> {
//            Intent homeIntent = new Intent(NewsActivity.this, MainActivity.class);
//            homeIntent.putExtra("IsLoggedin", true);
//            startActivity(homeIntent);
//            finish();
//        });
//
//        Button backbutton = findViewById(R.id.previous);
//        backbutton.setOnClickListener(v -> newsView.goBack());
//
//        Button nextbutton = findViewById(R.id.next);
//        nextbutton.setOnClickListener(v -> newsView.goForward());

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        WebSettings webSettings = newsView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        newsView.setWebViewClient(new WebViewClient());
        newsView.loadUrl("https://www.apnews.com/");
    }
}
