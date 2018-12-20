package com.example.ydd.printserver;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {

    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(this, PrintServer.class);

        startService(intent);
    }


    /**
     * 在退出整个application的时候关闭服务
     */
    void stopServer() {

        stopService(intent);


    }
}
