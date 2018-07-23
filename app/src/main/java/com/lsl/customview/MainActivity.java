package com.lsl.customview;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<FragmentInstance> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragmentData();

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = findViewById(R.id.table_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initFragmentData() {
        fragments.add(new FragmentInstance("FavoriteView", MainFragment.newInstance(R.layout.fragment_favorite_view)));
        fragments.add(new FragmentInstance("SlideTape", MainFragment.newInstance(R.layout.fragment_slide_tape)));
        fragments.add(new FragmentInstance("ClockView", MainFragment.newInstance(R.layout.fragment_clock_view)));
    }

    private class FragmentInstance {
        String title;
        Fragment fragment;

        FragmentInstance(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }

    private class CustomPagerAdapter extends FragmentPagerAdapter {

        CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position).fragment;
        }

        @Override
        public int getCount() {
            return fragments == null ? 0 : fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).title;
        }
    }
}
