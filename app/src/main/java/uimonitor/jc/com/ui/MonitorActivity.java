package uimonitor.jc.com.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import uimonitor.jc.com.R;


public class MonitorActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBtnUiMonitor;
    private Button mBtnSleep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        init();
    }

    private void init(){
        mBtnUiMonitor = (Button) findViewById(R.id.btn_uiMonitor);
        mBtnSleep = (Button) findViewById(R.id.btn_sleep);
        mBtnUiMonitor.setOnClickListener(this);
        mBtnSleep.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == mBtnUiMonitor){
            UiMonitorActivity.launch(MonitorActivity.this);
        }
        else if(view == mBtnSleep){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
