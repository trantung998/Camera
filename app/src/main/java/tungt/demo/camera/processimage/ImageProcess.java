package tungt.demo.camera.processimage;

import android.graphics.Bitmap;

/**
 * Created by 9i-tungt on 6/19/2015.
 */
public class ImageProcess {
    public static Bitmap squareCropv2(Bitmap bm, int size){
        int width = bm.getWidth();
        int height = bm.getHeight();
        int hMid = height / 2;
        int wMid = width / 2;

        int edgeOfsquare = (width < height) ? width : height;
        if(edgeOfsquare > size) edgeOfsquare = size;
        int edgeMid = edgeOfsquare/2;
        int top     = 0;               //top of source image
        int left    = wMid - edgeMid;

        Bitmap bm2 = Bitmap.createBitmap(
                bm,
                left,
                top,
                edgeOfsquare,
                edgeOfsquare);
        bm.recycle();
        return bm2;
    }
}
