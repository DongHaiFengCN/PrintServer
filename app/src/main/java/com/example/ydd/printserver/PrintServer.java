package com.example.ydd.printserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.util.concurrent.TimeUnit;

public class PrintServer extends Service {

    private Server mServer;


    @Override
    public void onCreate() {

        Log.e("DOAING","创建了");

        mServer = AndServer.serverBuilder()
                .inetAddress(NetUtils.getLocalIPAddress())
                .port(8080)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {

                        Log.e("DOAING","打印服务开启了");
                    }

                    @Override
                    public void onStopped() {

                        Log.e("DOAING","打印服务关闭了");
                    }

                    @Override
                    public void onException(Exception e) {
                    }
                })
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServer.startup();

        Log.e("DOAING",Thread.currentThread().getId()+"");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServer.shutdown();
        Log.e("DOAING","停止了～");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
