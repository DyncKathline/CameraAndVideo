package com.sparksoft.cameraandvideo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowId;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.IOException;

/**
 * TextureView 和 SurfaceView
 */
public class LiveCameraActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "LiveCameraActivity";
    //远端的视图
    private SurfaceView remote_sv;
    // 本地的视图
    private SurfaceView local_sv;
    private SurfaceHolder remote_holder;
    private SurfaceHolder local_holder;
    private RelativeLayout remote_rl;
    private RelativeLayout local_rl;

    private int StateAB = 0;
    private int StateBA = 1;
    private int mSate = StateAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera);

        remote_rl = findViewById(R.id.remote_rl);
        remote_sv = findViewById(R.id.remote_view);
        remote_sv.setOnClickListener(this);

        remote_holder = remote_sv.getHolder();
        remote_holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas c = remote_holder.lockCanvas();
                // 2.开画
                Paint p = new Paint();
                p.setColor(Color.RED);
                Rect aa = new Rect(0, 0, holder.getSurfaceFrame().width(),
                        holder.getSurfaceFrame().height());
                c.drawRect(aa, p);
                // 3. 解锁画布 更新提交屏幕显示内容
                remote_holder.unlockCanvasAndPost(c);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                Log.d(TAG, "remote_holder surfaceChanged width: " + width + ",height: " + height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        local_rl = findViewById(R.id.local_rl);
        local_sv = findViewById(R.id.local_view);
        local_sv.setOnClickListener(this);
        local_sv.setZOrderMediaOverlay(true);

//        local_holder = local_sv.getHolder();
//        local_holder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                Canvas c = holder.lockCanvas();
//                // 2.开画
//                Paint p = new Paint();
//                p.setColor(Color.GREEN);
//                Rect aa = new Rect(0, 0, holder.getSurfaceFrame().width(),
//                        holder.getSurfaceFrame().height());
//                c.drawRect(aa, p);
//                // 3. 解锁画布 更新提交屏幕显示内容
//                holder.unlockCanvasAndPost(c);
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format,
//                                       int width, int height) {
//                Log.d(TAG, "local_holder surfaceChanged width: " + width + ",height: " + height);
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
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
    private void switchView(View smallView, SurfaceView smallSurfaceView, View bigView, SurfaceView bigSurfaceView) {
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
        smallSurfaceView.setZOrderMediaOverlay(false);

        ViewGroup.LayoutParams bLayoutParams = bigView.getLayoutParams();//变小像
        bLayoutParams.width = smallWidth;
        bLayoutParams.height = smallHeight;
        bigSurfaceView.setLayoutParams(bLayoutParams);
        bigSurfaceView.setZOrderMediaOverlay(true);

        parentView.removeView(smallView);
        parentView.removeView(bigView);
        parentView.addView(smallView);
        parentView.addView(bigView);
        smallView.requestLayout();
        bigView.requestLayout();
        Log.d(TAG, String.format("after sw: %d, sh: %d, bw: %d, bh: %d", smallView.getWidth(), smallView.getHeight(), bigView.getWidth(), bigView.getHeight()));
    }
}