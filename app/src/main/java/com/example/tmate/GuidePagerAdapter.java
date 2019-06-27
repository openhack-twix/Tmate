package com.example.tmate;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class GuidePagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_PAGES = 3;

    public GuidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FirstFragment().newInstance();
            case 1:
                return new SecondFragment().newInstance();
            case 2:
                return new ThirdFragment().newInstance();
        }
        return new Fragment();
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
