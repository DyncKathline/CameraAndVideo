package com.sparksoft.cameraandvideo;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phil on 2017/9/13.
 */

public class CameraTextureView extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private Camera mCamera;
    private Camera.Parameters parameters;
    private int frameWidth = 1280;
    private int frameHeight = 720;
    private int mCamerId;
    private String flashMode = Camera.Parameters.FLASH_MODE_OFF;

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
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
            initCamera();
        } else if (mCamera != null && mCamerId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            /**
             * 切换后置摄像头
             */
            //停止摄像机释放资源
            stopCamera();
            mCamerId = Camera.CameraInfo.CAMERA_FACING_BACK;
            initCamera();
        }
        if(mCamera != null) {
            try {
                mCamera.setPreviewTexture(getSurfaceTexture());
                mCamera.startPreview();
            } catch (Exception e) {
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


            setSurfaceTextureListener(this);
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
            Log.i("Main", "width: " + size.width + "  height：" + size.height);
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

    public void take() {
        if (mCamera != null)
            mCamera.takePicture(null, null, mPictureCallback);
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mCamera.stopPreview();
            new FileSaver(data).save();
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        frameWidth = width;
        frameHeight = height;
        initCamera();
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(frameWidth, frameHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(frameWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, frameHeight);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Config.d("CameraTexture  onPreviewFrame ： ");
    }

    private class FileSaver implements Runnable {
        private byte[] buffer;

        public FileSaver(byte[] buffer) {
            this.buffer = buffer;
        }

        public void save() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "zhangphil.png");
                file.createNewFile();

                FileOutputStream os = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(os);

                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

                bos.flush();
                bos.close();
                os.close();

                Log.d("照片已保存", file.getAbsolutePath());

                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}