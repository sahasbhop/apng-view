package com.github.sahasbhop.apngview.sample;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.github.sahasbhop.apngview.ApngDrawable;
import com.github.sahasbhop.apngview.ApngImageLoader;
import com.github.sahasbhop.apngview.ApngImageLoadingListener;
import com.github.sahasbhop.flog.FLog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            final ImageView apngView = (ImageView) findViewById(R.id.image_apng);
            apngView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Drawable drawable = apngView.getDrawable();

                    if (drawable instanceof ApngDrawable) {
                        if (((ApngDrawable) drawable).isRunning()) {
                            FLog.v("Stop animation");
                            ((ApngDrawable) drawable).stop();
                        } else {
                            FLog.v("Start animation");
                            ((ApngDrawable) drawable).setNumPlays(3);
                            ((ApngDrawable) drawable).start();
                        }
                    }
                }
            });

//			String url = "http://littlesvr.ca/apng/images/clock.png";
//			String url = "http://toonstalk-staging.s3-ap-southeast-1.amazonaws.com/avatars/5423e12361626368a2070000/normals/original/avatar_normal.png?1411705571";
//			String assetName = "00BA08BA557D66FE8B69397B27E6D3EE.png";
//			String assetName = "55F7AAB0AB28AA3F89E5B27F4ED08ECF.png";
//			String assetName = "A9041AB7916E493C6381F96B5EE29C3D.png";
//			String assetName = "B74DEA83B6791665C515ECC972826826.png";
//			String assetName = "avatar_talk_1.png";
            String assetName = "apng_geneva_drive.png";

//			Uri uri = Uri.parse(url);
            Uri uri = Uri.parse("assets://apng/" + assetName);

            ApngImageLoadingListener loadingListener = new ApngImageLoadingListener(getApplicationContext(), uri);
            ApngImageLoader.getInstance().displayImage(uri.toString(), apngView, loadingListener);

        } catch (Exception e) {
            FLog.w("Error: %s", e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
