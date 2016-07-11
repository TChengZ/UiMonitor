package uimonitor.jc.com;

import android.app.Application;

/**
 * Created by Administrator on 2016/7/11.
 */
public class BaseApplication extends Application{

    private static BaseApplication mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static BaseApplication getInstance() {
        return mInstance;
    }
}
