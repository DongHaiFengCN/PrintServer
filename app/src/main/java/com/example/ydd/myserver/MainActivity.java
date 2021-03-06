package com.example.ydd.myserver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.gson.Gson;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends Activity {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;



    public String printerServerUrl;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new OkHttpClient();

        printerServerUrl = "http://192.168.2.111:8080/order";

        final PrintBill printBill = new PrintBill();

        printBill.setPeopleNum("3");

        printBill.setIp("");

        printBill.setTableNumber("五号桌");

        DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String s = format1.format(new Date());

        printBill.setCreateTime(s);
        List<PrintMerchandise> printMerchandiseList = new ArrayList<>();

        PrintMerchandise printMerchandise1 = new PrintMerchandise();

        printMerchandise1.setIp("192.168.2.248");

        printMerchandise1.setName("我是热菜");

        printMerchandise1.setPrice("20.2");

        printMerchandise1.setSum("23");


        PrintMerchandise printMerchandise2 = new PrintMerchandise();

        printMerchandise2.setIp("192.168.2.248");

        printMerchandise2.setName("我是凉菜");

        printMerchandise2.setPrice("18.2");

        printMerchandise2.setSum("33");

        //printMerchandiseList.add(printMerchandise1);

        printMerchandiseList.add(printMerchandise2);

        printBill.setPrintMerchandises(printMerchandiseList);

        final TextView ip = findViewById(R.id.ip);


        final List<String> ips = new ArrayList<>();

        ips.add("192.168.2.110");

        ips.add("192.168.2.248");

        final Gson gson = new Gson();


        ip.setText(NetUtils.getLocalIPAddress().getHostAddress());


        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody body = RequestBody.create(JSON, gson.toJson(ips));
                Request request = new Request.Builder()
                        .url("http://192.168.2.111:8080/open")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(okhttp3.Call call, Response response) throws IOException {


                        Log.e("DOAING",response.body().string());
                    }


                });
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Request request = new Request.Builder()
                        .url("http://192.168.2.111:8080/stop")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(okhttp3.Call call, Response response) throws IOException {

                    }


                });


            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                post(printerServerUrl, gson.toJson(printBill));

            }
        });


    }

    void post(String url, String json) {

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

            }


        });

    }
}
