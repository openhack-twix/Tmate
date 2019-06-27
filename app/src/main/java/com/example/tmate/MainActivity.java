package com.example.tmate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext = null;

    private TextView mesesage;

    private GoogleMap myMap;

    private double myLat = 0.0;
    private double myLng = 0.0;
    public static String myCity = "";

    //Test LatLng
    private static final double LAT_INTERLAKEN = 46.685523;
    private static final double LNG_INTERLAKEN = 7.858514;

    public static String ROOMS_API = "http://10.10.2.126:3000/api/rooms/";

    private final boolean[] is_moved = {false};
    public static Socket mSocket;

    public static String NICKNAME;
    public static String COLORCODE;
    public static String USERID;

    private String roomid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionAndContinue();

        mContext = this;

        Intent userdata = getIntent();

        NICKNAME = userdata.getStringExtra("nickname");
        COLORCODE = userdata.getStringExtra("colorcode");
        USERID = userdata.getStringExtra("userid");

        MainButtonClickListener mainButtonClickListener = new MainButtonClickListener();

        ImageButton btn_send = findViewById(R.id.main_btn_send);
        mesesage = findViewById(R.id.editText_messages);

        FloatingActionButton floating_mylocation = findViewById(R.id.floating_myLocation);
        floating_mylocation.setOnClickListener(mainButtonClickListener);
        FloatingActionButton floating_refresh = findViewById(R.id.floating_refresh);
        floating_refresh.setOnClickListener(mainButtonClickListener);
        FloatingActionButton floating_messages = findViewById(R.id.floating_messages);
        floating_messages.setOnClickListener(mainButtonClickListener);

        btn_send.setOnClickListener(mainButtonClickListener);

        try {
            mSocket = IO.socket(LoginActivity.SERVER_URL);
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSocket.on("create success", onSuccess);
        mSocket.on("join success", onJoin);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("Tmate를 종료하시겠습니까?");
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.finishAffinity(((Activity) mContext));
                System.runFinalizersOnExit(true);
                System.exit(0);
            }
        });
        builder.show();
    }

    private void setMap() {
        //Google Map API configuration
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        myMap = map;

        //Get current location

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        myLat = LAT_INTERLAKEN;
        myLng = LNG_INTERLAKEN;

        LatLng current = new LatLng(myLat, myLng);

        //Get city name
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(myLat, myLng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            Address city_addr = addresses.get(0);
            myCity = city_addr.getLocality();
        } else {
            Toast.makeText(this, "대체 어디 계신지...", Toast.LENGTH_LONG).show();
        }

        //Spread Markers

        //GET room array
        String room_url = ROOMS_API + myCity;
        final String result = LoginActivity.sendGet(room_url);
        Log.e("ROOMS", result);

        makeMarkers(map, result);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(current);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_me));
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 16));

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String roomID = marker.getSnippet();
                JSONObject joinData = new JSONObject();

                try {
                    joinData.put("roomid", roomID);
                    joinData.put("userid", MainActivity.USERID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                roomid = roomID;
                mSocket.emit("join", joinData);
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng clickCoords) {
                map.clear();
                makeMarkers(map, result);
                myLat = clickCoords.latitude;
                myLng = clickCoords.longitude;

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(clickCoords);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_me));

                map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
            }
        });
    }

    private void makeMarkers(GoogleMap map, String rooms) {
        try {
            JSONArray room_array = new JSONArray(rooms);
            for (int i = 0; i < room_array.length(); i++) {
                JSONObject temp = room_array.getJSONObject(i);

                JSONArray users = temp.getJSONArray("users");
                //check whether user is already in the room
                boolean occupied = false;
                for (int j = 0; j < users.length(); j++) {
                    if (users.getJSONObject(j).getString("_id").equals(USERID)) {
                        occupied = true;
                        break;
                    }
                }
                MarkerOptions newMarker = new MarkerOptions();
                LatLng pos = new LatLng(temp.getDouble("lat"), temp.getDouble("lon"));
                newMarker.position(pos);
                newMarker.title(temp.getString("title"));
                newMarker.snippet(temp.getString("_id"));
                if (occupied) {
                    newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.occupied_1x));
                } else {
                    newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.unoccupied_1x));
                }
                map.addMarker(newMarker);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("join success");
        mSocket.off("create success");
        mSocket.disconnect();
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //Nothing to do.
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            Intent refresh = new Intent(this,MainActivity.class);
            refresh.putExtra("nickname",NICKNAME);
            refresh.putExtra("colorcode",COLORCODE);
            refresh.putExtra("userid",USERID);
            startActivity(refresh);
            this.finish();

        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 120);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 120);
                Toast.makeText(this, "권한허용이 필요합니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            setMap();
        }
    }


    //CLICK LISTENER

    public class MainButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.floating_refresh:
                    Intent refresh = new Intent(mContext, MainActivity.class);
                    refresh.putExtra("nickname", NICKNAME);
                    refresh.putExtra("colorcode", COLORCODE);
                    refresh.putExtra("userid", USERID);
                    startActivity(refresh);
                    overridePendingTransition(0, 0);
                    finish();
                    break;
                case R.id.floating_myLocation:
                    myMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myLat, myLng)));
                    break;
                case R.id.floating_messages:
                    Intent intent = new Intent(MainActivity.this,ChatRoomActivity.class);
                    startActivityForResult(intent,1);
                    break;
                case R.id.main_btn_send:
                    //Gather information and emit to server socket
                    if (mesesage.getText().toString().trim().length() == 0) {
                        Toast.makeText(mContext, "할일을 입력해주세요!", Toast.LENGTH_LONG).show();
                        break;
                    }

                    JSONObject data = new JSONObject();
                    try {
                        data.put("city", myCity);
                        data.put("title", mesesage.getText().toString().trim());
                        data.put("content", "");
                        data.put("lat", myLat);
                        data.put("lon", myLng);
                        data.put("userid", USERID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("create room", data);
                    break;

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 120:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (!isGranted) {
                    Toast.makeText(this, "권한허용이 필요합니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
                Intent refresh = new Intent(this, MainActivity.class);
                startActivity(refresh);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    // Emitter Section
    private Emitter.Listener onSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String roomID = data.getString("roomid");
                Log.e("CREATE SUCCESS", roomID);

                JSONObject joinData = new JSONObject();
                joinData.put("roomid", roomID);
                joinData.put("userid", USERID);

                roomid = roomID;
                mSocket.emit("join", joinData);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                String logs = data.getString("logs");
                String title = data.getString("title");
                Log.e("JOINED ROOM", logs);
                Log.e("TITLE", title);

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("logs", logs);
                intent.putExtra("title", title);
                intent.putExtra("roomid", roomid);

                mesesage.setText("");
                startActivityForResult(intent,2);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
