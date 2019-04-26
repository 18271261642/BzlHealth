package com.bozlun.health.android.commdbserver;


import android.util.Log;
import com.bozlun.health.android.Commont;
import com.bozlun.health.android.MyApp;
import com.suchengkeji.android.w30sblelibrary.utils.SharedPreferencesUtils;
import org.litepal.LitePal;
import java.util.List;

/**
 * Created by Admin
 * Date 2019/3/4
 * 数据库的管理
 */
public class CommDBManager {

    private static final String TAG = "CommDBManager";

    private volatile static CommDBManager commDBManager = null;

    //根据设备地址和日期查询的条件
    String commDeviceWhere = "devicecode = ? and date = ?";

    private CommDBManager() {
    }

    public static CommDBManager getCommDBManager(){
        if(commDBManager == null){
            synchronized (CommDBManager.class){
                if(commDBManager == null)
                    commDBManager = new CommDBManager();
            }
        }
        return commDBManager;
    }


    /**
     * 保存总步数,一天只有一天数据
     * @param
     * @param bleMac
     * @param dataStr
     * @param countStep
     */
    public void saveCommCountStepDate(String bleName,String bleMac,String dataStr,int countStep){
        CommStepCountDb commStepCountDb = new CommStepCountDb();
        commStepCountDb.setUserid((String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA));
        commStepCountDb.setCount(1);
        commStepCountDb.setStepnumber(countStep);
        commStepCountDb.setDate(dataStr);
        commStepCountDb.setDevicecode(bleMac);
        commStepCountDb.setBleName(bleName);
        commStepCountDb.setUpload(false);
        //int currStep = LitePal.where(whereStr).limit(0).find(CommStepCountDb.class).get(0).getStepnumber();
        //有就修改，没有就保存
        boolean isSave = commStepCountDb.saveOrUpdate(commDeviceWhere,bleMac,bleMac);
        Log.e(TAG,"----------isSave="+isSave);
    }

    /**
     * 总步数保存所有的数据，用于从后台下载数据后全部保存
     * @param commStepCountDbList
     */
    public void saveAllCommCountStepDate(List<CommStepCountDb> commStepCountDbList){
        LitePal.saveAll(commStepCountDbList);
    }

    /**
     * 步数查询需要上传的数据
     * @param bMac
     * @param dateStr 指定天数
     * @return
     */
    public List<CommStepCountDb> findCountStepForUpload(String bMac, String dateStr) {
        List<CommStepCountDb> commStepCountDbList = LitePal.where(commDeviceWhere, bMac, dateStr).find(CommStepCountDb.class);

        return (commStepCountDbList == null || commStepCountDbList.isEmpty()) ? null : commStepCountDbList;
    }

    /**
     * 步数查询开始和结束日期的数据
     * @param bMac
     * @param startDay 开始日期
     * @param endDay    结束日期
     * @return
     */
    public List<CommStepCountDb> findCountStepForUpload(String bMac, String startDay,String endDay) {
        String where = "devicecode = ? and date  between ? and ?";
        List<CommStepCountDb> commStepCountDbList = LitePal.where(where, bMac, startDay,endDay).find(CommStepCountDb.class);

        return (commStepCountDbList == null || commStepCountDbList.isEmpty()) ? null : commStepCountDbList;
    }


    public void updateCountStepData(CommStepCountDb commStepCountDb,boolean isUpload){
        commStepCountDb.setUpload(isUpload);
        commStepCountDb.save();
    }


    //*******************心率数据 ************************************


    /**
     * 保存心率数据
     * @param bleName
     * @param bleMac
     * @param dataStr
     * @param avgHeart
     */
    public void saveCommHeartData(String bleName,String bleMac,String dataStr,int avgHeart){
        CommHeartDb commHeartDb = new CommHeartDb();
        commHeartDb.setBleName(bleName);
        commHeartDb.setUserid((String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA));
        commHeartDb.setDevicecode(bleMac);
        commHeartDb.setAvgheartrate(avgHeart);
        commHeartDb.setRtc(dataStr);
        commHeartDb.setMaxheartrate(0);
        commHeartDb.setMinheartrate(0);
        boolean isHeartSave = commHeartDb.saveOrUpdate(commDeviceWhere,bleMac,dataStr);
        Log.e(TAG,"--------心率保存="+isHeartSave);
    }


    /**
     * 批量保存心率数据
     * @param heartList
     */
    public void saveAllCommHeartData(List<CommHeartDb> heartList){
        LitePal.saveAll(heartList);
    }



    /**
     * 查询指定日期的心率数据
     * @param bMac
     * @param dateStr 指定的日期
     * @return
     */
    public List<CommHeartDb> findCommHeartForUpload(String bMac, String dateStr){
        List<CommHeartDb> commHeartDbList = LitePal.where(commDeviceWhere,bMac,dateStr).find(CommHeartDb.class);
        return (commHeartDbList == null || commHeartDbList.isEmpty()) ? null : commHeartDbList;
    }

    /**
     * 查询开始和结束日期期间内的心率数据
     * @param bMac
     * @param startDay
     * @param endDay
     * @return
     */
    public List<CommHeartDb> findCommHeartForUpload(String bMac, String startDay,String endDay){
        String where = "devicecode = ? and date  between ? and ?";
        List<CommHeartDb> commHeartDbList = LitePal.where(where,bMac,startDay,endDay).find(CommHeartDb.class);
        return (commHeartDbList == null || commHeartDbList.isEmpty()) ? null : commHeartDbList;
    }


    /****************睡眠********************/

    /**
     * 保存睡眠数据
     * @param bleName
     * @param bleMac
     * @param dataStr 日期
     * @param deep 深睡时长
     * @param low 浅睡时长
     * @param sober 清醒时长
     * @param allSleep 所有睡眠时间
     * @param sleeptime 入睡时间
     * @param waketime  清醒时间
     * @param wakeCount 清醒次数
     */
    public void saveCommSleepDbData(String bleName,String bleMac,String dataStr,int deep,
                                    int low,int sober,int allSleep,String sleeptime,String waketime,int wakeCount){
        CommSleepDb commSleepDb = new CommSleepDb();
        commSleepDb.setBleName(bleName);
        commSleepDb.setDevicecode(bleMac);
        commSleepDb.setRtc(dataStr);
        commSleepDb.setUserid((String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA));
        commSleepDb.setDeepsleep(deep);
        commSleepDb.setShallowsleep(low);   //浅睡
        commSleepDb.setSleeplen(allSleep);  //睡眠总时长
        commSleepDb.setSoberlen(sober);  //清醒时时长
        commSleepDb.setSleeptime(sleeptime);    //入睡时间
        commSleepDb.setWaketime(waketime);  //清醒时间
        commSleepDb.setWakecount(wakeCount);    //清醒次数

        boolean isSleepSave = commSleepDb.saveOrUpdate(commDeviceWhere,bleMac,dataStr);
        Log.d(TAG,"---------睡眠保存="+isSleepSave);

    }

    /**
     * 批量保存睡眠数据
     * @param sleepDbList
     */
    public void saveAllCommSleepData(List<CommSleepDb> sleepDbList){
        LitePal.saveAll(sleepDbList);
    }

    /**
     * 查询指定日期的睡眠数据
     * @param bMac
     * @param dateStr
     * @return
     */
    public List<CommSleepDb> findCommSleepForUpload(String bMac, String dateStr){
        List<CommSleepDb> commSleepDbList = LitePal.where(commDeviceWhere,bMac,dateStr).find(CommSleepDb.class);
        return (commSleepDbList == null || commSleepDbList.isEmpty()) ? null : commSleepDbList;
    }


    /**
     * 查询时间段的睡眠数据
     * @param bMac
     * @param startDay
     * @param endDay
     * @return
     */
    public List<CommSleepDb> findCommSleepForUpload(String bMac, String startDay,String endDay){
        String where = "devicecode = ? and date  between ? and ?";
        List<CommSleepDb> commSleepDbList = LitePal.where(where,bMac,startDay,endDay).find(CommSleepDb.class);
        return (commSleepDbList == null || commSleepDbList.isEmpty()) ? null : commSleepDbList;

    }





    /*********************血压****************/

    /**
     * 保存血压数据
     * @param bleMac
     * @param dateStr
     * @param maxDiastolic
     * @param minSystolic
     * @param avgDiastolic
     * @param avgSystolic
     */
    public void saveCommBloodDb(String bleMac,String dateStr,int maxDiastolic,int minSystolic,int avgDiastolic,int avgSystolic){
        CommBloodDb commBloodDb = new CommBloodDb();
        commBloodDb.setUserid((String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA));
        commBloodDb.setRtc(dateStr);
        commBloodDb.setDevicecode(bleMac);
        commBloodDb.setMaxdiastolic(maxDiastolic); //舒张压
        commBloodDb.setMaxdiastolic(minSystolic);  //收缩压
        commBloodDb.setAvgdiastolic(avgDiastolic);  //平均的舒张压
        commBloodDb.setAvgsystolic(avgSystolic);    //平均收缩压
        boolean isBloodSave = commBloodDb.saveOrUpdate(commDeviceWhere,bleMac,dateStr);

    }


    /**
     * 查询指定日期的血压数据
     * @param bleMac
     * @param dateStr
     * @return
     */
    public List<CommBloodDb> findCommBloodForUpload(String bleMac,String dateStr){
        List<CommBloodDb> commBloodDbs = LitePal.where(commDeviceWhere,bleMac,dateStr).find(CommBloodDb.class);
        return commBloodDbs == null || commBloodDbs.isEmpty() ? null : commBloodDbs;
    }

    /**
     * 查询时间段的血压数据
     * @param bleMac
     * @param startDay
     * @param endDay
     * @return
     */
    public List<CommBloodDb> findCommBloodForUpload(String bleMac,String startDay,String endDay){
        String where = "devicecode = ? and date  between ? and ?";
        List<CommBloodDb> commBloodDbs = LitePal.where(where,bleMac,startDay,endDay).find(CommBloodDb.class);
        return commBloodDbs == null || commBloodDbs.isEmpty() ? null : commBloodDbs;
    }


    /**
     * 保存血氧的数据
     * @param bleName
     * @param bleMac
     * @param dateStr
     * @param avgBloodoxygen
     */
    public void saveCommBloodOxyDbData(String bleName,String bleMac,String dateStr,int avgBloodoxygen){
        CommBloodOxyDb commBloodOxyDb = new CommBloodOxyDb();
        commBloodOxyDb.setUserid((String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA));
        commBloodOxyDb.setBleName(bleName);
        commBloodOxyDb.setRtc(dateStr);
        commBloodOxyDb.setDevicecode(bleMac);
        commBloodOxyDb.setMaxbloodoxygen(0);
        commBloodOxyDb.setMinbloodoxygen(0);
        commBloodOxyDb.setAvgbloodoxygen(avgBloodoxygen);
        boolean isBloodOxySave = commBloodOxyDb.saveOrUpdate(commDeviceWhere,bleMac,dateStr);
    }
//15591061973
    //17724491524   123456789shao

}
