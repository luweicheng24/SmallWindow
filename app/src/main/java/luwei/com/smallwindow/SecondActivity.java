package luwei.com.smallwindow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Author   : luweicheng on 2018/8/23 18:00
 * E-mail   ：1769005961@qq.com
 * GitHub   : https://github.com/luweicheng24
 * function :
 **/

public class SecondActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }


    public void dismissWindow(View view) {
        dismissWindow();
    }
}
