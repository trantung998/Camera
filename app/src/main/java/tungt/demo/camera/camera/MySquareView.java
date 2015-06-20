package tungt.demo.camera.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by 9i-tungt on 6/20/2015.
 */
public class MySquareView extends LinearLayout {

    public MySquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MySquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
        if(getMeasuredWidth() > getMeasuredHeight() ) size = getMeasuredHeight();
        else size = getMeasuredWidth();
        setMeasuredDimension(size, size); //Snap to width
    }
}
