package tungt.demo.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private ImageView btnCapture, btnSwitchCamera, btnCameraSettings;
    private Context mContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
    }

    //====================init=====================//

    private void init(){
        mContext = this;
        cameraPreview =(LinearLayout)findViewById(R.id.camera_preview);
        mPreview =  new CameraPreview(mCamera, this);
        cameraPreview.addView(mPreview);

        btnCapture = (ImageView)findViewById(R.id.button_capture);
        btnCapture.setOnClickListener(captureOnClickListener);

        btnSwitchCamera = (ImageView)findViewById(R.id.button_switchCamera);
        btnSwitchCamera.setOnClickListener(switchCameraOnClickListener);

        btnCameraSettings = (ImageView)findViewById(R.id.button_settings);
        btnCameraSettings.setOnClickListener(settingCameraOnClickListener);
    }

    //================== Button Click Listener =====================//
    OnClickListener captureOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null,null,mPicture);
        }
    };

    OnClickListener switchCameraOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(mContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    OnClickListener settingCameraOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    /**
     * Picture callback
     */
    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
                File pictureFile = getOutputMediaFile();

                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Toast toast = Toast.makeText(mContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
                    toast.show();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }

                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }

    /**
     * create directory fo the picture
     */
    private static File getOutputMediaFile() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "JCG Camera");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        //and make a media file:
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    /**
     * @return CameraId: id of front camera, return -1 if device have no front camera
     */
    private int findFrontCamera(){
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    /**
     * @return CameraId: id of back camera, return -1 if device have no back camera
     */
    private int findBackCamera(){
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    /**
     * check camera
     * @param context
     * @return
     */
    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    //=============== onPause, onResume, onDestroy ======================//
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;

    public void onResume() {
        super.onResume();
        if (!hasCamera(mContext)) {
            Toast toast = Toast.makeText(mContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            if (findFrontCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                btnSwitchCamera.setVisibility(View.GONE);
            }

            mCamera = Camera.open(findBackCamera());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            Camera.Parameters cameraParams = mCamera.getParameters();
            mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
            mPictureSizeList = cameraParams.getSupportedPictureSizes();

            for (int i = 0; i < mPictureSizeList.size(); i++) {
                Log.d("picture size support", "width: " + mPictureSizeList.get(i).width + " height: "+ mPictureSizeList.get(i).height);
            }

            for (int i = 0; i < mPictureSizeList.size(); i++) {
                Log.d("mPreviewSizeList", "width: " + mPreviewSizeList.get(i).width + " height: "+ mPreviewSizeList.get(i).height);
            }

        }
    }
}
