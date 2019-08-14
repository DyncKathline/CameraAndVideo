package com.sparksoft.cameraandvideo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * 双SurfaceView
 */
public class LiveCameraActivity2 extends Activity {

    private CameraSurfaceView mTextureView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera2);

        mTextureView = findViewById(R.id.textureView);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_switch:
                mTextureView.switchCamera();
                break;
        }
    }
}