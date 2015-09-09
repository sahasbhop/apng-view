package com.github.sahasbhop.apngview.sample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sahasbhop.apngview.ApngDrawable;
import com.github.sahasbhop.apngview.ApngImageLoader;
import com.github.sahasbhop.flog.FLog;

public class ImageFragment extends Fragment {

    private ImageView imageView;

    public static ImageFragment newInstance(String title, String url) {
        ImageFragment fragment = new ImageFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("url", url);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, null);

        Bundle arguments = getArguments();
        String title = arguments.getString("title");
        String url = arguments.getString("url");

        TextView textView = (TextView) view.findViewById(R.id.text_view);
        textView.setText(title);

        imageView = (ImageView) view.findViewById(R.id.image_view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable = ((ImageView) v).getDrawable();

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

        ApngImageLoader.getInstance().displayImage(url, imageView);

        return view;
    }

    @Override
    public void onDestroy() {
        if (imageView != null && imageView.getDrawable() instanceof ApngDrawable) {
            ((ApngDrawable) imageView.getDrawable()).recycleBitmaps();
        }
        super.onDestroy();
    }
}
