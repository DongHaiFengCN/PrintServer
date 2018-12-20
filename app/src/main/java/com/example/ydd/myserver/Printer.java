package com.example.ydd.myserver;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gprinter.command.EscCommand;
import com.gprinter.io.EthernetPort;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class Printer {

    Gson gson;
    ConcurrentHashMap<String, EthernetPort> ethernetPortHashMap = new ConcurrentHashMap<>();

    @PostMapping("/order")
    public void printOrder(RequestBody body, HttpResponse response) {

        String json = null;

        try {

            json = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintBill printBill = gson.fromJson(json, PrintBill.class);


        if (printBill == null) {

            response.setBody(new StringBody("订单解析出错，重新发送！"));

            return;
        }

        List<PrintMerchandise> list = printBill.getPrintMerchandises();

        String billIp = printBill.getIp();


        if (!"".equals(billIp)) {

            EthernetPort ethernetPort = ethernetPortHashMap.get(billIp);

            verifyState(ethernetPort);


            EscCommand esc = new EscCommand();

            esc.addInitializePrinter();
            esc.addPrintAndFeedLines((byte) 3);
            // 设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            // 设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

            esc.addText(printBill.getTableNumber());
            esc.addPrintAndFeedLines((byte) 2);
            esc.addText(printBill.getCreateTime());
            esc.addPrintAndFeedLines((byte) 2);
            esc.addText(printBill.getPeopleNum() + "人");

            esc.addPrintAndLineFeed();
            // 打印文字

            distribute(list, esc, printBill);

            esc.addText("###############################################\n\n\n\n\n\n\n\n\n");

            Vector<Byte> datas = esc.getCommand();

            try {
                ethernetPort.writeDataImmediately(datas, 0, datas.size());
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {

            distribute(list, null, printBill);
        }


    }

    private void verifyState(EthernetPort ethernetPort) {


        Socket socket = null;

        try {
            Class<?> clazz = ethernetPort.getClass();
            //获取有参构造

            try {
                Field field = clazz.getDeclaredField("mSocket");
                field.setAccessible(true);

                socket = (Socket) field.get(ethernetPort);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        send(socket);

        //isServerClose(socket);




     /*  if(send(socket)){
           Log.e("DOAING",socket.toString()+"  连接成功～");

       }else {
           Log.e("DOAING",socket.toString()+"  连接失败～");
       }*/
    }

    /**
     * 发送数据，发送失败返回false,发送成功返回true
     *
     * @return
     */
    public void send(Socket socket) {
        try {
            /*PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("测试--");*/

            socket.getOutputStream().write(0xff);
            socket.getOutputStream().flush();
            Log.e("DOAING", "发送了");

        } catch (Exception se) {
            se.printStackTrace();
            Log.e("DOAING", "没发送");
        }
    }

    /*  *//**
     * 判断是否断开连接，断开返回true,没有返回false
     * @param socket
     * @return
     *//*
    public Boolean isServerClose(Socket socket){
        try{
            socket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信

            Log.e("DOAING","开着");
            return false;
        }catch(Exception se){
            Log.e("DOAING","关了");
            return true;
        }
    }*/

    /**
     * 分单打印
     *
     * @param list
     * @param esc
     * @param printBill
     */
    private void distribute(List<PrintMerchandise> list, EscCommand esc, PrintBill printBill) {
        for (PrintMerchandise p : list) {


            if (esc != null) {

                esc.addPrintAndFeedLines((byte) 1);
                esc.addSetAbsolutePrintPosition((short) 4);
                esc.addText(p.getName());
                esc.addSetAbsolutePrintPosition((short) 300);
                esc.addText(p.getSum() + "份");
                esc.addPrintAndFeedLines((byte) 2);

            }


            String ip = p.getIp();

            EthernetPort ethernetPort1 = ethernetPortHashMap.get(ip);

            verifyState(ethernetPort1);
            EscCommand esc1 = new EscCommand();
            esc1.addInitializePrinter();
            esc1.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            esc1.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
            esc1.addText(printBill.getTableNumber());
            esc1.addPrintAndFeedLines((byte) 2);
            esc1.addText(printBill.getCreateTime());
            esc1.addPrintAndFeedLines((byte) 2);
            esc1.addText(printBill.getPeopleNum() + "人");
            esc1.addPrintAndFeedLines((byte) 2);
            esc1.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
            esc1.addSetAbsolutePrintPosition((short) 4);
            esc1.addText(p.getName());
            esc1.addSetAbsolutePrintPosition((short) 300);
            esc1.addText(p.getSum() + "份\n\n\n");

            esc1.addPrintAndFeedLines((byte) 8);
            Vector<Byte> datas = esc1.getCommand();


            try {
                ethernetPort1.writeDataImmediately(datas, 0, datas.size());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 结账单的打印 checkOrder
     *
     * @param body
     */
    @PostMapping("/check_order")
    public void printCheckOrder(RequestBody body) {
        String json = null;
        try {

            json = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  gson.fromJson(json,);

    }

    @GetMapping("/stop")
    public void stop() {

        for (Map.Entry<String, EthernetPort> entry : ethernetPortHashMap.entrySet()) {


            Log.e("DOAING", "关闭了：" + entry.getKey());
            entry.getValue().closePort();
        }
        ethernetPortHashMap.clear();
    }

    @PostMapping("/open")
    public void open(RequestBody body, HttpResponse response) {
        List fail = null;
        String json = null;

        try {
            json = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (gson == null) {
            gson = new Gson();
        }
        List<String> stringList = gson.fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        EthernetPort ethernetPort;
        for (int i = 0; i < stringList.size(); i++) {
            String ip = stringList.get(i);

            if (ethernetPortHashMap.get(ip) == null) {

                ethernetPort = new EthernetPort(ip, 9100);
                if (ethernetPort.openPort()) {
                    ethernetPortHashMap.put(ip, ethernetPort);

                    Log.e("DOAING", "打开了：" + ip);
                } else {
                    if (fail == null) {
                        fail = new ArrayList();
                    }
                    fail.add(ip);
                }
            }

        }

        if (fail != null) {
            String rs = gson.toJson(fail);
            response.setBody(new StringBody(rs));
        }

    }

}
