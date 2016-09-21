package com.github.sahasbhop.apngview.assist;


import com.github.sahasbhop.apngview.ApngDrawable;

public abstract class ApngListener {

    public void onAnimationStart(ApngDrawable apngDrawable) {}

    public void onAnimationRepeat(ApngDrawable apngDrawable) {}

    public void onAnimationEnd(ApngDrawable apngDrawable) {}

}
