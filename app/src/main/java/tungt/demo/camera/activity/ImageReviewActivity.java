package tungt.demo.camera.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;

import tungt.demo.camera.R;
import tungt.demo.camera.processimage.ImageProcess;


public class ImageReviewActivity extends ActionBarActivity {
    ImageView mImageReview;
    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_review);
        mImageReview = (ImageView)findViewById(R.id.imageview);
        imagePath = getIntent().getExtras().getString("imagePath");
        Bitmap thumbnail = fixImageExif(imagePath);
//        mImageReview.setImageBitmap(thumbnail);

        AsyncTask<Bitmap,Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Bitmap... params) {
                return crop(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if(result != null){
                    mImageReview.setImageBitmap(result);
                }
            }
        };
        task.execute(thumbnail);
    }

    public Bitmap crop(Bitmap _bm)
    {
        return ImageProcess.squareCropv2(_bm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_review, menu);
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

    public Bitmap fixImageExif(String filePath){
        Bitmap myBitmap = BitmapFactory.decodeFile(filePath);
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
        }
        return myBitmap;
    }
}
