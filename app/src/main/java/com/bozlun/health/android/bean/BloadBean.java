package com.bozlun.health.android.bean;

import java.util.List;

/**
 * @aboutContent:
 * @author： An
 * @crateTime: 2018/9/6 17:02
 * @mailBox: an.****.life@gmail.com
 * @company: 东莞速成科技有限公司
 */

public class BloadBean {


    /**
     * bloodPressure : [{"rtc":"2018-08-30","maxDiastolic":63,"avgSystolic":114,"avgDiastolic":75,"weekCount":"4","minSystolic":151},{"rtc":"2018-08-31","maxDiastolic":62,"avgSystolic":116,"avgDiastolic":74,"weekCount":"5","minSystolic":172},{"rtc":"2018-09-01","maxDiastolic":67,"avgSystolic":126,"avgDiastolic":83,"weekCount":"6","minSystolic":143},{"rtc":"2018-09-02","maxDiastolic":65,"avgSystolic":100,"avgDiastolic":65,"weekCount":"7","minSystolic":100},{"rtc":"2018-09-03","maxDiastolic":67,"avgSystolic":117,"avgDiastolic":78,"weekCount":"1","minSystolic":140},{"rtc":"2018-09-04","maxDiastolic":0,"avgSystolic":0,"weekCount":"2","avgDiastolic":0,"minSystolic":0},{"rtc":"2018-09-05","maxDiastolic":0,"avgSystolic":0,"weekCount":"3","avgDiastolic":0,"minSystolic":0}]
     * max_min : {"maxDiastolic":62,"minSystolic":172}
     * resultCode : 001
     */

    private MaxMinBean max_min;
    private String resultCode;
    private List<BloodPressureBean> bloodPressure;

    public MaxMinBean getMax_min() {
        return max_min;
    }

    public void setMax_min(MaxMinBean max_min) {
        this.max_min = max_min;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public List<BloodPressureBean> getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(List<BloodPressureBean> bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public static class MaxMinBean {
        /**
         * maxDiastolic : 62
         * minSystolic : 172
         */

        private int maxDiastolic;
        private int minSystolic;

        public int getMaxDiastolic() {
            return maxDiastolic;
        }

        public void setMaxDiastolic(int maxDiastolic) {
            this.maxDiastolic = maxDiastolic;
        }

        public int getMinSystolic() {
            return minSystolic;
        }

        public void setMinSystolic(int minSystolic) {
            this.minSystolic = minSystolic;
        }
    }

    public static class BloodPressureBean {
        /**
         * rtc : 2018-08-30
         * maxDiastolic : 63
         * avgSystolic : 114
         * avgDiastolic : 75
         * weekCount : 4
         * minSystolic : 151
         */

        private String rtc;
        private int maxDiastolic;
        private int avgSystolic;
        private int avgDiastolic;
        private String weekCount;
        private int minSystolic;

        public String getRtc() {
            return rtc;
        }

        public void setRtc(String rtc) {
            this.rtc = rtc;
        }

        public int getMaxDiastolic() {
            return maxDiastolic;
        }

        public void setMaxDiastolic(int maxDiastolic) {
            this.maxDiastolic = maxDiastolic;
        }

        public int getAvgSystolic() {
            return avgSystolic;
        }

        public void setAvgSystolic(int avgSystolic) {
            this.avgSystolic = avgSystolic;
        }

        public int getAvgDiastolic() {
            return avgDiastolic;
        }

        public void setAvgDiastolic(int avgDiastolic) {
            this.avgDiastolic = avgDiastolic;
        }

        public String getWeekCount() {
            return weekCount;
        }

        public void setWeekCount(String weekCount) {
            this.weekCount = weekCount;
        }

        public int getMinSystolic() {
            return minSystolic;
        }

        public void setMinSystolic(int minSystolic) {
            this.minSystolic = minSystolic;
        }
    }
}
