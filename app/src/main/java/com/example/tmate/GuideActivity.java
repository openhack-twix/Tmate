package com.example.tmate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        Button skipButton = findViewById(R.id.guide_btn_skip);
        final Button startButton = findViewById(R.id.guide_btn_start);
        final ViewPager mViewPager = findViewById(R.id.guide_vp_pager);

        GuideClickListener guideClickListener = new GuideClickListener();
        final ImageView indicator = findViewById(R.id.guide_indicator);
        skipButton.setOnClickListener(guideClickListener);
        startButton.setOnClickListener(guideClickListener);


        GuidePagerAdapter guidePagerAdapter = new GuidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(guidePagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                if (i == 0) {
                    indicator.setImageResource(R.drawable.indicator_first_3x);
                } else if (i == 1) {
                    indicator.setImageResource(R.drawable.indicator_second_3x);
                    startButton.setVisibility(View.GONE);

                } else {
                    startButton.setVisibility(View.VISIBLE);
                    indicator.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }


    public class GuideClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.guide_btn_start:

                case R.id.guide_btn_skip:
                    Intent intentLogin = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intentLogin);
                    break;

            }
        }
    }
}
