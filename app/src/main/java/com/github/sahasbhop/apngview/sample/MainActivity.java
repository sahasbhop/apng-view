package com.github.sahasbhop.apngview.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_view_pager).setOnClickListener(this);
        findViewById(R.id.button_list_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.button_view_pager:
                intent = new Intent(this, ViewPagerActivity.class);
                break;
            case R.id.button_list_view:
                intent = new Intent(this, ListViewActivity.class);
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
