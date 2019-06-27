package com.example.tmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private ImageButton custom_btn_fb, custom_btn_kakao;
    private LoginButton btn_kakao_login;
    private com.facebook.login.widget.LoginButton btn_fb_login;
    private CallbackManager callbackManager;

    public static String LOGIN_URL = "http://10.10.2.126:3000/auth/login";
    public static String SERVER_URL = "http://10.10.2.126:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Check whether token is valid or not
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if(isLoggedIn){
            serverLogin(accessToken.toString());
        }


        //findViewById
        ImageView main_logo = findViewById(R.id.login_iv_logo);
        btn_fb_login = findViewById(R.id.fb_login_button);
        custom_btn_fb = findViewById(R.id.custom_btn_fb);
        btn_kakao_login = findViewById(R.id.btn_kakao_login);
        custom_btn_kakao = findViewById(R.id.custom_btn_kakao);

        //OnClickListener
        LoginButtonClickListener loginButtonClickListener = new LoginButtonClickListener();
        main_logo.setOnClickListener(loginButtonClickListener);
        custom_btn_fb.setOnClickListener(loginButtonClickListener);
        custom_btn_kakao.setOnClickListener(loginButtonClickListener);


        callbackManager = CallbackManager.Factory.create();

        //FACEBOOK Auth Configuration
        btn_fb_login.setReadPermissions("email");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken token = AccessToken.getCurrentAccessToken();
                        serverLogin(token.toString());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });


        //KAKAO Auth Configuration
        SessionCallback callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void redirectToMain(String colorcode, String nickname, String userid) {
        Log.e("REDIRECT",colorcode);
        Log.e("USERID",userid);
        Log.e("NICKNAME",nickname);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("colorcode",colorcode);
        intent.putExtra("userid",userid);
        intent.putExtra("nickname",nickname);
        startActivity(intent);
        finish();
    }

    public static String sendGet(final String key_url){
        final String[] response = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(key_url);
                    StringBuffer res = new StringBuffer();

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int status = conn.getResponseCode();
                    if(status!=200){
                        throw new IOException("GET failed");
                    }else{
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine())!=null){
                            res.append(inputLine);
                        }
                        in.close();
                    }

                    conn.disconnect();

                    response[0] = res.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while(thread.isAlive()){}
        return response[0];
    }

    public static String sendPost(final byte[] postDataBytes, final String key_url){
        final String[] response = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(key_url);
                    StringBuffer res = new StringBuffer();

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    //set parameters
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                    os.write(postDataBytes);

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    int status = conn.getResponseCode();
                    if(status!=200){
                        throw new IOException("Post failed");
                    }else{
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine())!=null){
                            res.append(inputLine);
                        }
                        in.close();
                    }

                    conn.disconnect();

                    response[0] = res.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while(thread.isAlive()){}
        return response[0];
    }

    public static byte[] parseParameter(Map<String,Object> params){
        StringBuilder postData = new StringBuilder();
        byte[] postDataBytes = null;
        try {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append("=");
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }

            postDataBytes = postData.toString().getBytes("UTF-8");
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  postDataBytes;
    }



    public class LoginButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.login_iv_logo:
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);
                    break;

                case R.id.custom_btn_fb:
                    btn_fb_login.performClick();
                    break;

                case R.id.custom_btn_kakao:
                    btn_kakao_login.performClick();
                    break;


            }
        }
    }

    public class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            requestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        // 사용자 정보 요청
        public void requestMe() {
            // 사용자정보 요청 결과에 대한 Callback
            UserManagement.getInstance().requestMe(new MeResponseCallback() {
                // 세션 오픈 실패. 세션이 삭제된 경우,
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.e("SessionCallback :: ", "onSessionClosed : " + errorResult.getErrorMessage());
                }

                // 회원이 아닌 경우,

                @Override
                public void onNotSignedUp() {
                    Log.e("SessionCallback :: ", "onNotSignedUp");
                }

                // 사용자정보 요청에 성공한 경우,
                @Override
                public void onSuccess(UserProfile userProfile) {
                    Log.e("SessionCallback :: ", "onSuccess");
                    String nickname = userProfile.getNickname();

                    serverLogin(nickname);
                }


                // 사용자 정보 요청 실패
                @Override
                public void onFailure(ErrorResult errorResult) {
                    Log.e("SessionCallback :: ", "onFailure : " + errorResult.getErrorMessage());
                }
            });

        }
    }

    private void serverLogin(String value){
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("username",value);

        byte[] loginDataBytes = parseParameter(data);
        String resultLogin = sendPost(loginDataBytes,LOGIN_URL);
        Log.e("LOGIN", resultLogin);
        try {
            JSONObject result = new JSONObject(resultLogin);
            if(result.getString("success").equals("0")){
                Log.e("SUCCESS","0");
                redirectToMain(result.getString("colorcode"),result.getString("nickname"),result.getString("user"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }
}
