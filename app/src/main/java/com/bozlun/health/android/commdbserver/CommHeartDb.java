package com.bozlun.health.android.commdbserver;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Admin
 * Date 2019/2/28
 * 上传心率时的bean
 */
public class CommHeartDb extends LitePalSupport {

    /**
     * 用户ID
     */
    private String userid;

    /**
     * 设备名称
     *
     */
    private String bleName;

    /**
     * mac地址
     */
    private String devicecode;

    /**
     * 日期 yyyy-MM-dd格式
     */
    private String rtc;

    /**
     * 最大心率
     */
    private int maxheartrate;

    /**
     * 最小心率
     */
    private int minheartrate;

    /**
     * 平均心率
     */
    private int avgheartrate;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDevicecode() {
        return devicecode;
    }

    public void setDevicecode(String devicecode) {
        this.devicecode = devicecode;
    }

    public String getRtc() {
        return rtc;
    }

    public void setRtc(String rtc) {
        this.rtc = rtc;
    }

    public int getMaxheartrate() {
        return maxheartrate;
    }

    public void setMaxheartrate(int maxheartrate) {
        this.maxheartrate = maxheartrate;
    }

    public int getMinheartrate() {
        return minheartrate;
    }

    public void setMinheartrate(int minheartrate) {
        this.minheartrate = minheartrate;
    }

    public int getAvgheartrate() {
        return avgheartrate;
    }

    public void setAvgheartrate(int avgheartrate) {
        this.avgheartrate = avgheartrate;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    @Override
    public String toString() {
        return "CommHeartDb{" +
                "userid='" + userid + '\'' +
                ", bleName='" + bleName + '\'' +
                ", devicecode='" + devicecode + '\'' +
                ", rtc='" + rtc + '\'' +
                ", maxheartrate=" + maxheartrate +
                ", minheartrate=" + minheartrate +
                ", avgheartrate=" + avgheartrate +
                '}';
    }
}
