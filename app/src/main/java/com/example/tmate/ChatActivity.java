package com.example.tmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatActivity extends AppCompatActivity {

    private Socket mSocket = MainActivity.mSocket;

    private TextView tv_roomtitle;
    private ListView logView;
    private EditText et_chat_room;
    private ImageButton btn_chat_send;
    private Button btn_chat_exit;

    private String roomtitle;
    private String logs;
    private String roomid;

    private String myname = MainActivity.NICKNAME;

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageAdapter = new MessageAdapter(this);

        Intent data = getIntent();
        roomtitle = data.getStringExtra("title");
        logs = data.getStringExtra("logs");
        roomid = data.getStringExtra("roomid");

        Log.e("JOINED LOG",logs);

        mSocket.on("receive", onReceive);

        tv_roomtitle = findViewById(R.id.tv_roomtitle);
        tv_roomtitle.setText(roomtitle);
        logView = findViewById(R.id.messages_view);
        logView.setAdapter(messageAdapter);
        logView.setDivider(null);
        et_chat_room = findViewById(R.id.et_chat_text);
        btn_chat_exit = findViewById(R.id.btn_chat_exit);
        btn_chat_send = findViewById(R.id.btn_chat_send);
        btn_chat_send.setImageResource(R.drawable.icon_send);
        btn_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_chat_room.getText().toString().trim();
                if(text.length()==0){
                    return;
                }
                JSONObject message = new JSONObject();
                try {
                    message.put("roomid",roomid);
                    message.put("userid",MainActivity.USERID);
                    message.put("message",text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("send message",message);
                et_chat_room.setText("");
            }
        });

        btn_chat_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //param : userid, roomid
                JSONObject data = new JSONObject();
                try {
                    data.put("userid",MainActivity.USERID);
                    data.put("roomid",roomid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("leave",data);
                finish();
            }
        });

        try {
            makeLog();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void makeLog() throws JSONException {
        JSONArray log = new JSONArray(logs);
        for(int i=0; i<log.length(); i++){
            JSONObject temp = log.getJSONObject(i);
            JSONObject user = temp.getJSONObject("user");
            boolean is_me = (user.getString("name").equals(MainActivity.NICKNAME));

            MemberData newMember = new MemberData(user.getString("name"),user.getString("colorcode"));
            Message msg = new Message(temp.getString("message"),newMember,is_me);
            messageAdapter.add(msg);
        }
        scrollMyListViewToBottom();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mSocket.off("receive");
    }

    private void scrollMyListViewToBottom() {
        logView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                logView.setSelection(logView.getCount() - 1);
            }
        });
    }

    private Emitter.Listener onReceive= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("onReceive", "onReceive");

            try {
                JSONObject data = (JSONObject) args[0];
                //Toast.makeText(ChatActivity.this, "SOMETHING CAME", Toast.LENGTH_LONG).show();
                JSONObject user = data.getJSONObject("user");
                String name = user.getString("name");
                String colorcode = user.getString("colorcode");
                String message = data.getString("message");
                Log.e("NAME", name);
                Log.e("MESSAGE",message);
                Log.e("COLOR",colorcode);

                boolean is_me = (name.equals(MainActivity.NICKNAME));

                MemberData newMember = new MemberData(name,colorcode);
                Message msg = new Message(message,newMember,is_me);

                messageAdapter.add(msg);
                // scroll the ListView to the last added element
                scrollMyListViewToBottom();

            }catch (Exception e){
                Log.e("onReceive ERROR", e.getMessage());
                e.printStackTrace();
            }
        }
    };
}
