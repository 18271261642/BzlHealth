package com.bozlun.health.android.commdbserver;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Admin
 * Date 2019/2/28
 * 睡眠上传
 */
public class CommSleepDb extends LitePalSupport {

    /**
     * userId
     */
    private String userid;

    /**
     * mac地址
     */
    private String devicecode;

    /**
     * 日期 yyyy-MM-dd
     */
    private String rtc;

    /**
     * 清醒时长 分钟
     */
    private int soberlen;

    /**
     * 深睡时长 分钟
     */
    private int deepsleep;

    /**
     * 浅睡时长  分钟
     */
    private int shallowsleep;

    /**
     * 睡眠总时长
     */
    private int sleeplen;

    /**
     * 入睡时间 yyyy-MM-dd HH:mm:ss
     */
    private String sleeptime;

    /**
     * 清醒时间 yyyy-MM-dd HH:mm:ss
     */
    private String waketime;

    /**
     * 设备名称
     */
    private String bleName;

    /**
     * 清醒次数
     */
    private int wakecount;

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

    public int getSoberlen() {
        return soberlen;
    }

    public void setSoberlen(int soberlen) {
        this.soberlen = soberlen;
    }

    public int getDeepsleep() {
        return deepsleep;
    }

    public void setDeepsleep(int deepsleep) {
        this.deepsleep = deepsleep;
    }

    public int getShallowsleep() {
        return shallowsleep;
    }

    public void setShallowsleep(int shallowsleep) {
        this.shallowsleep = shallowsleep;
    }

    public int getSleeplen() {
        return sleeplen;
    }

    public void setSleeplen(int sleeplen) {
        this.sleeplen = sleeplen;
    }

    public String getSleeptime() {
        return sleeptime;
    }

    public void setSleeptime(String sleeptime) {
        this.sleeptime = sleeptime;
    }

    public String getWaketime() {
        return waketime;
    }

    public void setWaketime(String waketime) {
        this.waketime = waketime;
    }

    public int getWakecount() {
        return wakecount;
    }

    public void setWakecount(int wakecount) {
        this.wakecount = wakecount;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    @Override
    public String toString() {
        return "CommSleepDb{" +
                "userid='" + userid + '\'' +
                ", devicecode='" + devicecode + '\'' +
                ", rtc='" + rtc + '\'' +
                ", soberlen=" + soberlen +
                ", deepsleep=" + deepsleep +
                ", shallowsleep=" + shallowsleep +
                ", sleeplen=" + sleeplen +
                ", sleeptime='" + sleeptime + '\'' +
                ", waketime='" + waketime + '\'' +
                ", bleName='" + bleName + '\'' +
                ", wakecount=" + wakecount +
                '}';
    }
}
