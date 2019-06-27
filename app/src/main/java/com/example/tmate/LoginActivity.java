package com.example.tmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButtonClickListener loginButtonClickListener = new LoginButtonClickListener();
        Button btn_fb = findViewById(R.id.login_btn_fb);
        Button btn_kakao = findViewById(R.id.login_btn_kakao);
        ImageView main_logo = findViewById(R.id.login_iv_logo);

        btn_fb.setOnClickListener(loginButtonClickListener);
        btn_kakao.setOnClickListener(loginButtonClickListener);
        main_logo.setOnClickListener(loginButtonClickListener);


    }


    public class LoginButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_btn_fb:
                    break;

                case R.id.login_btn_kakao:
                    break;

                case R.id.login_iv_logo:
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);
                    break;

            }
        }
    }
}
