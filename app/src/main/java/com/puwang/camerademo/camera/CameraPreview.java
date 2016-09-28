package com.puwang.camerademo.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Leo on 2016/9/27.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private Camera.Parameters parameters;
    private SurfaceHolder mHolder;

    public CameraPreview(Context context, Camera mCamera) {
        super(context);
        this.mCamera = mCamera;

        mHolder = getHolder();
        mHolder.addCallback(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //API 11及以后废弃，需要时自动配置
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();//开启预览

        } catch (IOException e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //对焦关键代码
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                if (b) {
                    initCamera();
                    mCamera.cancelAutoFocus();
                }
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // TODO Auto-generated method stub
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
    }


    //释放相机
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    //重置相机
    public Camera reset(int oritention) {
        releaseCamera();
        mCamera = Camera.open(oritention);
        try {
            //设置Surface用于实时预览。
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //启动捕获和绘图预览画面到屏幕上。
        mCamera.startPreview();

        //设置与预览显示顺时针旋转
        mCamera.setDisplayOrientation(90);
        return mCamera;
    }

    //相机参数的初始化设置
    private void initCamera() {
        parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);

        //开启闪光灯
        //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        //设置闪光灯自动开启


        //连续对焦
        parameters.setFocusMode(Camera.Parameters.
                FOCUS_MODE_CONTINUOUS_PICTURE);
        setDispaly(parameters, mCamera);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        //如果要实现连续的自动对焦，这一句必须加上
        mCamera.cancelAutoFocus();

    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }

    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }
}
