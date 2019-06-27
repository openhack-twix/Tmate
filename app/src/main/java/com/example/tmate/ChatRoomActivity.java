package com.example.tmate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

import static com.example.tmate.MainActivity.ROOMS_API;
import static com.example.tmate.MainActivity.USERID;
import static com.example.tmate.MainActivity.myCity;

public class ChatRoomActivity extends AppCompatActivity {

    private ListView room_list;
    private ChatRoomAdapter chatRoomAdapter;

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
                    if(users.getJSONObject(j).getString("_id").equals(USERID)){

                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
