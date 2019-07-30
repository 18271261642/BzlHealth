package com.bozlun.health.android.bean;

/**
 * 后台返回格式bean
 * Created by Admin
 * Date 2019/7/22
 */
public class ResuleServerBean {

    private int code;

    private String msg;

    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResuleServerBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
