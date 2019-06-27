package com.example.tmate;

//import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MapView map;

    private TextView tvHour = null;
    private TextView tvMinutes = null;
    private Context mContext = null;
    private Calendar targetCalendar = null;
    private int targetHour = 0;
    private int targetMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        MainButtonClickListener mainButtonClickListener = new MainButtonClickListener();


        Button btn_send = findViewById(R.id.main_btn_send);
        LinearLayout timeLayout = findViewById(R.id.main_layout_timepicker);
        tvHour = findViewById(R.id.main_tv_timeHour);
        tvMinutes = findViewById(R.id.main_tv_timeMinute);


        btn_send.setOnClickListener(mainButtonClickListener);
        timeLayout.setOnClickListener(mainButtonClickListener);


        //kakao Map API Configuration
        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        // 중심점 변경
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);

        // 줌 레벨 변경
        mapView.setZoomLevel(7, true);

        // 중심점 변경 + 줌 레벨 변경
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(33.41, 126.52), 9, true);

        // 줌 인
        mapView.zoomIn(true);

        // 줌 아웃
        mapView.zoomOut(true);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Default Marker");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633));
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

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
