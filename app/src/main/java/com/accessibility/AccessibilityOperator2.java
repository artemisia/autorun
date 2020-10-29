package com.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;

import com.accessibility.utils.AccessibilityLog;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityOperator2 {

    private static final String TARGET_ACTIIVTY = "";
    private static final String TARGET_TEXT = "";
    private long delayTime = 1000;

    private Context mContext;
    private static AccessibilityOperator2 mInstance = new AccessibilityOperator2();
    private AccessibilityEvent mAccessibilityEvent;
    private AccessibilityService mAccessibilityService;

    private AccessibilityOperator2() {
    }

    public static AccessibilityOperator2 getInstance() {
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void updateEvent(AccessibilityService service, AccessibilityEvent event) {
        if (service != null && mAccessibilityService == null) {
            mAccessibilityService = service;
        }
        if (event != null) {
            mAccessibilityEvent = event;
        }
    }

    public void progressQQChat(AccessibilityEvent event) {

        if (TextUtils.isEmpty(event.getClassName())) {
            return;
            //如果当前页面是聊天页面或者当前的描述信息是"返回消息界面"，就肯定是对话页面
        }

        //验证当前事件是否符合查询页面上的红包
        if (!invalidEnvelopeUi(event)) {
            return;
        }

        //延迟点击红包，防止被检测到开了抢红包，不过感觉还是感觉会被检测到，应该有的效果吧...
        try {
            Thread.sleep(delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //普通红包,检索点击拆开的字眼。
        List<AccessibilityNodeInfo> envelope = findViewListByText(TARGET_TEXT, false);
        //处理普通红包
        progressNormal(envelope);
    }


    public boolean invalidEnvelopeUi(AccessibilityEvent event) {

        //判断类名是否是聊天页面
        if (!TARGET_ACTIIVTY.equals(event.getClassName().toString())) {
            return true;
        }

        //判断页面中的元素是否有点击拆开的文本，有就返回可以进行查询了
        int recordCount = event.getRecordCount();
        if (recordCount > 0) {
            for (int i = 0; i < recordCount; i++) {
                AccessibilityRecord record = event.getRecord(i);
                if (record == null) {
                    break;
                }
                List<CharSequence> text = record.getText();
                if (text != null && text.size() > 0 && text.contains(TARGET_TEXT)) {
                    //如果文本中有点击拆开的字眼，就返回可以进行查询了
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @param clickable 该View是否可以点击
     * @return View
     */
    public List<AccessibilityNodeInfo> findViewListByText(String text, boolean clickable) {
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = new ArrayList<>();

        AccessibilityNodeInfo accessibilityNodeInfo = getRootNodeInfo();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    accessibilityNodeInfoList.add(nodeInfo);
                }
            }
        }
        return accessibilityNodeInfoList;
    }

    private AccessibilityNodeInfo getRootNodeInfo() {
        AccessibilityEvent curEvent = mAccessibilityEvent;
        AccessibilityNodeInfo nodeInfo = null;
        if (Build.VERSION.SDK_INT >= 16) {
            // 建议使用getRootInActiveWindow，这样不依赖当前的事件类型
            if (mAccessibilityService != null) {
                nodeInfo = mAccessibilityService.getRootInActiveWindow();
                AccessibilityLog.printLog("nodeInfo: " + nodeInfo);
            }
            // 下面这个必须依赖当前的AccessibilityEvent
//            nodeInfo = curEvent.getSource();
        } else {
            nodeInfo = curEvent.getSource();
        }
        return nodeInfo;
    }

    public void progressNormal(List<AccessibilityNodeInfo> passwordList) {
        if (passwordList != null && passwordList.size() > 0) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : passwordList) {
                if (accessibilityNodeInfo != null && !TextUtils.isEmpty(accessibilityNodeInfo.getText()) && TARGET_TEXT.equals(accessibilityNodeInfo.getText().toString())) {

                    //点击拆开红包
                    performViewClick(accessibilityNodeInfo);
                }
            }
            //最后延迟事件触发返回事件，关闭红包页面
        }
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
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟返回操作
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void performBackClick(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAccessibilityService.performGlobalAction(mAccessibilityService.GLOBAL_ACTION_BACK);
    }
}
