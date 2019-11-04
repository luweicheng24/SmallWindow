# SmallWindow
> 需求描述 

类似微信视频、语音时点击返回会形成一个App小窗口浮动在界面上，点击继续是通通话，如下图：
![微信小窗口](https://upload-images.jianshu.io/upload_images/4082354-221defcd168eb660.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 效果展示

![效果演示图](https://upload-images.jianshu.io/upload_images/4082354-9ed9532f1b4504d5.gif?imageMogr2/auto-orient/strip)
> 技术分析 

其实实现这个功能只需要你细心分析一下就有思路了：首先这个小窗口是浮动在app最上层的视图，其次所有触屏事件需先由该小窗口处理，还有就是小窗口的生命周期和Application也能虽可能不能同生，但是确是可以共死。所以可以在Application中创建一个view添加到WindowManage，这里将视图为view的window的type设置成系统级别的窗口，这样这个window可以在在全局呈现。另外，还需要让这个window可以随手指拖动而滑动，手指释放后会回弹到距离这个释放点最近的屏幕侧边，所以需要重写view 的OnTouch事件。

> 代码细节实现

 -  创建全局Application，在Application创建的时候初始化一个view，以及一个WindowManager.LayoutParams,并设置get方法，方便外部调用:
 

```
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
```
-  编写一个BaseActivity 实现可供子类来显示隐藏窗口的方法：

```
  private WindowManager wm;
    private SmallWindowView windowView;
    private WindowManager.LayoutParams mLayoutParams;
    private int OVERLAY_PERMISSION_REQ_CODE = 2;
    private boolean isRange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wm = ((MyApplication)getApplication()).getWm();
        windowView = ((MyApplication)getApplication()).getWindowView();
        mLayoutParams = ((MyApplication)getApplication()).getmLayoutParams();
    }


    public void alertWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 7.0 以上需要引导用去设置开启窗口浮动权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0 以上type需要设置成这个
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            requestDrawOverLays();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 6.0 动态申请
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (wm != null && windowView.getWm() == null) {
                wm.addView(windowView, mLayoutParams);
            }
        } else {
            Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show();
        }
    }


    private int[] location = new int[2]; // 小窗口位置坐标

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isRange = calcPointRange(event);
        }
        if (isRange) {
            windowView.dispatchTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    /**
     *  计算当前点击事件坐标是否在小窗口内
     * @param event
     * @return
     */
    private boolean calcPointRange(MotionEvent event) {
        windowView.getLocationOnScreen(location);
        int width = windowView.getMeasuredWidth();
        int height = windowView.getMeasuredHeight();
        float curX = event.getRawX();
        float curY = event.getRawY();
        if (curX >= location[0] && curX <= location[0] + width && curY >= location[1] && curY <= location[1] + height) {
            return true;
        }
        return false;
    }

    private static final String TAG = "BaseActivity";

    // android 23 以上先引导用户开启这个权限 该权限动态申请不了
    @TargetApi(Build.VERSION_CODES.M)
    public void requestDrawOverLays() {
        if (!Settings.canDrawOverlays(BaseActivity.this)) {
            Toast.makeText(this, "can not DrawOverlays", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + BaseActivity.this.getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            if (wm != null && windowView.getWindowId() == null) {
                wm.addView(windowView, mLayoutParams);
            }
            Toast.makeText(this, "权限已经授予", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "设置权限拒绝", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "设置权限成功", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 移除window
    public void dismissWindow() {
        if (wm != null && windowView != null && windowView.getWindowId() != null) {
            wm.removeView(windowView);
        }
    }
```
-  自定义一个处理Window内滑动事件的ViewGroup

```
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
        this.wmParams.x = screenWidth; // 窗口先贴附在右边
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
        x = event.getRawX(); // 触摸点相对屏幕的x坐标
        y = event.getRawY() - statusHeight; // 触摸点相对于屏幕的y坐标
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (wmParams.x > 0) {
                    isRight = true;
                }
                if (wmParams.x < 0) {
                    isRight = false;
                }
                mTouchStartX = event.getX();// 触摸点在View内的相对x坐标
                mTouchStartY = event.getY();// 触摸点在View内的相对Y坐标
                Log.i("startP", "startX" + mTouchStartX + "====startY" + mTouchStartY);
                break;

            case MotionEvent.ACTION_MOVE:
                updateViewPosition(); //  跟新window布局参数
                break;
            case MotionEvent.ACTION_UP:
                if (wmParams.x <= 0) {  //窗口贴附在左边
                    wmParams.x = Math.abs(wmParams.x) <= screenWidth / 2 ? -screenWidth : screenWidth;
                } else {  // 窗口贴附在右边
                    wmParams.x = wmParams.x <= screenWidth / 2 ? screenWidth : -screenWidth;
                }

                // wmParams.x = screenWidth;
                wmParams.y = (int) (y - screenHeight / 2);// 跟新y坐标
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

```
以上就能实现一个应用内小窗口了，这里windowManager的布局参数有坑要踩：

 - 不设置Gravity属性，window的坐标是以屏幕左上角为(0,0)原点，而当第一次接受到触摸事件之后就会以默认原点更改为屏幕中心，[Github源码，给个小星星](https://github.com/luweicheng24/SmallWindow)
