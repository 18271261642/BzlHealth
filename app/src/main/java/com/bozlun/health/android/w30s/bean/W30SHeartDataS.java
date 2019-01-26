package com.bozlun.health.android.w30s.bean;

/**
 * @aboutContent:
 * @author： An
 * @crateTime: 2018/3/14 09:56
 * @mailBox: an.****.life@gmail.com
 * @company: 东莞速成科技有限公司
 */

public class W30SHeartDataS {
    private long time;
    private int value;

    public W30SHeartDataS() {
    }

    public W30SHeartDataS(long time, int value) {
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
