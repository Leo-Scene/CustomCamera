package com.puwang.camerademo.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.puwang.camerademo.MyApplication;
import com.puwang.camerademo.R;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends Activity {
    Context context;
    private boolean isCamera;
    private CameraPreview mCameraPreview;

    Camera mCamera;
    private Camera.Parameters mParameters;
    private float pointX, pointY;
    private int mode;                      //0是聚焦 1是放大
    static final int FOCUS = 1;            // 聚焦
    static final int ZOOM = 2;            // 缩放
    private float dist;
    private ImageView mSwitchOritention;
    private ImageView mTakePhoto;
    private int cameraPosition = 0;    //0代表后置,1代表前置
    private FrameLayout mFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        context = CameraActivity.this;
        check();
        initView();
        initCamera();
        setOnClick();


    }

    private void initView() {
        mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);

        mSwitchOritention = (ImageView) findViewById(R.id.switchOritention);
        mTakePhoto = (ImageView) findViewById(R.id.take_photo);
    }

    private void setOnClick() {


        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                autoFocus();
                //判断是否支持对焦
                boolean b = getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_AUTOFOCUS);

                if (b){
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        // 主点按下
                        case MotionEvent.ACTION_DOWN:
                            pointX = event.getX();
                            pointY = event.getY();
                            mode = FOCUS;
                            break;
                        // 副点按下
                        case MotionEvent.ACTION_POINTER_DOWN:

                            dist = spacing(event);
                            // 如果连续两点距离大于10，则判定为多点模式
                            if (spacing(event) > 10f) {
                                mode = ZOOM;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            mode = FOCUS;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mode == FOCUS) {
                                // pointFocus((int) event.getRawX(), (int) event.getRawY());
                            } else if (mode == ZOOM) {
                                float newDist = spacing(event);
                                if (newDist > 10f) {
                                    float tScale = (newDist - dist) / dist;
                                    if (tScale < 0) {
                                        tScale = tScale * 16;
                                    }
                                    addZoomIn((int) tScale);
                                }
                            }
                            break;
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"不支持缩放",Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });


        mCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //autoFocus();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        mCamera.cancelAutoFocus();
                    }
                });
                try {
                    boolean b = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
                    if (b){
                        pointFocus((int) pointX, (int) pointY);
                    }else {
                        Toast.makeText(getApplicationContext(),"不支持缩放",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mSwitchOritention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //转换摄像头
                mCameraPreview.releaseCamera();
                if (cameraPosition == 0) {
                    cameraPosition = 1;
                } else {
                    cameraPosition = 0;
                }
                mCamera = mCameraPreview.reset(cameraPosition);
            }
        });

        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"点击了照相。",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCamera() {
        //默认后置摄像头
        mCamera = getCameraInstance(0);
        mCameraPreview = new CameraPreview(context, mCamera);

        //设置预览方向
        mCamera.setDisplayOrientation(90);

        mFrameLayout.addView(mCameraPreview);
    }

    private void check() {
        isCamera = checkCameraHardware(context);

        if (!isCamera) {
            Toast.makeText(context, "没有摄像头", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(int i) {
        Camera c = null;
        try {
            //默认的是打开的后置摄像头，当选择为
            c = Camera.open(i); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    //放大缩小
    int curZoomValue = 0;

    private void addZoomIn(int delta) {

        try {
            Camera.Parameters params = mCamera.getParameters();
            android.util.Log.d("Camera", "Is support Zoom " + params.isZoomSupported());
            if (!params.isZoomSupported()) {
                return;
            }
            curZoomValue += delta;
            if (curZoomValue < 0) {
                curZoomValue = 0;
            } else if (curZoomValue > params.getMaxZoom()) {
                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {
                params.setZoom(curZoomValue);
                mCamera.setParameters(params);
                return;
            } else {
                mCamera.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定点对焦的代码
    private void pointFocus(int x, int y) {
        mCamera.cancelAutoFocus();
        mParameters = mCamera.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showPoint(x, y);
        }
        mCamera.setParameters(mParameters);
        autoFocus();
    }

    private void showPoint(int x, int y) {
        if (mParameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            //xy变换了
            int rectY = -x * 2000 / MyApplication.getApp().getScreenWidth() + 1000;
            int rectX = y * 2000 / MyApplication.getApp().getScreenHeight() - 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;
            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));
            mParameters.setMeteringAreas(areas);
        }

        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    //实现自动对焦
    private void autoFocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCamera == null) {
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.cancelAutoFocus();
                        }
                    }
                });
            }
        };
    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
