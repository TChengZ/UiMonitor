package uimonitor.jc.com.flow;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Printer;
import android.widget.Toast;

import uimonitor.jc.com.BaseApplication;
import uimonitor.jc.com.utils.Constant;
import uimonitor.jc.com.utils.FileUtil;
import uimonitor.jc.com.utils.TimeUtil;

/**
 * Ui卡顿检测业务类
 */
@SuppressWarnings("HardCodedStringLiteral")
public class UiMonitor implements Printer {

    private boolean mIsUiMonitoring = false; //是否在进行UI检测
    private int mMonitorTestTime = DEFAULT_MONITOR_TEST_TIME; //UI操作超时时间，默认200毫秒
    public final static int DEFAULT_MONITOR_TEST_TIME = 200;
    public final static int MAX_MONITOR_TEST_TIME = 1000; //最大检测时间

    private boolean isStartLogPrint = false;
    private String mUIDelayContent = null;

    private volatile Handler mMonitorHandler;
    private HandlerThread mMonitorHandlerThread;

    private static UiMonitor mInstance = null;


    private Runnable mUiMonitorRunnable = new Runnable() {
        @Override
        public void run() {
            StackTraceElement[] arrTrace = Looper.getMainLooper().getThread().getStackTrace();
            if(isLoopQueueTime(arrTrace)){
                return;
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0;i < arrTrace.length; i++) {
                StackTraceElement ele = arrTrace[i];
                sb.append(ele.getClassName() + "." + ele.getMethodName() + "(" + ele.getLineNumber() + ")\n");
            }
            final String uiDelay = sb.toString();
            Runnable writeRunnable = new Runnable() {
                @Override
                public void run() {
                    writeUiDelayLog(uiDelay, getAvailMemory());
                }
            };
            Thread thread = new Thread(writeRunnable);
            thread.start();
            Toast.makeText(BaseApplication.getInstance(), "UI响应缓慢，请查看日志", Toast.LENGTH_SHORT).show();
        }
    };

    public static UiMonitor getInstance(){
        if(null == mInstance){
            synchronized (UiMonitor.class){
                if(null == mInstance){
                    mInstance = new UiMonitor();
                }
            }
        }
        return mInstance;
    }

    private UiMonitor(){
        if (mMonitorHandler == null) {
            mMonitorHandlerThread = new HandlerThread("uiMonitor");
            mMonitorHandlerThread.start();

            mMonitorHandler = new Handler(mMonitorHandlerThread.getLooper());
        }
    }

    /**
     * UI检测超时是因为没有消息处理等待时间长，这种情况应该过滤掉
     * @param arrTrace
     * @return
     */
    private boolean isLoopQueueTime(StackTraceElement[] arrTrace){
        return null != arrTrace && arrTrace.length >= 3
                && "android.os.MessageQueue".equals(arrTrace[0].getClassName()) && "nativePollOnce".equals(arrTrace[0].getMethodName())
                && "android.os.MessageQueue".equals(arrTrace[1].getClassName()) && "next".equals(arrTrace[1].getMethodName())
                && "android.os.Looper".equals(arrTrace[2].getClassName()) && "loop".equals(arrTrace[2].getMethodName());
    }

    public boolean getIsUiMonitoring(){
        return mIsUiMonitoring;
    }

    public void startUiMonitor(int time){
        mMonitorTestTime = time;
        mIsUiMonitoring = true;
        Looper.getMainLooper().setMessageLogging(this);
    }

    public void stopUiMonitor(){
        Looper.getMainLooper().setMessageLogging(null);
        mIsUiMonitoring = false;
    }

    public String getLastUiDelayContent(){
        return mUIDelayContent;
    }

    @Override
    public void println(String logLooper) {
        if (logLooper != null && isStartLogPrint) {
            mMonitorHandler.removeCallbacksAndMessages(null);
            mMonitorHandler.postDelayed(mUiMonitorRunnable, mMonitorTestTime);
        }
        isStartLogPrint = !isStartLogPrint;//每次只在logPrint的start发送消息，第一次肯定是logPrint的end事件调用println
    }


    private void writeUiDelayLog(String uiDelay, String availableMemory){
        long time = System.currentTimeMillis();
        mUIDelayContent = TimeUtil.yyyyMMddHHmmssSSS(time) + "\n" +
                "Available Memory:" + availableMemory + " Total Memory:" + getTotalMemory() + "\n"
                + uiDelay;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // SDCard正常
            String pathUiMonitor = Constant.SAVE_PATH + "ui.log";
            FileUtil.append2file(pathUiMonitor, mUIDelayContent);
        }
        Log.d("UITime", mUIDelayContent);
    }

    /**
     * 获取系统内存信息
     * @return
     */
    private String getTotalMemory() {
        ActivityManager am = (ActivityManager) BaseApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(BaseApplication.getInstance(), mi.totalMem);
    }

    /**
     * 获取当前android可用内存大小
     * @return
     */
    private String getAvailMemory() {
        ActivityManager am = (ActivityManager) BaseApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(BaseApplication.getInstance(), mi.availMem);// 将获取的内存大小规格化
    }
}
