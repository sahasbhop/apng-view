package com.github.sahasbhop.apngview.sample;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.github.sahasbhop.apngview.ApngDrawable;
import com.github.sahasbhop.apngview.ApngImageLoader;
import com.github.sahasbhop.apngview.ApngImageLoadingListener;
import com.github.sahasbhop.flog.FLog;

public class MainActivity extends AppCompatActivity {

    private ImageView apngView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            apngView = (ImageView) findViewById(R.id.image_apng);
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
//			String url = "assets://apng/00BA08BA557D66FE8B69397B27E6D3EE.png";
//			String url = "assets://apng/55F7AAB0AB28AA3F89E5B27F4ED08ECF.png";
//			String url = "assets://apng/A9041AB7916E493C6381F96B5EE29C3D.png";
//			String url = "assets://apng/B74DEA83B6791665C515ECC972826826.png";
//			String url = "assets://apng/avatar_talk_1.png";
            String url = "assets://apng/apng_geneva_drive.png";

			Uri uri = Uri.parse(url);

            ApngImageLoadingListener loadingListener = new ApngImageLoadingListener(getApplicationContext(), uri);
            ApngImageLoader.getInstance().displayImage(uri.toString(), apngView, loadingListener);

        } catch (Exception e) {
            FLog.w("Error: %s", e.toString());
        }
    }

}
