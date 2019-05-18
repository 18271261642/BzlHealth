package com.bozlun.health.android.b30.bean;


import android.os.Handler;
import android.util.Log;
import org.litepal.LitePal;
import java.util.List;


/**
 * 数据库操作: B30半小时数据源
 *
 * @author XuBo 2018-09-19
 */
public class B30HalfHourDao {

    Handler handler = new Handler();

    /**
     * 单例
     */
    private static B30HalfHourDao mInstance;

    private B30HalfHourDao() {
    }

    /**
     * 获取单例
     */
    public static B30HalfHourDao getInstance() {
        if (mInstance == null) {
            mInstance = new B30HalfHourDao();
        }
        return mInstance;
    }

    /**
     * 数据源类型: 步数数据
     */
    public static final String TYPE_STEP = "step";
    /**
     * 数据源类型: 运动数据
     */
    public static final String TYPE_SPORT = "sport";
    /**
     * 数据源类型: 心率数据
     */
    public static final String TYPE_RATE = "rate";
    /**
     * 数据源类型: 血压数据
     */
    public static final String TYPE_BP = "bp";
    /**
     * 数据源类型: 睡眠数据
     */
    public static final String TYPE_SLEEP = "sleep";


   // List<TempB31HRVBean> resultList = new ArrayList<>();
    /**
     * 获取单条数据源
     *
     * @param address 手环MAC地址
     * @param date    日期
     * @param type    数据类型{@link #TYPE_STEP}
     * @return 数据源Json字符串
     */
    private B30HalfHourDB getOriginData(String address, String date, String type) {
        String where = "address = ? and date = ? and type = ?";
        List<B30HalfHourDB> resultList = LitePal.where(where, address, date, type).limit(1).find
                (B30HalfHourDB.class);// 一个类型,同一天只有一条数据
        return resultList == null || resultList.isEmpty() ? null : resultList.get(0);
    }

    /**
     * 查找单条数据源
     *
     * @param address 手环MAC地址
     * @param date    日期
     * @param type    数据类型{@link #TYPE_STEP}
     * @return 数据源Json字符串
     */
    public String findOriginData(String address, String date, String type) {
        B30HalfHourDB result = getOriginData(address, date, type);
        return result == null ? null : result.getOriginData();
    }




    /**
     * 保存(更新)单条数据源
     *
     * @param db 数据源实体类
     */
    public synchronized void saveOriginData(B30HalfHourDB db) {
        boolean result;
        String bMac = db.getAddress();
        String strDate = db.getDate();
        String type = db.getType();
        result = db.saveOrUpdate("address=? and date =? and type=?",bMac,strDate,type);
        Log.e("DB","--------数据存储="+result);

    }


    /**
     * 根据类型查找所有没上传服务器的数据源,不分日期
     *
     * @param address 手环MAC地址
     * @param type    数据类型{@link #TYPE_STEP}
     * @return 指定类型的, 没有上传服务器的所有数据源
     */
    public List<B30HalfHourDB> findNotUploadData(String address, String type) {
        String where = "upload = 0 and address = ? and type = ?";
//        String where = "address = ? and type = ?";
        return LitePal.where(where, address, type).find(B30HalfHourDB.class);
    }


    /**
     * 根据日期查询没有上传的数据,一个类型一天只有一条数据
     * @param bleMac mac地址
     * @param type 类型
     * @param dayStr 日期yyyy-mm-dd
     * @return
     */
    public List<B30HalfHourDB> findNotUpDataByDay(String bleMac,String type,String dayStr){
        List<B30HalfHourDB> stepDayList = LitePal.where("upload = 0 and address = ? and type = ? and date = ?",bleMac,type,dayStr).find(B30HalfHourDB.class);
        return stepDayList == null || stepDayList.isEmpty() ? null : stepDayList;
    }


    /**
     * 根据指定日期查询心率详细数据
     * @param dateStr 日期
     * @param bleMac mac地址
     * @param type 类型
     * @return
     */
    public List<B30HalfHourDB> findW30HeartDetail(String dateStr,String bleMac,String type ){
        List<B30HalfHourDB> w30HeartDbList = LitePal.where("address = ? and date = ? and type = ?",bleMac,dateStr,type).find(B30HalfHourDB.class);
        return w30HeartDbList == null || w30HeartDbList.isEmpty() ? null : w30HeartDbList;
    }


    /**
     * 根据指定日期查询睡眠数据，一天只有一条
     * @param dateStr 日期
     * @param bleMac mac地址
     * @param type 类型
     * @return
     */
    public List<B30HalfHourDB> findW30SleepDetail(String dateStr,String bleMac,String type){
        List<B30HalfHourDB> w30SleepDbList = LitePal.where("address = ? and date = ? and type = ?",bleMac,dateStr,type).find(B30HalfHourDB.class);
        return w30SleepDbList == null || w30SleepDbList.isEmpty() ? null : w30SleepDbList;
    }

    /**
     * 根据指定日期查询w30保存的数据
     * @param dateStr 日期
     * @param bleMac 地址
     * @return B30HalfHourDB
     */
    public List<B30HalfHourDB> findW30SportData(String dateStr,String bleMac,String type){
        List<B30HalfHourDB> w30SportDbList = LitePal.where("address = ? and date = ? and type = ?",bleMac,dateStr,type).find(B30HalfHourDB.class);
        return w30SportDbList == null || w30SportDbList.isEmpty() ? null : w30SportDbList;
    }


}
