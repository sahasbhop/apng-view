package com.github.sahasbhop.apngview.sample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.sahasbhop.apngview.ApngDrawable;
import com.github.sahasbhop.apngview.ApngImageLoader;
import com.github.sahasbhop.apngview.ApngImageLoaderCallback;
import com.github.sahasbhop.flog.FLog;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {

    private List<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        urls = new ArrayList<>();
        urls.add("assets://apng/apng_geneva_drive.png");
        urls.add("assets://apng/00BA08BA557D66FE8B69397B27E6D3EE.png");
        urls.add("assets://apng/55F7AAB0AB28AA3F89E5B27F4ED08ECF.png");
        urls.add("assets://apng/A9041AB7916E493C6381F96B5EE29C3D.png");
        urls.add("assets://apng/B74DEA83B6791665C515ECC972826826.png");
        urls.add("assets://apng/avatar_talk_1.png");

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new LocalAdapter());
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

    class LocalAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_list_view_item, null);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) convertView.findViewById(R.id.text_view);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);

                convertView.setTag(R.id.tag_image, viewHolder);
            }

            String url = (String) getItem(position);

            ViewHolder viewHolder = (ViewHolder) convertView.getTag(R.id.tag_image);
            viewHolder.textView.setText(String.valueOf(position + 1));

            ApngImageLoader.getInstance().setCallback(new ApngImageLoaderCallback() {
                @Override
                public void onLoadComplete(String imageUri, View view) {
                    if (view == null || !(view instanceof ImageView)) return;

                    Drawable drawable = ((ImageView) view).getDrawable();

                    if (!(drawable instanceof ApngDrawable)) {
                        return;
                    }

                    FLog.d("Play: %s", imageUri);
                    ((ApngDrawable) drawable).setNumPlays(3);
                    ((ApngDrawable) drawable).start();
                }

                @Override
                public void onLoadFailed(String imageUri, View view) {
                    FLog.d("Failed loading: %s", imageUri);
                }
            });
            ApngImageLoader.getInstance().displayImage(url, viewHolder.imageView);

            return convertView;
        }

        class ViewHolder {
            TextView textView;
            ImageView imageView;
        }
    }
}
