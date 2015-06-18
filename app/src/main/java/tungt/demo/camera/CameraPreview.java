package tungt.demo.camera;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by user on 6/14/2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Activity mActivity;
    private Camera.Size mPreviewSize;

    private boolean isFront = false;

    public CameraPreview(Camera mCamera, Activity mActivity) {
        super(mActivity);
        this.mCamera = mCamera;
        this.mActivity = mActivity;
        initHolder();

    }

    void initHolder(){
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("surfaceCreated","");
        Log.d("mCamera", "" + mCamera);
        Log.d("holder", "" + holder);

        try {
        if(mCamera != null){

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }
        }catch (IOException e){
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(mCamera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("surfaceDestroyed", "");
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        mCamera= camera;
        configureCameraParameters();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    protected void configureCameraParameters() {
        int angle;
        int degrees = 0;
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Log.d("rotation" , rotation+ "");
        switch (rotation) {
            case Surface.ROTATION_0: // This is display orientation
                angle = 90; // This is camera orientation
                degrees = 270;
                break;
            case Surface.ROTATION_90:
                angle = 0;
                degrees = 180;
                break;
            case Surface.ROTATION_180:
                angle = 270;
                degrees = 90;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                degrees = 0;
                break;
            default:
                angle = 90;
                break;
        }

        Camera.Parameters params = mCamera.getParameters();
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraside = (isFront)?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_BACK;
        Camera.getCameraInfo(cameraside, info);
        Log.d("info.orientation", info.orientation + "");
        Log.d("info.facing", info.facing + "");
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360; // compensate the mirror
            params.setRotation(degrees);
        }
        else
        { // back-facing
//            result = (info.orientation - degrees + 360) % 360;
            params.setRotation(angle);
        }
        mCamera.setDisplayOrientation(angle);
        mCamera.setParameters(params);
    }

    public boolean isFront() {
        return isFront;
    }

    public void setIsFront(boolean isFront) {
        this.isFront = isFront;
    }
//===================//
    private boolean listenerSet = false;
    public Paint paint;
    private DrawingView drawingView;
    private boolean drawingViewSet = false;

//    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){
//
//        @Override
//        public void onAutoFocus(boolean arg0, Camera arg1) {
//            if (arg0){
//                mCamera.cancelAutoFocus();
//            }
//        }
//    };

//
//    /**
//     * Called from PreviewSurfaceView to set touch focus.
//     * @param - Rect - new area for auto focus
//     */
//    public void doTouchFocus(final Rect tfocusRect) {
//        try {
//            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
//            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
//            focusList.add(focusArea);
//
//            Camera.Parameters param = mCamera.getParameters();
//            param.setFocusAreas(focusList);
//            param.setMeteringAreas(focusList);
//            mCamera.setParameters(param);
//
//            mCamera.autoFocus(myAutoFocusCallback);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i("Camera", "Unable to autofocus");
//        }
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("Touch","onTouchEvent");
//        if (!listenerSet) {
//            return false;
//        }
//        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            float x = event.getX();
//            float y = event.getY();
//
//            Rect touchRect = new Rect(
//                    (int)(x - 100),
//                    (int)(y - 100),
//                    (int)(x + 100),
//                    (int)(y + 100));
//
//            final Rect targetFocusRect = new Rect(
//                    touchRect.left * 2000/this.getWidth() - 1000,
//                    touchRect.top * 2000/this.getHeight() - 1000,
//                    touchRect.right * 2000/this.getWidth() - 1000,
//                    touchRect.bottom * 2000/this.getHeight() - 1000);
//
//            doTouchFocus(targetFocusRect);
//            if (drawingViewSet) {
//                drawingView.setHaveTouch(true, touchRect);
//                drawingView.invalidate();
//
//                // Remove the square after some time
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
//                        drawingView.invalidate();
//                    }
//                }, 1000);
//            }
//
//        }
//        return false;
//    }

    public void setDrawingView(DrawingView dView) {
        drawingView = dView;
        drawingViewSet = true;
    }

}
