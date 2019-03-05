package com.bozlun.health.android.commdbserver;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Admin
 * Date 2019/2/28
 * 每天总步数的bean 汇总的步数
 * 上传的bean
 */
public class CommStepCountDb extends LitePalSupport {


    /**
     * userId
     */
    private String userid;

    /**
     * 日期 yyyy-MM-dd格式
     */
    private String date;

    /**
     * 步数总数
     */
    private int stepnumber;

    private int count;

    /**
     * 设备mac地址
     */
    private String devicecode;

    /**
     * 设备名称
     * @return
     */
    private String bleName;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStepnumber() {
        return stepnumber;
    }

    public void setStepnumber(int stepnumber) {
        this.stepnumber = stepnumber;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDevicecode() {
        return devicecode;
    }

    public void setDevicecode(String devicecode) {
        this.devicecode = devicecode;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    @Override
    public String toString() {
        return "CommStepCountDb{" +
                "userid='" + userid + '\'' +
                ", date='" + date + '\'' +
                ", stepnumber=" + stepnumber +
                ", count=" + count +
                ", devicecode='" + devicecode + '\'' +
                ", bleName='" + bleName + '\'' +
                '}';
    }
}
