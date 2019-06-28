package com.example.tmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.example.tmate.MainActivity.NICKNAME;
import static com.example.tmate.MainActivity.ROOMS_API;
import static com.example.tmate.MainActivity.USERID;
import static com.example.tmate.MainActivity.mSocket;
import static com.example.tmate.MainActivity.myCity;

public class ChatRoomActivity extends AppCompatActivity {

    private ListView room_list;
    private ChatRoomAdapter chatRoomAdapter;

    private String roomid;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        room_list = findViewById(R.id.list_chat_room);

        chatRoomAdapter = new ChatRoomAdapter(this);
        room_list.setAdapter(chatRoomAdapter);

        //GET room array
        String room_url = ROOMS_API + myCity;
        final String result = LoginActivity.sendGet(room_url);
        Log.e("ROOMS",result);

        JSONArray room_array = null;
        try {
            room_array = new JSONArray(result);
            for(int i = 0; i<room_array.length(); i++){
                JSONObject temp = room_array.getJSONObject(i);

                JSONArray users = temp.getJSONArray("users");
                //check whether user is already in the room
                for(int j = 0; j<users.length(); j++){
                    if(users.getJSONObject(j).getString("nickname").equals(NICKNAME)){
                        JSONArray logs = temp.getJSONArray("logs");
                        String last_message;
                        if(logs.length()==0){
                            last_message = "";
                            Log.d("THEREISONE","EMPTY");
                        }else{
                            last_message = logs.getJSONObject(logs.length()-1).getString("message");
                            Log.e("THEREISONE",last_message);
                        }

                        ChatRoom newRoom = new ChatRoom(temp.getString("title"),temp.getString("_id"),last_message);
                        chatRoomAdapter.add(newRoom);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        room_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatRoom clicked = (ChatRoom) chatRoomAdapter.getItem(i);
                String roomID = clicked.getRoomCode();
                Log.e("ROOMID",roomID);

                JSONObject joinData = new JSONObject();
                try {
                    joinData.put("roomid",roomID);
                    joinData.put("userid",USERID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                roomid = roomID;
                mSocket.emit("join",joinData);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}
