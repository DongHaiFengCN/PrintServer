package com.example.ydd.client;

import java.util.List;

/**
 * @author dong
 *
 * 说明：分单打印中的总单，order数据的载体
 */
public class PrintBill {

    private String tableNumber;
    private String createTime;
    private String ip;

    public String getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(String peopleNum) {
        this.peopleNum = peopleNum;
    }

    private String peopleNum;
    private List<PrintMerchandise> printMerchandises;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<PrintMerchandise> getPrintMerchandises() {
        return printMerchandises;
    }

    public void setPrintMerchandises(List<PrintMerchandise> printMerchandises) {
        this.printMerchandises = printMerchandises;
    }
    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
