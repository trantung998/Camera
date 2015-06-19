package tungt.demo.camera;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    Button btn_Opencamera;
    Button btn_OpenGallery;
    Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout);
        mActivity = this;
        btn_Opencamera = (Button)findViewById(R.id.btn_menu_open_camera);
        btn_Opencamera.setOnClickListener(openCameraOnClick);
        btn_OpenGallery = (Button)findViewById(R.id.btn_menu_select);
        btn_OpenGallery.setOnClickListener(openGalleryOnClick);

    }

    View.OnClickListener openCameraOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(mActivity, CameraActivity.class);
            startActivity(i);

        }
    };

    View.OnClickListener openGalleryOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);

            startActivityForResult(intent, 2);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            Uri selectedImage = data.getData();
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            Log.w("Path file from gallery:", picturePath + "");

            Intent in = new Intent(this, ImageReviewActivity.class);
            in.putExtra("imagePath", picturePath);
            //startActivity(in);
            try {

                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                // indicate image type and Uri
                cropIntent.setDataAndType(selectedImage, "image/*");
                // set crop properties
                cropIntent.putExtra("crop", "true");
                // indicate aspect of desired crop
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
                cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                // indicate output X and Y
//                cropIntent.putExtra("outputX", 512);
//                cropIntent.putExtra("outputY", 512);
                // retrieve data on return
                cropIntent.putExtra("return-data", true);
                // start the activity - we handle returning in onActivityResult
                startActivityForResult(cropIntent, 1);
            }
            // respond to users whose devices do not support the crop action
            catch (ActivityNotFoundException anfe) {
                // display an error message
                String errorMessage = "Whoops - your device doesn't support the crop action!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if (requestCode == 1) {
            if (data != null) {
//                // get the returned data
//                Bundle extras = data.getExtras();
//                // get the cropped bitmap
//                Bitmap selectedBitmap = extras.getParcelable("data");
                ImageView imgView = (ImageView)findViewById(R.id.image_view_menu);
//                imgView.setImageBitmap(selectedBitmap);

                File tempFile = getTempFile();

                String filePath= Environment.getExternalStorageDirectory()
                        +"/"+TEMP_PHOTO_FILE;
                Log.d("path ",filePath);


                Bitmap selectedImage =  BitmapFactory.decodeFile(filePath);
                imgView.setImageBitmap(selectedImage );

                if (tempFile.exists()) tempFile.delete();
            }
        }
    }

    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File file = new File(Environment.getExternalStorageDirectory(),TEMP_PHOTO_FILE);
            try {
                file.createNewFile();
            } catch (IOException e) {}

            return file;
        } else {

            return null;
        }
    }

    private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";
}