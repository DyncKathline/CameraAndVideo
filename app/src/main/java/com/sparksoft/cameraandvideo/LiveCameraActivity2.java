package com.sparksoft.cameraandvideo;

import android.app.Activity;
import android.os.Bundle;

/**
 * 双SurfaceView
 */
public class LiveCameraActivity2 extends Activity {
    private MSurfaceView mTextureView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera2);

        mTextureView = findViewById(R.id.textureView);
    }
}