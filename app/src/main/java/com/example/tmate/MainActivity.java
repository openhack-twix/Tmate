package com.example.tmate;

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


    }


    @Override
    protected void onResume() {
        super.onResume();

        if (targetCalendar == null) {
            targetCalendar = Calendar.getInstance();
        }

        targetHour = targetCalendar.get(Calendar.HOUR_OF_DAY);
        targetMinutes = targetCalendar.get(Calendar.MINUTE);
        tvHour.setText(String.valueOf(targetHour));
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
                                tvHour.setText(String.valueOf(targetHour));
                                tvMinutes.setText(String.valueOf(targetMinutes));
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
