package com.bozlun.health.android.friend.bean;

import java.util.List;

public class FrendHaretBean {
    /**
     * friendHeartRate : [{"rtc":"2018-11-26 00:00","heartRate":78},{"rtc":"2018-11-26 00:05","heartRate":65},{"rtc":"2018-11-26 01:00","heartRate":0},{"rtc":"2018-11-26 07:30","heartRate":0},{"rtc":"2018-11-26 07:50","heartRate":65},{"rtc":"2018-11-26 11:26","heartRate":0},{"rtc":"2018-11-26 14:09","heartRate":78},{"rtc":"2018-11-26 16:30","heartRate":65}]
     * resultCode : 001
     */

    private String resultCode;
    private List<FriendHeartRateBean> friendHeartRate;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public List<FriendHeartRateBean> getFriendHeartRate() {
        return friendHeartRate;
    }

    public void setFriendHeartRate(List<FriendHeartRateBean> friendHeartRate) {
        this.friendHeartRate = friendHeartRate;
    }

    public static class FriendHeartRateBean {
        /**
         * rtc : 2018-11-26 00:00
         * heartRate : 78
         */

        private String time;
        private int heartRate;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getHeartRate() {
            return heartRate;
        }

        public void setHeartRate(int heartRate) {
            this.heartRate = heartRate;
        }
    }

//    /**
//     * friendHeartRate : [{"date":"2018-11-21 00:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7916984,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":58,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 00:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":70,"id":7990161,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 01:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990137,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 01:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":70,"id":7990162,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 02:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990139,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":36,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 02:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":59,"id":7990163,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 03:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990138,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":133,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 03:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":68,"id":7990164,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 04:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990140,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":79,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 04:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":58,"id":7990165,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 05:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990141,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":73,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 05:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":55,"id":7990166,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 06:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990142,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 06:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":59,"id":7990167,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 07:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990143,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 07:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":85,"id":7990168,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 08:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990144,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":977,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 08:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":73,"id":7990169,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 09:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990145,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":235,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 09:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":73,"id":7990170,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 10:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990146,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":26,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 10:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":85,"id":7990171,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 11:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990147,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 11:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":81,"id":7990172,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 12:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990148,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":160,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 12:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990173,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 13:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990149,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":739,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 13:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990174,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 14:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990150,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":627,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 14:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990175,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 15:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990151,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":166,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 15:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990176,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 16:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990152,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 16:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990177,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 17:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990153,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":281,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 17:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990178,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 18:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990154,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":29,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 18:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990179,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 19:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990155,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 19:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990180,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 20:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990156,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 20:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990181,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 21:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990157,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 21:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990182,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 22:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990158,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 22:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990183,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 23:00","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990159,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0},{"date":"2018-11-21 23:30","bloodOxygen":0,"systolic":0,"diastolic":0,"heartRate":0,"id":7990184,"deviceCode":"DA:7D:A8:9B:B4:5E","stepNumber":0,"userId":"d3546a77d5bb44d2805c6bf40508ad2e","status":0}]
//     * resultCode : 001
//     */
//
//    private String resultCode;
//    private List<FriendHeartRateBean> friendHeartRate;
//
//    public String getResultCode() {
//        return resultCode;
//    }
//
//    public void setResultCode(String resultCode) {
//        this.resultCode = resultCode;
//    }
//
//    public List<FriendHeartRateBean> getFriendHeartRate() {
//        return friendHeartRate;
//    }
//
//    public void setFriendHeartRate(List<FriendHeartRateBean> friendHeartRate) {
//        this.friendHeartRate = friendHeartRate;
//    }
//
//    public static class FriendHeartRateBean {
//        /**
//         * date : 2018-11-21 00:00
//         * bloodOxygen : 0
//         * systolic : 0
//         * diastolic : 0
//         * heartRate : 0
//         * id : 7916984
//         * deviceCode : DA:7D:A8:9B:B4:5E
//         * stepNumber : 58
//         * userId : d3546a77d5bb44d2805c6bf40508ad2e
//         * status : 0
//         */
//
//        private String date;
//        private int bloodOxygen;
//        private int systolic;
//        private int diastolic;
//        private int heartRate;
//        private int id;
//        private String deviceCode;
//        private int stepNumber;
//        private String userId;
//        private int status;
//
//        public String getDate() {
//            return date;
//        }
//
//        public void setDate(String date) {
//            this.date = date;
//        }
//
//        public int getBloodOxygen() {
//            return bloodOxygen;
//        }
//
//        public void setBloodOxygen(int bloodOxygen) {
//            this.bloodOxygen = bloodOxygen;
//        }
//
//        public int getSystolic() {
//            return systolic;
//        }
//
//        public void setSystolic(int systolic) {
//            this.systolic = systolic;
//        }
//
//        public int getDiastolic() {
//            return diastolic;
//        }
//
//        public void setDiastolic(int diastolic) {
//            this.diastolic = diastolic;
//        }
//
//        public int getHeartRate() {
//            return heartRate;
//        }
//
//        public void setHeartRate(int heartRate) {
//            this.heartRate = heartRate;
//        }
//
//        public int getId() {
//            return id;
//        }
//
//        public void setId(int id) {
//            this.id = id;
//        }
//
//        public String getDeviceCode() {
//            return deviceCode;
//        }
//
//        public void setDeviceCode(String deviceCode) {
//            this.deviceCode = deviceCode;
//        }
//
//        public int getStepNumber() {
//            return stepNumber;
//        }
//
//        public void setStepNumber(int stepNumber) {
//            this.stepNumber = stepNumber;
//        }
//
//        public String getUserId() {
//            return userId;
//        }
//
//        public void setUserId(String userId) {
//            this.userId = userId;
//        }
//
//        public int getStatus() {
//            return status;
//        }
//
//        public void setStatus(int status) {
//            this.status = status;
//        }
//    }



}
