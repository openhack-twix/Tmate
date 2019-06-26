package com.example.tmate;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainButtonClickListener mainButtonClickListener = new MainButtonClickListener();
        //FloatingActionButton fab = findViewById(R.id.main_fab);

        //fab.setOnClickListener(mainButtonClickListener);


    }


    public class MainButtonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            switch (v.getId()){

            }

        }
    }

    public void requestFacebookLogin(View view){

    }

}
