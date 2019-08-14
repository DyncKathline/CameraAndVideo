package com.sparksoft.cameraandvideo;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera mCamera;
    private Camera.Parameters parameters;
    private int frameWidth = 1280;
    private int frameHeight = 720;
    private int mCamerId;
    private String flashMode = Camera.Parameters.FLASH_MODE_OFF;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        Config.d(String.format("init: w = %d, h = %d", frameWidth, frameHeight));
        initCamera();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Config.d(String.format("onFinishInflate: w = %d, h = %d", frameWidth, frameHeight));
        initCamera();
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mCamera != null && mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            /**
             * 切换前置摄像头
             */
            //停止摄像机释放资源
            stopCamera();
            mCamerId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            Config.d(String.format("CAMERA_FACING_FRONT: w = %d, h = %d", frameWidth, frameHeight));
            initCamera();
        } else if (mCamera != null && mCamerId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            /**
             * 切换后置摄像头
             */
            //停止摄像机释放资源
            stopCamera();
            mCamerId = Camera.CameraInfo.CAMERA_FACING_BACK;
            Config.d(String.format("CAMERA_FACING_BACK: w = %d, h = %d", frameWidth, frameHeight));
            initCamera();
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        try {
            mCamera = Camera.open(mCamerId);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(this);
            Camera.Size optionSize = getOptimalPreviewSize(frameWidth, frameHeight);
            Config.d(String.format("initCamera: w = %d, h = %d", optionSize.width, optionSize.height));
            //得到摄像头的参数
            parameters = mCamera.getParameters();
//            parameters.setJpegQuality(80);//设置照片的质量
            parameters.setPreviewSize(optionSize.width, optionSize.height);//设置预览尺寸
            parameters.setPictureSize(optionSize.width, optionSize.height);//设置照片分辨率
            // 部分摄像头可能不支持自动对焦
//            List<String> focusModes = parameters.getSupportedFocusModes();
//            for (String mode : focusModes) {
//                if (mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
//                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//                    break;
//                }
//            }
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setFlashMode(flashMode);
            //Camera.Parameters.FOCUS_MODE_AUTO; //自动聚焦模式
            //Camera.Parameters.FOCUS_MODE_INFINITY;//无穷远
            //Camera.Parameters.FOCUS_MODE_MACRO;//微距
            //Camera.Parameters.FOCUS_MODE_FIXED;//固定焦距
            parameters.setRotation(90);  //生成的图像旋转90度
            mCamera.setParameters(parameters);
            setCameraDisplayOrientation(0, mCamera);

            SurfaceHolder mSurfaceHolder = getHolder();
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } else {
            Toast.makeText(getContext(), "摄像头打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过传入的宽高  计算出最接近相机支持的宽高
     *
     * @param w
     * @param h
     * @return 返回一个Camera.Size类型 通过setPreviewSize设置给相机
     */
    public Camera.Size getOptimalPreviewSize(int w, int h) {

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            Log.d("Main", "width = " + size.width + "  height = " + size.height);
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * 设置 摄像头的角度
     *
     * @param cameraId 摄像头ID（假如手机有N个摄像头，cameraId 的值 就是 0 ~ N-1）
     * @param camera   摄像头对象
     */
    public void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        //获取摄像头信息
        Camera.getCameraInfo(cameraId, info);
        int rotation = getActivityFromContext(getContext())
                .getWindowManager()
                .getDefaultDisplay()
                .getRotation();
        //获取摄像头当前的角度
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 前置摄像头
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            // 后置摄像头
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private Activity getActivityFromContext(Context context) {
        if (null != context) {
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        return null;
    }

    /**
     * 停止销毁摄像机
     */
    private void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            //停止当前摄像头预览
            mCamera.stopPreview();
            //释放摄像机资源
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Config.d("CameraSurface setVisibility ： " + visibility);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Config.d("CamereSurface  created.");
        Config.d(String.format("surfaceCreated: w = %d, h = %d", frameWidth, frameHeight));
        initCamera();
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Config.d("CameraSurface onAttachedToWindow");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Config.d("CameraSurface  surfaceChanged ： " + width + " " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Config.d("CameraSurface  surfaceDestroyed：");
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Config.d("CameraSurface  onPreviewFrame ： ");
    }

}
