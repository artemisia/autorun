package com.accessibility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MusicPlayerService extends Service {
    private static final String TAG = MusicPlayerService.class.getSimpleName();

    public static final String TAG_BG_SERVICE = "TAG_BG_SERVICE";
    public static final int TAG_START_BG_SERVICE = 10;
    public static final int TAG_STOP_BG_SERVICE = 11;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        boolean bgServiceOp = intent.getIntExtra(TAG_BG_SERVICE, TAG_START_BG_SERVICE) == TAG_START_BG_SERVICE;
        if (bgServiceOp) {
            createNotification();
        } else {
            stopService();
        }

        return START_STICKY;
    }

    private void stopService() {
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        System.exit(1);
    }

    private void createNotification() {
        String CHANNEL_ID = "com.example.recyclerviewtest.N1";
        String CHANNEL_NAME = "TEST";
        NotificationChannel notificationChannel = null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(this, AccessibilityMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID).
                setContentTitle("antutu test").
                setContentText("").
                setWhen(System.currentTimeMillis()).
                setSmallIcon(R.mipmap.ic_launcher).
                setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)).
                setContentIntent(pendingIntent).build();
        startForeground(1, notification);
    }



    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }
}
