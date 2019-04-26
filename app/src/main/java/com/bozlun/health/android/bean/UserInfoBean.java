package com.bozlun.health.android.bean;


import java.io.Serializable;

/**
 * Created by Administrator on 2018/7/26.
 */

public class UserInfoBean implements Serializable {


    /**
     * {"birthday":"1994-07-05","image":"http:\/\/47.90.83.197\/image\/2018\/12\/27\/1545915697212.jpg",
     * "nickName":"超级管理员","sex":"F","weight":"60","equipment":"H9","userId":"5c2b58f0681547a0801d4d4ac8465f82",
     * "mac":"CF:CB:41:3C:AC:4E","phone":"15916947377","height":"170"}
     */

    private String birthday;
    private String image;
    private String nickName;
    private String sex;
    private String weight;
    private String equipment;
    private String mac;
    private String userId;
    private String phone;
    private String height;

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    @Override
    public String toString() {
        return "UserInfoBean{" +
                "birthday='" + birthday + '\'' +
                ", image='" + image + '\'' +
                ", nickName='" + nickName + '\'' +
                ", sex='" + sex + '\'' +
                ", weight='" + weight + '\'' +
                ", equipment='" + equipment + '\'' +
                ", mac='" + mac + '\'' +
                ", userId='" + userId + '\'' +
                ", phone='" + phone + '\'' +
                ", height='" + height + '\'' +
                '}';
    }
}
