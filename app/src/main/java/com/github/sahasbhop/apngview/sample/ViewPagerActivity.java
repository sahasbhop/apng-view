package com.github.sahasbhop.apngview.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.MenuItem;

import java.util.ArrayList;

public class ViewPagerActivity extends AppCompatActivity {

    private ArrayList<Pair<String, String>> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        urls = new ArrayList<>();

        urls.add(new Pair<>("Asset 1", "assets://apng/apng_geneva_drive.png"));
        urls.add(new Pair<>("Asset 2", "assets://apng/00BA08BA557D66FE8B69397B27E6D3EE.png"));
        urls.add(new Pair<>("Asset 3", "assets://apng/55F7AAB0AB28AA3F89E5B27F4ED08ECF.png"));
        urls.add(new Pair<>("Asset 4", "assets://apng/A9041AB7916E493C6381F96B5EE29C3D.png"));
        urls.add(new Pair<>("Asset 5", "assets://apng/B74DEA83B6791665C515ECC972826826.png"));
        urls.add(new Pair<>("Asset 6", "assets://apng/avatar_talk_1.png"));
        urls.add(new Pair<>("Internet Source", "http://littlesvr.ca/apng/images/clock.png"));

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new LocalViewPager(getSupportFragmentManager()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class LocalViewPager extends FragmentPagerAdapter {
        public LocalViewPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Pair<String, String> pair = urls.get(position);
            return ImageFragment.newInstance(pair.first, pair.second);
        }

        @Override
        public int getCount() {
            return urls.size();
        }
    }
}
