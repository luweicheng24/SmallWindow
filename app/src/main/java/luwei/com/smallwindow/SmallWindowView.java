package luwei.com.smallwindow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Author   : luweicheng on 2018/8/24 11:22
 * E-mail   ：1769005961@qq.com
 * GitHub   : https://github.com/luweicheng24
 * function:
 **/

public class SmallWindowView extends LinearLayout {
    private final int screenHeight;
    private final int screenWidth;
    private int statusHeight;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;

    private WindowManager wm;
    public WindowManager.LayoutParams wmParams;


    public SmallWindowView(Context context) {
        this(context, null);
    }

    public WindowManager getWm() {
        return wm;
    }

    public void setWm(WindowManager wm) {
        this.wm = wm;
    }

    public WindowManager.LayoutParams getWmParams() {
        return wmParams;
    }

    public void setWmParams(WindowManager.LayoutParams wmParams) {
        this.wmParams = wmParams;
        this.wmParams.x = screenWidth;
    }

    public SmallWindowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmallWindowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        statusHeight = getStatusHeight(context);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

    }


    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    boolean isRight = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY() - statusHeight;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (wmParams.x > 0) {
                    isRight = true;
                }
                if (wmParams.x < 0) {
                    isRight = false;
                }
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                Log.i("startP", "startX" + mTouchStartX + "====startY" + mTouchStartY);
                break;

            case MotionEvent.ACTION_MOVE:
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:

                if (wmParams.x <= 0) {
                    wmParams.x = Math.abs(wmParams.x) <= screenWidth / 2 ? -screenWidth : screenWidth;
                } else {
                    wmParams.x = wmParams.x <= screenWidth / 2 ? screenWidth : -screenWidth;
                }

                // wmParams.x = screenWidth;
                wmParams.y = (int) (y - screenHeight / 2);
                wm.updateViewLayout(this, wmParams);
                break;
        }
        return true;
    }
    private void updateViewPosition() {
        wmParams.gravity = Gravity.NO_GRAVITY;
        //更新浮动窗口位置参数
        int dx = (int) (mTouchStartX - x);
        int dy = (int) (y-screenHeight / 2);
        if (isRight) {
            wmParams.x = screenWidth / 2 - dx;
        } else {
            wmParams.x = -dx - screenWidth / 2;
        }
        wmParams.y = dy;
        Log.i("winParams", "x : " + wmParams.x + "y :" + wmParams.y + "  dy :" + dy);
        wm.updateViewLayout(this, wmParams);
        //刷新显示
    }
}
