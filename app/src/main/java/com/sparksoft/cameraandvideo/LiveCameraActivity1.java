package com.sparksoft.cameraandvideo;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * TextureView 和 SurfaceView
 */
public class LiveCameraActivity1 extends Activity implements View.OnClickListener {
    public static final String TAG = "LiveCameraActivity";
    //远端的视图
    private TextureView remote_sv;
    // 本地的视图
    private CameraTextureView local_sv;
    private RelativeLayout remote_rl;
    private RelativeLayout local_rl;

    private int StateAB = 0;
    private int StateBA = 1;
    private int mSate = StateAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera1);

        remote_rl = findViewById(R.id.remote_rl);
        remote_sv = findViewById(R.id.remote_view);
        remote_sv.setOnClickListener(this);
        remote_sv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Canvas c = remote_sv.lockCanvas();
                // 2.开画
                Paint p = new Paint();
                p.setColor(Color.RED);
                Rect aa = new Rect(0, 0, remote_sv.getWidth(),
                        remote_sv.getHeight());
                c.drawRect(aa, p);
                // 3. 解锁画布 更新提交屏幕显示内容
                remote_sv.unlockCanvasAndPost(c);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "remote_holder surfaceChanged width: " + width + ",height: " + height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        local_rl = findViewById(R.id.local_rl);
        local_sv = findViewById(R.id.local_view);
        local_sv.setOnClickListener(this);
//        local_sv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                Canvas c = local_sv.lockCanvas();
//                // 2.开画
//                Paint p = new Paint();
//                p.setColor(Color.GREEN);
//                Rect aa = new Rect(0, 0, local_sv.getWidth(),
//                        local_sv.getHeight());
//                c.drawRect(aa, p);
//                // 3. 解锁画布 更新提交屏幕显示内容
//                local_sv.unlockCanvasAndPost(c);
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//                Log.d(TAG, "local_holder surfaceChanged width: " + width + ",height: " + height);
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//            }
//        });

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                int localWidth = local_rl.getMeasuredWidth();
                int localHeight = local_rl.getMeasuredHeight();
                int remoteWidth = remote_rl.getMeasuredWidth();
                int remoteHeight = remote_rl.getMeasuredHeight();
                Log.d(TAG, String.format(" sw: %d, sh: %d, bw: %d, bh: %d", localWidth, localHeight, remoteWidth, remoteHeight));
            }
        });

        findViewById(R.id.btn_switch).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.local_view:
                Log.d(TAG, " onClick local_view: " + mSate);
                if (mSate == StateAB) {
                    switchView(local_rl, local_sv, remote_rl, remote_sv);
                    mSate = StateBA;
                }
                break;
            case R.id.remote_view:
                Log.d(TAG, " onClick emote_view: " + mSate);
                if (mSate == StateBA) {
                    switchView(remote_rl, remote_sv, local_rl, local_sv);
                    mSate = StateAB;
                }
                break;
            case R.id.btn_switch:
                local_sv.switchCamera();
            default:
                break;
        }

    }

    /**
     * 大小像切换
     *
     * @param smallView        即将要变成的大像
     * @param smallSurfaceView
     * @param bigView          即将要变成的小像
     * @param bigSurfaceView
     */
    private void switchView(View smallView, TextureView smallSurfaceView, View bigView, TextureView bigSurfaceView) {
        RelativeLayout parentView = (RelativeLayout) smallView.getParent();

        int smallWidth = smallView.getWidth();
        int smallHeight = smallView.getHeight();
        int bigWidth = bigView.getWidth();
        int bigHeight = bigView.getHeight();
        Log.d(TAG, String.format("before sw: %d, sh: %d, bw: %d, bh: %d", smallView.getWidth(), smallView.getHeight(), bigView.getWidth(), bigView.getHeight()));
        ViewGroup.LayoutParams sLayoutParams = smallView.getLayoutParams();//变大像
        sLayoutParams.width = bigWidth;
        sLayoutParams.height = bigHeight;
        smallView.setLayoutParams(sLayoutParams);

        ViewGroup.LayoutParams bLayoutParams = bigView.getLayoutParams();//变小像
        bLayoutParams.width = smallWidth;
        bLayoutParams.height = smallHeight;
        bigSurfaceView.setLayoutParams(bLayoutParams);
        parentView.bringChildToFront(bigView);

        smallView.requestLayout();
        bigView.requestLayout();
        Log.d(TAG, String.format("after sw: %d, sh: %d, bw: %d, bh: %d", smallView.getWidth(), smallView.getHeight(), bigView.getWidth(), bigView.getHeight()));
    }
}