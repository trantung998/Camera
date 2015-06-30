package tungt.demo.camera.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

/**
 * Created by 9i-tungt on 6/18/2015.
 */
public class DrawingView extends SurfaceView {
    private boolean haveTouch = false;
    private Rect touchArea;
    private Paint paint;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(0xeed7d7d7);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        haveTouch = false;
    }

    public void setHaveTouch(boolean val, Rect rect) {
        haveTouch = val;
        touchArea = rect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(haveTouch){
            Log.d("onDraw","SSAd");
            canvas.drawRect(
                    touchArea.left, touchArea.top, touchArea.right, touchArea.bottom,
                    paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("Touch", "onTouchEvent");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (getHolder().getSurface().isValid()) {
                Canvas canvas = getHolder().lockCanvas();
                canvas.drawColor(Color.BLACK);
                canvas.drawCircle(event.getX(), event.getY(), 50, paint);
                getHolder().unlockCanvasAndPost(canvas);
            }
            return true;
        }
        return false;
    }
}
