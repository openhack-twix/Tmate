package com.example.tmate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private Socket mSocket = LoginActivity.mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


    }
}
