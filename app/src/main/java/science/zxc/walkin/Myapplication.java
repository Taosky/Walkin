package science.zxc.walkin;

import android.app.Application;
import android.content.Context;

/**
 * AUTH: Taosky
 * TIME: 2017/4/8 0008:下午 1:39.
 * MAIL: t@firefoxcn.net
 * DESC:
 */
public class Myapplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}
