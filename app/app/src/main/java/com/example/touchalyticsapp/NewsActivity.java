package com.example.touchalyticsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.BuildCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewsActivity extends AppCompatActivity {

    private WebView newsView;
    private Swipe currentSwipe;
    private DatabaseReference database;
    private int userId;
    private boolean doAuthentication;

    private final int minSwipeEvents = 5;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseApp app = FirebaseApp.initializeApp(this);
        System.out.println(app);
        database = FirebaseDatabase.getInstance().getReference();
        OnBackPressedCallback backCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                newsView.goBack();
            }
        };

        Intent intent = getIntent();
        userId = intent.getIntExtra("USERID", -1);
        doAuthentication = intent.getBooleanExtra("USERPROFILED", false);

        EdgeToEdge.enable(this);
        setContentView(R.layout.news_view);

        newsView = findViewById(R.id.NewsWebView);
        initializeWebView();

        newsView.setOnTouchListener((v, event) -> {
            handleTouchEvent(event);
            return false;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        WebSettings webSettings = newsView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        newsView.setWebViewClient(new WebViewClient());
        newsView.loadUrl("https://www.apnews.com/");
    }

    private void handleTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                newSwipe(e);
                break;
            case MotionEvent.ACTION_MOVE:
                updateSwipe(e);
                break;
            case MotionEvent.ACTION_UP:
                endSwipe(e);
        }
    }

    private void newSwipe(MotionEvent e) {
        if(this.currentSwipe != null) {
            endSwipe(e);
        }
        currentSwipe = new Swipe(this.userId);
        currentSwipe.push(e);
    }

    private void updateSwipe(MotionEvent e) {
        if(this.currentSwipe != null)
            this.currentSwipe.push(e);
    }

    private void endSwipe(MotionEvent e) {
        updateSwipe(e);
        if(this.currentSwipe.points.size() >= minSwipeEvents) {
            if(doAuthentication)
                authSwipe(this.currentSwipe);
            else
                sendSwipe(this.currentSwipe);
        }
        this.currentSwipe = null;
    }

    private void authSwipe(Swipe s) {
        boolean keepAuth = new SwipeReport(s).auth("http://127.0.0.1:5001/auth");
        if(!keepAuth) {
            Intent intent = new Intent(NewsActivity.this, LoginActivity.class);
            intent.putExtra("UNAUTHORIZED", true);
            startActivity(intent);
        }
    }

    private void sendSwipe(Swipe s) {
        DatabaseReference swipesRef = database.child("Swipes");
        DatabaseReference newSwipeRef = swipesRef.push();

        newSwipeRef.child("UserID").setValue(s.userId);
        new SwipeReport(s).send(newSwipeRef);
    }
}
