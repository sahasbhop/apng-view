APNG View
=====
APNG View is a library that supports playing animation from APNG image files on Android application.

Usage
-----
To load an image and start/stop an animation on users click
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	...
    ImageView imageView = (ImageView) findViewById(R.id.image_view1);
    
    // Display image from a file in assets
    String uri = "assets://apng/apng_geneva_drive.png";
    ApngImageLoader.getInstance().displayImage(uri, imageView);
    
    imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApngDrawable apngDrawable = ApngDrawable.getFromView(v);
                if (apngDrawable == null) return;
                
                if (apngDrawable.isRunning()) {
                    apngDrawable.stop(); // Stop animation
                } else {
                    apngDrawable.setNumPlays(3); // Fix number of repetition
                    apngDrawable.start(); // Start animation
                }
            }
    });
}
```
To start an animation immediately after an image has finished loading, passing an object of ApngConfig to the method displayApng(), see the example code below.
```java
ApngImageLoader.getInstance()
	.displayApng(uri, imageView, 
        new ApngImageLoader.ApngConfig(3, true));
```
ApngConfig has 2 attributes e.g. number of repetition and auto-start. If the number of repetition is less than 1, the library will try to grab the meta-data from APNG source file, if it's not specified, the animation will be playing continuously.

Different formats of URI that are also supported:
```
String uri = "file:///sdcard/apng_geneva_drive.png"
```
And
```
String uri = "http://littlesvr.ca/apng/images/clock.png"
```

Installation
-----
Use Gradle:
```gradle
dependencies {
    compile 'com.github.sahasbhop:apng-view:1.3'
}
```
Image loader library (Universal Image Loader), is needed to be initialized before start using. Try adding the following code.

In MyApplication.java
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        .. // Initializing stuffs e.g. Google Analytics, Crashlytics, etc.
        ApngImageLoader.getInstance().init(getApplicationContext());
    }
}
```
Dependencies
-----
* [PNGJ v2.1.1](https://github.com/leonbloy/pngj/)
* [Universal Image Loader v1.9.4](https://github.com/nostra13/Android-Universal-Image-Loader)
* [Apache Common IO v2.4](https://commons.apache.org/proper/commons-io/)

License
-----
Copyright 2015 Sahasbhop Suvadhanabhakdi.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.