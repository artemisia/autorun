package com.accessibility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.accessibility.utils.AccessibilityLog;
import com.accessibility.utils.AccessibilityUtil;

public class AccessibilityMainActivity extends Activity implements View.OnClickListener {

    private View mOpenSetting;
    private TextView status;
    private View toStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility_main);
        initView();
        AccessibilityOperator.getInstance().init(this);
        AccessibilityOperator2.getInstance().init(this);

        AccessibilityLog.printLog("AccessibilityMainActivity.onCreatex");
    }

    private void bgServiceIssue(boolean onOff) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.putExtra(MusicPlayerService.TAG_BG_SERVICE,
                onOff ? MusicPlayerService.TAG_START_BG_SERVICE: MusicPlayerService.TAG_STOP_BG_SERVICE);
        startService(intent);
    }

    private void initView() {
        mOpenSetting = findViewById(R.id.open_accessibility_setting);
        mOpenSetting.setOnClickListener(this);
        findViewById(R.id.accessibility_find_and_click).setOnClickListener(this);
        status = findViewById(R.id.status);
        reflushStatus();

        toStop = findViewById(R.id.toStop);
        toStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bgServiceIssue(false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(1);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        reflushStatus();
    }

    private void reflushStatus() {
        if (isServiceRunning()) {
            status.setText("状态：已开启, 请启动安兔兔app");
        } else {
            status.setText("请点击上面开启按钮，找到 安兔兔自动重跑, 并开启");
        }
    }

    public boolean isServiceRunning() {
        return AccessibilityUtil.isAccessibilitySettingsOn(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.open_accessibility_setting:
                bgServiceIssue(true);
                OpenAccessibilitySettingHelper.jumpToSettingPage(this);
                break;
            case R.id.accessibility_find_and_click:
                bgServiceIssue(true);
                startActivity(new Intent(this, AccessibilityNormalSample.class));
                break;
        }
    }
}
