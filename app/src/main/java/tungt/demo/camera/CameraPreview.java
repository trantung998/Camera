package tungt.demo.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

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
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

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

    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;

    protected void configureCameraParameters() {
        int angle;
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                angle = 90; // This is camera orientation
                break;
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = 270;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                angle = 90;
                break;
        }
        mCamera.setDisplayOrientation(angle);

        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraside = (isFront)?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_BACK;
        Camera.getCameraInfo(cameraside, info);
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Log.d("rotation", ": " + rotation);
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees   = 90; break; //Natural orientation
            case Surface.ROTATION_90: degrees  = 0; break; //Landscape left
            case Surface.ROTATION_180: degrees = 270; break;//Upside down
            case Surface.ROTATION_270: degrees = 180; break;//Landscape right
        }
        int rotate = (info.orientation - degrees + 360) % 360;
        Camera.Parameters params = mCamera.getParameters();
//        params.setRotation(degrees);
//        params.setPictureSize(300,300);
//        params.setPreviewSize(300,300);
        mCamera.setParameters(params);
    //test
//        Camera.Parameters cameraParams = mCamera.getParameters();
//        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
//        mPictureSizeList = cameraParams.getSupportedPictureSizes();

//        for (int i = 0; i < mPictureSizeList.size(); i++) {
//            Log.d("picture size support", "width: " + mPictureSizeList.get(i).width + " height: "+ mPictureSizeList.get(i).height);
//        }
//
//        for (int i = 0; i < mPictureSizeList.size(); i++) {
//            Log.d("mPreviewSizeList", "width: " + mPreviewSizeList.get(i).width + " height: "+ mPreviewSizeList.get(i).height);
//        }
    }

    public boolean isFront() {
        return isFront;
    }

    public void setIsFront(boolean isFront) {
        this.isFront = isFront;
    }
}
