package com.bozlun.health.android.siswatch.utils;

/**
 * Created by Admin
 * Date 2018/12/5
 */
public class DateTimeUtils {


    static String currDate = WatchUtils.getCurrentDate();

    /**
     * 获取当前日期的年
     * @return
     */
    public static int getCurrYear(){
        return  Integer.valueOf(!WatchUtils.isEmpty(currDate) ? currDate.substring(0, 4).trim() : "");
    }

    /**
     * 获取指定日期的年
     * @return
     */
    public static int getCurrYear(String str){
        return  Integer.valueOf(!WatchUtils.isEmpty(str) ? str.substring(0, 4).trim() : "");
    }



    //获取当前日期的月
    public static int getCurrMonth(){
        return  Integer.valueOf(!WatchUtils.isEmpty(currDate) ? currDate.substring(5, 7).trim() : "");
    }


    //获取指定期的月
    public static int getCurrMonth(String str){
        return  Integer.valueOf(!WatchUtils.isEmpty(str) ? str.substring(5, 7).trim() : "");
    }

    //获取当前日期的天
    public static int getCurrDay(){
        return  Integer.valueOf(!WatchUtils.isEmpty(currDate) ? currDate.substring(8, currDate.length()).trim() : "");
    }


    //获取指定日期的天
    public static int getCurrDay(String str){
        return  Integer.valueOf(!WatchUtils.isEmpty(str) ? str.substring(8, str.length()).trim() : "");
    }




}
