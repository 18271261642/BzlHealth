package com.suchengkeji.android.w30sblelibrary.bean.servicebean;


import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @aboutContent:
 * @author： An
 * @crateTime: 2018/3/15 09:36
 * @mailBox: an.****.life@gmail.com
 * @company: 东莞速成科技有限公司
 */

public class W30S_SleepDataItem extends LitePalSupport implements Serializable {
    private String sleepType;
    private String startTime;

    public W30S_SleepDataItem() {
    }

    public String getSleepType() {
        return sleepType;
    }

    public void setSleepType(String sleepType) {
        this.sleepType = sleepType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "W30S_SleepDataItem{" +
                "sleepType='" + sleepType + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}
