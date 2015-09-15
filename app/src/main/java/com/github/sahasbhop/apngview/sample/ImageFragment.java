package com.github.sahasbhop.apngview.sample;

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
                ApngDrawable apngDrawable = ApngDrawable.getFromView(v);
                if (apngDrawable == null) return;

                if (apngDrawable.isRunning()) {
                    FLog.v("Stop animation");
                    apngDrawable.stop();
                } else {
                    FLog.v("Start animation");
                    apngDrawable.setNumPlays(3);
                    apngDrawable.start();
                }
            }
        });

        ApngImageLoader.getInstance().displayImage(url, imageView);

        return view;
    }
}
