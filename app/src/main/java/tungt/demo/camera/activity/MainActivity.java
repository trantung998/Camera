package tungt.demo.camera.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import tungt.demo.camera.R;
import tungt.demo.camera.activity.CameraActivity;
import tungt.demo.camera.activity.ImageReviewActivity;


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
        ImageView imgView = (ImageView)findViewById(R.id.image_view_menu);
        Bitmap bm = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        Bitmap bm2 = Bitmap.createBitmap(bm,bm.getWidth()/2,bm.getHeight()/2,bm.getWidth()/2,bm.getHeight()/2);
        imgView.setImageBitmap(bm2);
//        imgView.setImageBitmap(selectedImage);

    }

    public Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
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

            startActivityForResult(intent, OPEN_GALLERY);
        }
    };

    private  final int OPEN_CROP_IMAGE = 1;
    private final int OPEN_GALLERY = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_GALLERY) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Log.w("Path file from gallery:", picturePath + "");
                try {
                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                    // indicate image type and Uri
                    cropIntent.setDataAndType(selectedImage, "image/*");
                    // set crop properties
                    cropIntent.putExtra("crop", "true");
                    // indicate aspect of desired crop
                    cropIntent.putExtra("aspectX", 2);
                    cropIntent.putExtra("aspectY", 2);
                    cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
                    cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    // indicate output X and Y
//                cropIntent.putExtra("outputX", 512);
//                cropIntent.putExtra("outputY", 512);
                    // retrieve data on return
                    cropIntent.putExtra("return-data", true);
                    // start the activity - we handle returning in onActivityResult
                    startActivityForResult(cropIntent, OPEN_CROP_IMAGE);
                }
                // respond to users whose devices do not support the crop action
                catch (ActivityNotFoundException anfe) {
                    // display an error message
                    String errorMessage = "Whoops - your device doesn't support the crop action!";
                    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
        if (requestCode == OPEN_CROP_IMAGE) {
            if (data != null) {
                ImageView imgView = (ImageView)findViewById(R.id.image_view_menu);
                File tempFile = getTempFile();
                String filePath= Environment.getExternalStorageDirectory()
                        +"/"+TEMP_PHOTO_FILE;
                Log.d("path ",filePath);
//                Bitmap selectedImage =  BitmapFactory.decodeFile(filePath);
//                imgView.setImageBitmap(selectedImage);
                Intent in = new Intent(this, ImageReviewActivity.class);
                in.putExtra("imagePath", filePath);
                startActivity(in);
//                if (tempFile.exists()) tempFile.delete();
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