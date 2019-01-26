package com.bozlun.health.android.friend.bean;

public class FindByPhoneBean {

    /**
     * userInfo : {"birthday":"1995-06-15","image":"http://47.90.83.197/image/2018/11/07/1541586772350.jpg","nickName":"手机","sex":"M","weight":"60 kg","userId":"0d56716e5629475882d4f4bfc7c51420","friendStatus":0,"phone":"14791685830","height":"170 cm"}
     * resultCode : 001
     */

    private UserInfoBean userInfo;
    private String resultCode;

    public UserInfoBean getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoBean userInfo) {
        this.userInfo = userInfo;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public static class UserInfoBean {
        /**
         * birthday : 1995-06-15
         * image : http://47.90.83.197/image/2018/11/07/1541586772350.jpg
         * nickName : 手机
         * sex : M
         * weight : 60 kg
         * userId : 0d56716e5629475882d4f4bfc7c51420
         * friendStatus : 0
         * phone : 14791685830
         * height : 170 cm
         */

        private String birthday;
        private String image;
        private String nickName;
        private String sex;
        private String weight;
        private String userId;
        private int friendStatus;
        private String phone;
        private String height;

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

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getFriendStatus() {
            return friendStatus;
        }

        public void setFriendStatus(int friendStatus) {
            this.friendStatus = friendStatus;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }
    }
}
