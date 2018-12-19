package com.example.ydd.printserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;
    Intent intent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();
        final TextView ip = findViewById(R.id.ip);


        final List<String> ips = new ArrayList<>();

        ips.add("192.168.2.110");

        ips.add("192.168.2.248");

        final Gson gson = new Gson();


        ip.setText(NetUtils.getLocalIPAddress().getHostAddress());

        intent = new Intent(this, PrintServer.class);
        startService(intent);
        findViewById(R.id.start1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody body = RequestBody.create(JSON, gson.toJson(ips));
                Request request = new Request.Builder()
                        .url("http://192.168.2.111:8080/open")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("DOAING", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                      Log.e("DOAING", response.body().string());
                    }
                });
            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Request request = new Request.Builder()
                        .url("http://192.168.2.111:8080/stop")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("DOAING", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        Log.e("DOAING", response.body().string());
                    }
                });


            }
        });

    }
/*
    private Socket openPrint(String ip, int port) {

        Socket socket = null;

        try {
            Class<?> clazz = Class.forName("com.gprinter.io.EthernetPort");
            //获取有参构造
            try {
                Constructor c = clazz.getConstructor(String.class, int.class);
                try {
                    mPort = (EthernetPort) c.newInstance(ip, port);
                    mPort.openPort();
                    try {
                        Field field = clazz.getDeclaredField("mSocket");
                        field.setAccessible(true);
                        socket = (Socket) field.get(mPort);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return socket;
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }
}
