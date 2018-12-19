package com.example.ydd.mylibrary;

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
import com.yanzhenjie.andserver.http.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@RestController
public class Printer {

    Gson gson;
    HashMap<String, EthernetPort> ethernetPortHashMap = new HashMap<>();

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

            EscCommand esc = new EscCommand();

            esc.addInitializePrinter();
            esc.addPrintAndFeedLines((byte) 3);
            // 设置打印居中
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
            // 设置为倍高倍宽
            esc.addSelectPrintModes(EscCommand.FONT.FONTB, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);

            esc.addText(printBill.getTableNumber());
            esc.addPrintAndFeedLines((byte) 2);
            esc.addText(printBill.getCreateTime());
            esc.addPrintAndFeedLines((byte) 2);
            esc.addText(printBill.getPeopleNum() + "人");

            esc.addPrintAndLineFeed();
            // 打印文字

            分单(list, esc, printBill);

            esc.addText("####################################################");

            esc.addPrintAndFeedLines((byte) 8);

            Vector<Byte> datas = esc.getCommand();


            try {
                ethernetPort.writeDataImmediately(datas, 0, datas.size());
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {

            分单(list, null, printBill);
        }


    }

    private void 分单(List<PrintMerchandise> list, EscCommand esc, PrintBill printBill) {
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

            EscCommand esc1 = new EscCommand();
            esc1.addInitializePrinter();
            esc1.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
            esc1.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);

            esc1.addText(printBill.getTableNumber());
            esc1.addPrintAndFeedLines((byte) 2);
            esc1.addText(printBill.getCreateTime());
            esc1.addPrintAndFeedLines((byte) 2);
            esc1.addText(printBill.getPeopleNum() + "人");
            esc1.addPrintAndFeedLines((byte) 2);

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

    @PostMapping("/check_order")
    public void printCheckOrder(RequestBody body, HttpResponse response) {
        String json = null;
        try {

            json = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @GetMapping("/test")
    public void test(HttpResponse response) {


        String json = "{\n" +
                "      \"channelId\": \"f4b572c2\",\n" +
                "      \"className\": \"Area\",\n" +
                "      \"name\": \"房间1\"\n" +
                "    }";

        ResponseBody body = new StringBody(json);
        response.setBody(body);

    }

    @GetMapping("/stop")
    public void stop() {

        for (Map.Entry<String, EthernetPort> entry : ethernetPortHashMap.entrySet()) {

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

    void sendReceiptWithResponse(String msg) throws IOException {





/*
        if (mPort == null) {

            mPort = new EthernetPort(msg, 9100);
            mPort.openPort();
            portHashMap.put(msg,mPort);

            Log.e("DOAING", "新创建的");
        } else if (!mPort.openPort()) {

            portHashMap.get(msg).openPort();

            Log.e("DOAING", "断开了，再打开");
        }*/


     /*   EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText("\n");
        esc.addPrintAndLineFeed();

        *//* 打印文字 *//*
        // 取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        // 打印文字
        esc.addText("Print text\n");
        // 打印文字
        esc.addText("Welcome to use SMARNET printer!\n");

        *//* 打印繁体中文 需要打印机支持繁体字库 *//*
        String message = "佳博智匯票據打印機\n";
        esc.addText(message, "GB2312");
        esc.addPrintAndLineFeed();

        *//* 绝对位置 具体详细信息请查看GP58编程手册 *//*
        esc.addText("智汇");
        esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
        esc.addSetAbsolutePrintPosition((short) 6);
        esc.addText("网络");
        esc.addSetAbsolutePrintPosition((short) 10);
        esc.addText("设备");
        esc.addPrintAndLineFeed();

        *//* 打印图片 *//*
        // 打印文字
        esc.addText("Print bitmap!\n");
 *//*       Bitmap b = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        // 打印图片
        esc.addOriginRastBitImage(b, 384, 0);*//*

         *//* 打印一维条码 *//*
        // 打印文字
        esc.addText("Print code128\n");
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        // 设置条码可识别字符位置在条码下方
        // 设置条码高度为60点
        esc.addSetBarcodeHeight((byte) 60);
        // 设置条码单元宽度为1
        esc.addSetBarcodeWidth((byte) 1);
        // 打印Code128码
        esc.addCODE128(esc.genCodeB("SMARNET"));
        esc.addPrintAndLineFeed();

        *//*
         * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
         *//*
        // 打印文字
        esc.addText("Print QRcode\n");
        // 设置纠错等级
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31);
        // 设置qrcode模块大小
        esc.addSelectSizeOfModuleForQRCode((byte) 3);
        // 设置qrcode内容
        esc.addStoreQRCodeData("www.smarnet.cc");
        esc.addPrintQRCode();// 打印QRCode
        esc.addPrintAndLineFeed();

        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        //打印文字
        esc.addText("Completed!\r\n");

        // 开钱箱
        esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
        esc.addPrintAndFeedLines((byte) 8);
        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        Vector<Byte> datas = esc.getCommand();
        // 发送数据

        mPort.writeDataImmediately(datas, 0, datas.size());*/

    }

/*    void printAll(String msg) throws IOException {


        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 2);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText(msg + "\n");
        esc.addPrintAndLineFeed();

        Vector<Byte> datas = esc.getCommand();

        ethernetPort.writeDataImmediately(datas, 0, datas.size());
    }*/

}
