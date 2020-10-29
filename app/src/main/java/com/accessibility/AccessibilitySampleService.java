package com.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.accessibility.utils.AccessibilityLog;

import java.util.List;

/**
 * Created by popfisher on 2017/7/6.
 */

@TargetApi(16)
public class AccessibilitySampleService extends AccessibilityService {

    private AccessibilityManager mAccessibilityManager;
    private Context mContext;
    private static AccessibilitySampleService mInstance;

    private static final String BenchResult = "跑分监控";

    private static final int MSG_CHECK = 1;
    private static final int INTERVAL = 1000 * 2;

    private HandlerThread handlerThread = null;

    public static AccessibilitySampleService getInstance() {
        if (mInstance == null) {
            mInstance = new AccessibilitySampleService();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mAccessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    Handler workHandler = null;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        initHandleThread();


        AccessibilityLog.printLog("AccessibilitySampleService.onServiceConnected: xx");
        // 通过代码可以动态配置，但是可配置项少一点
//        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
//        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED
//                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
//                | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
//        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
//        accessibilityServiceInfo.notificationTimeout = 0;
//        accessibilityServiceInfo.flags = AccessibilityServiceInfo.DEFAULT;
//        setServiceInfo(accessibilityServiceInfo);
    }

    private void initHandleThread() {
        handlerThread = new HandlerThread("AccessibilityThread");
        handlerThread.start();

        workHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_CHECK) {
                    workHandler.removeMessages(MSG_CHECK);

                    manualMessage();

                    workHandler.sendEmptyMessageDelayed(MSG_CHECK, INTERVAL);
                }
            }
        };

        workHandler.sendEmptyMessageDelayed(MSG_CHECK, INTERVAL);
    }

    private void manualMessage() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        String currentPkg = accessibilityNodeInfo.getPackageName().toString();
        AccessibilityLog.printLog("manualMessage " + currentPkg);

        if (timeisUp()) {
            antutuIssue(currentPkg);
            //bilibiliIssue(pkgName);
        }
    }

    private long lastIssueCheck = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 此方法是在主线程中回调过来的，所以消息是阻塞执行的
        // 获取包名
        String pkgName = event.getPackageName().toString();
        int eventType = event.getEventType();
        //AccessibilityOperator.getInstance().updateEvent(this, event);
        //AccessibilityOperator2.getInstance().updateEvent(this, event);
//        try {
//            Thread.sleep(200);
//        } catch (Exception e) {}
        AccessibilityLog.printLog("eventType: " + eventType + " pkgName: " + pkgName);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                break;
        }

        if (timeisUp()) {
            antutuIssue(pkgName);
            //bilibiliIssue(pkgName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handlerThread.quit();
    }

    private boolean timeisUp() {
        if (INTERVAL < (System.currentTimeMillis() - lastIssueCheck)) {
            lastIssueCheck = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }


    private void bilibiliIssue(String pkgName) {
        if (!pkgName.equals("tv.danmaku.bili")) {
            return;
        }

        AccessibilityNodeInfo target = findViewByText("会员购");
        AccessibilityLog.printLog("findViewByText: " + target);
        if (target != null) {
            AccessibilityNodeInfo clickable = getClickableParent(target);
            if (clickable != null) {
                performViewClick(clickable);
            }
        }
    }

    private void antutuIssue(String pkgName) {
        if (!pkgName.equals("com.antutu.ABenchMark")) {
            return;
        }

        clickByText("立即测试");
        clickByText("重新测试");
        benchResultIssue();
    }

    private void benchResultIssue() {
        AccessibilityNodeInfo target = findViewByText(BenchResult);
        AccessibilityLog.printLog("findViewByText: " + target);
        if (target != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            performBackClick();
        }
    }

    private void performBackClick() {
        AccessibilityLog.printLog("performBackClick ");
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    private void clickByText(String text) {
        AccessibilityNodeInfo target = findViewByText(text);
        AccessibilityLog.printLog("findViewByText: " + target);
        if (target != null) {
            AccessibilityNodeInfo clickable = getClickableParent(target);
            if (clickable != null) {
                performViewClick(clickable);
            }
        }
    }

    private AccessibilityNodeInfo getClickableParent(AccessibilityNodeInfo target) {

        AccessibilityNodeInfo result = null;
        AccessibilityNodeInfo temp = target;
        while (true) {
            if (temp == null) {
                break;
            }
            if (temp.isClickable()) {
                result = temp;
                break;
            }
            temp = temp.getParent();
        }

        return result;
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        AccessibilityLog.printLog("performViewClick: ");

        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }
}
