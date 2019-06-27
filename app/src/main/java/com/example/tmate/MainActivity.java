package com.example.tmate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvHour = null;
    private TextView tvMinutes = null;
    private Context mContext = null;
    private Calendar targetCalendar = null;
    private int targetHour = 0;
    private int targetMinutes = 0;

    private GoogleMap myMap;

    private double myLat = 0.0;
    private double myLng = 0.0;
    private String myCity = "";

    //Test LatLng
    private static final double LAT_INTERLAKEN = 46.685523;
    private static final double LNG_INTERLAKEN = 7.858514;

    private final boolean[] is_moved = {false};
    public static Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionAndContinue();

        mContext = this;

        MainButtonClickListener mainButtonClickListener = new MainButtonClickListener();


        ImageButton btn_send = findViewById(R.id.main_btn_send);
        ConstraintLayout timeLayout = findViewById(R.id.main_layout_timepicker);
        tvHour = findViewById(R.id.main_tv_timeHour);
        tvMinutes = findViewById(R.id.main_tv_timeMinute);

        FloatingActionButton floating_chat_room = findViewById(R.id.floating_chatroom);
        FloatingActionButton floating_mylocation = findViewById(R.id.floating_myLocation);
        floating_mylocation.setImageResource(R.drawable.floating_mylocation_3x);

        btn_send.setOnClickListener(mainButtonClickListener);
        timeLayout.setOnClickListener(mainButtonClickListener);
        floating_chat_room.setOnClickListener(mainButtonClickListener);

        try {
            mSocket = IO.socket(LoginActivity.SERVER_URL);
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject data = new JSONObject();
        try {
            data.put("city", "익산");
            data.put("title", "익산");
            data.put("content", "익산");
            data.put("lat", 0.1);
            data.put("lon", 0.3);
            data.put("userid", "test");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("create room", data);
    }

    private void setMap() {
        //Google Map API configuration
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        LatLng SEOUL = new LatLng(37.56, 126.97);
        LatLng INTERLAKEN = new LatLng(LAT_INTERLAKEN, LNG_INTERLAKEN);

        myMap = map;

        //Get current location

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //myLat = location.getLatitude();
        //myLng = location.getLongitude();

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
            Toast.makeText(this, myCity, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "대체 어디 계신지...", Toast.LENGTH_LONG).show();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(current);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_me));
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 14));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng clickCoords) {
                map.clear();

                myLat = clickCoords.latitude;
                myLng = clickCoords.longitude;

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(clickCoords);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_me));

                map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
            }
        });
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (is_moved[0]) {
                    map.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(map.getCameraPosition().target);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_me));

                    map.addMarker(markerOptions);
                }
                is_moved[0] = false;
            }
        });
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                is_moved[0] = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (targetCalendar == null) {
            targetCalendar = Calendar.getInstance();
        }

        targetHour = targetCalendar.get(Calendar.HOUR_OF_DAY);
        targetMinutes = targetCalendar.get(Calendar.MINUTE);
        setTimeTextView(targetHour, targetMinutes);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //Nothing to do.
            }
        });
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 120);
            } else {
                Toast.makeText(this, "권한허용이 필요합니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            setMap();
        }
    }

    public void setTimeTextView(int targetHour, int targetMinutes) {
        tvHour.setText(String.valueOf(targetHour));
        if (targetMinutes < 10) {
            tvMinutes.setText("0" + targetMinutes);
            return;
        }
        tvMinutes.setText(String.valueOf(targetMinutes));
    }

    public class MainButtonClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.main_btn_send:
                    Toast.makeText(MainActivity.this, "send", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.main_layout_timepicker:
                    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if (view.isShown()) {
                                targetHour = hourOfDay;
                                targetMinutes = minute;
                                setTimeTextView(targetHour, targetMinutes);
                            }
                        }
                    };

                    TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, onTimeSetListener, targetHour, targetMinutes, true);
                    timePickerDialog.setTitle("몇 시까지?");
                    timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    timePickerDialog.show();
                    break;
                case R.id.floating_chatroom:
                    Intent intent_chatroom = new Intent(mContext, ChatActivity.class);
                    startActivity(intent_chatroom);
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
}
