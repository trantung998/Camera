package tungt.demo.camera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 9i-tungt on 6/17/2015.
 */
public class CameraActivity extends Activity{

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mIpgPictureCallback;

    private ImageView btnCapture, btnSwitchCamera, btnCameraSettings;
    private Context mContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;

    private AlertDialog imgaeQualityDialog;
    private int currentQuality = 0;

    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;

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
        refreshCamera();

        DrawingView drawingView = (DrawingView)findViewById(R.id.drawing_surface);
        mPreview.setDrawingView(drawingView);
    }

    @Override
    protected void onDestroy()  {
        Log.d("onDestroy", "");
        super.onDestroy();
    }



    //================== Button Click Listener =====================//
    View.OnClickListener captureOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null,null, mIpgPictureCallback);
        }
    };

    View.OnClickListener switchCameraOnClickListener = new View.OnClickListener() {
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
                Toast.makeText(mContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG).show();
            }
        }
    };

    View.OnClickListener settingCameraOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //cần sửa lại để tự động setup các giá trị này
            final CharSequence[] items = {"SD(640x480)","HD(1280x720)","FHD(1920x1080)","Max(4128*3096)"};
            // Creating and Building the Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Select The Image Quality");
            builder.setSingleChoiceItems(items, currentQuality, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    currentQuality = item;
                    changeImageQuality();
                    imgaeQualityDialog.dismiss();
                }
            });
            imgaeQualityDialog = builder.create();
            imgaeQualityDialog.show();
        }
    };

    void changeImageQuality(){
        Camera.Parameters par = mCamera.getParameters();
//        Camera.Size previewSize = getOptimalSize();
        Camera.Size pictureSize = mPictureSizeList.get(mPictureSizeList.size() - 1 - currentQuality);
        Log.d("Size", "" + pictureSize.width + ", " + pictureSize.height);
//        par.setPreviewSize(previewSize.width, previewSize.height);
        par.setPictureSize(pictureSize.width, pictureSize.height);
        mCamera.setParameters(par);
    }
    private float PREVIEW_SIZE_FACTOR = 1.3f;

    private Camera.Size getOptimalSize() {
        Camera.Size result = null;

        final int width = this.getWindowManager().getDefaultDisplay().getWidth();
        final int height = this.getWindowManager().getDefaultDisplay().getHeight();
        final Camera.Parameters parameters = mCamera.getParameters();
        Log.i(CameraActivity.class.getSimpleName(), "window width: " + width + ", height: " + height);
        for (final Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width * PREVIEW_SIZE_FACTOR && size.height <= height * PREVIEW_SIZE_FACTOR) {
                if (result == null) {
                    result = size;
                } else {
                    final int resultArea = result.width * result.height;
                    final int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        if (result == null) {
            result = parameters.getSupportedPreviewSizes().get(0);
        }
        Log.i(CameraActivity.class.getSimpleName(), "Using PreviewSize: " + result.width + " x " + result.height);
        return result;
    }

    public void chooseCamera() {
        //if the camera preview is the front
        int cameraId;
        if (cameraFront) {
            cameraFront = false;
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            cameraFront = true;
            cameraId = findFrontCamera();
            if(cameraId < 0)// truong hop chac ko xay ra
            {
                cameraFront = false;
                cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
        mPreview.setIsFront(false);
        mCamera = Camera.open(cameraId);
        mIpgPictureCallback = getPictureCallback();
        mPreview.refreshCamera(mCamera);
    }

    /**
     * Picture callback
     */
    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file

                AsyncTask<byte[], Void, Boolean> task = new AsyncTask<byte[], Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(byte[]... params) {
                        Boolean saved = writeToDisk(params[0]); //Your write code
                        return saved;
                    }

                    @Override
                    protected void onPostExecute(Boolean saved) {
                        if (saved) {
                            Toast.makeText(mContext, "Picture saved", Toast.LENGTH_LONG).show();
                            ExifInterface ei = null;
                            try {
                                ei = new ExifInterface(lastImagePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            Log.d("sdasd:", orientation + "");
                            //HANDLE SUCCESS
                        } else {
                            Toast.makeText(mContext,"Error on save", Toast.LENGTH_LONG).show();
                            //HANDLE ERROR
                        }
                    }
                };

                task.execute(data);

                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }
    private String lastImagePath = "";
    private boolean writeToDisk(byte[] data){
        boolean saved = false;

        try {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                lastImagePath = pictureFile.getPath();
                return saved;
            }
            //write the file
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Log.w("data path","" + pictureFile.getAbsolutePath());
            Log.w("data path","" + pictureFile.getPath());
            Log.w("data path","" + pictureFile.getCanonicalPath());
            fos.write(data);
            fos.close();
            saved = true;
        }
        catch (FileNotFoundException e) {
            saved = false;
        }
        catch (IOException e) {
            saved =  false;
        }
        return saved;
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
    private boolean isDeviceHaveFrontCamera = false;
    private int findFrontCamera(){
        if(isDeviceHaveFrontCamera) return Camera.CameraInfo.CAMERA_FACING_FRONT;
        int cameraId = findCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) isDeviceHaveFrontCamera = true;
        return cameraId;
    }

    /**
     * @return CameraId: id of back camera, return -1 if device have no back camera
     */
    private int findBackCamera(){
        return findCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    private int findCamera(int cId){
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == cId) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }



    /**
     * check camera
     * @param context
     * @return Check camera device
     */
    private boolean isCameraCheced = false;
    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            isCameraCheced = true;
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
        Log.d("onPause", "");
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    public void onResume() {
        Log.d("onResume", "");
        super.onResume();
        refreshCamera();
        if(mPreview  != null) Log.d("mPreview:"," alive");
        else Log.d("mPreview:"," death");
    }

    private void refreshCamera(){
        if(!isCameraCheced){
            if (!hasCamera(mContext)) {
                Toast toast = Toast.makeText(mContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        }
        else{
            if (mCamera == null) {
                //if the front facing camera does not exist
                if (findFrontCamera() < 0) {
                    Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                    btnSwitchCamera.setVisibility(View.GONE);
                }

                mCamera = Camera.open(findBackCamera());
                Camera.Parameters parameters =  mCamera.getParameters();
                mPreviewSizeList = parameters.getSupportedPreviewSizes();
                mPictureSizeList = parameters.getSupportedPictureSizes();
                Camera.Size pictureSize = mPictureSizeList.get(0);
                parameters.setJpegQuality(100);
                parameters.setJpegThumbnailQuality(75);
                parameters.setPreviewFpsRange(30, 35);
                parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                parameters.setPictureSize(pictureSize.width, pictureSize.height);

                if (parameters.getMaxNumMeteringAreas() > 0){ // check that metering areas are supported
                    List<Camera.Area> meteringAreas = new ArrayList<>();

                    Rect areaRect1 = new Rect(-100, -100, 100, 100);    // specify an area in center of image
                    meteringAreas.add(new Camera.Area(areaRect1, 600)); // set weight to 60%
                    Rect areaRect2 = new Rect(800, -1000, 1000, -800);  // specify an area in upper right of image
                    meteringAreas.add(new Camera.Area(areaRect2, 400)); // set weight to 40%
                    parameters.setMeteringAreas(meteringAreas);
                }

                mCamera.setParameters(parameters);
                mIpgPictureCallback = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
