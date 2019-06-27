package com.example.tmate;

//import android.support.design.widget.FloatingActionButton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvHour = null;
    private TextView tvMinutes = null;
    private Context mContext = null;
    private Calendar targetCalendar = null;
    private int targetHour = 0;
    private int targetMinutes = 0;

    private double myLat = 0.0;
    private double myLng = 0.0;
    private String myCity = "";

    //Test LatLng
    private static final double LAT_INTERLAKEN = 46.685523;
    private static final double LNG_INTERLAKEN = 7.858514;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},120);
        }
        setContentView(R.layout.activity_main);

        mContext = this;

        MainButtonClickListener mainButtonClickListener = new MainButtonClickListener();


        Button btn_send = findViewById(R.id.main_btn_send);
        LinearLayout timeLayout = findViewById(R.id.main_layout_timepicker);
        tvHour = findViewById(R.id.main_tv_timeHour);
        tvMinutes = findViewById(R.id.main_tv_timeMinute);


        btn_send.setOnClickListener(mainButtonClickListener);
        timeLayout.setOnClickListener(mainButtonClickListener);

        //Google Map API configuration
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        LatLng SEOUL = new LatLng(37.56, 126.97);
        LatLng INTERLAKEN = new LatLng(LAT_INTERLAKEN, LNG_INTERLAKEN);

        //Get current location

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        myLat = location.getLatitude();
        myLng = location.getLongitude();

        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

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
            Toast.makeText(this,myCity, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,"대체 어디 계신지...", Toast.LENGTH_LONG).show();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(current);
        markerOptions.title("나, 여기!");
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
                markerOptions.title("나, 여기!");
                map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
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
    protected void onDestroy(){
        super.onDestroy();
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //Nothing to do.
            }
        });
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
            }

        }
    }


}
