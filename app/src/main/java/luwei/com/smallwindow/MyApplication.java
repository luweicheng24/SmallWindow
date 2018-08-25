package luwei.com.smallwindow;

import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Author   : luweicheng on 2018/8/25 12:06
 * E-mail   ：1769005961@qq.com
 * GitHub   : https://github.com/luweicheng24
 * function:
 **/

public class MyApplication extends Application {

    private SmallWindowView windowView;
    private WindowManager wm;
    private WindowManager.LayoutParams mLayoutParams;
    public SmallWindowView getWindowView() {
        return windowView;
    }
    public WindowManager getWm() {
        return wm;
    }
    public WindowManager.LayoutParams getmLayoutParams() {
        return mLayoutParams;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        initSmallViewLayout();
    }
    public void initSmallViewLayout() {
        windowView = (SmallWindowView) LayoutInflater.from(this).inflate(R.layout.small_window, null);
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.NO_GRAVITY;
        //使用非CENTER时，可以通过设置XY的值来改变View的位置
        windowView.setWm(wm);
        windowView.setWmParams(mLayoutParams);
    }
}
