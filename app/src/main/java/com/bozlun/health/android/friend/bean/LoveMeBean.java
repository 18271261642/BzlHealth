package com.bozlun.health.android.friend.bean;

import java.util.List;

public class LoveMeBean {
    /**
     * resultCode : 001
     * friendList : [{"birthday":"1992-12-25","image":"http://47.90.83.197/image/2018/11/24/1543020102182.png","addTime":"2017-05-09 14:34:35","nickName":"我难道不是你的小可爱？","sex":"M","weight":"70","equipment":"W30","updateTime":"2018-12-05 11:12","lon":"113.721745","type":0,"userId":"8c4c511a45374bb595e6fdf30bb878b7","phone":"18738546101","id":3478,"pwd":"e10adc3949ba59abbe56e057f20f883e","lat":"22.990020","height":"180","status":0}]
     */

    private String resultCode;
    private List<FriendListBean> friendList;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public List<FriendListBean> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<FriendListBean> friendList) {
        this.friendList = friendList;
    }

    public static class FriendListBean {
        /**
         * birthday : 1992-12-25
         * image : http://47.90.83.197/image/2018/11/24/1543020102182.png
         * addTime : 2017-05-09 14:34:35
         * nickName : 我难道不是你的小可爱？
         * sex : M
         * weight : 70
         * equipment : W30
         * updateTime : 2018-12-05 11:12
         * lon : 113.721745
         * type : 0
         * userId : 8c4c511a45374bb595e6fdf30bb878b7
         * phone : 18738546101
         * id : 3478
         * pwd : e10adc3949ba59abbe56e057f20f883e
         * lat : 22.990020
         * height : 180
         * status : 0
         */

        private String birthday;
        private String image;
        private String addTime;
        private String nickName;
        private String sex;
        private String weight;
        private String equipment;
        private String updateTime;
        private String lon;
        private int type;
        private String userId;
        private String phone;
        private int id;
        private String pwd;
        private String lat;
        private String height;
        private int status;

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getAddTime() {
            return addTime;
        }

        public void setAddTime(String addTime) {
            this.addTime = addTime;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getEquipment() {
            return equipment;
        }

        public void setEquipment(String equipment) {
            this.equipment = equipment;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
