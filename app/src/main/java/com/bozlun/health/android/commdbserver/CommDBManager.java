package com.bozlun.health.android.commdbserver;

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

    private volatile static CommDBManager commDBManager = null;

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
     * @param bleName
     * @param bleMac
     * @param dataStr
     * @param countStep
     */
    public void saveCommCountStepDate(String bleName,String bleMac,String dataStr,int countStep){
        String whereStr = "date = ? and devicecode = ?";
        CommStepCountDb commStepCountDb = new CommStepCountDb();
        commStepCountDb.setUserid((String) SharedPreferencesUtils.readObject(MyApp.getContext(),Commont.USER_ID_DATA));
        commStepCountDb.setCount(1);
        commStepCountDb.setStepnumber(countStep);
        commStepCountDb.setDate(dataStr);
        commStepCountDb.setDevicecode(bleMac);
        commStepCountDb.setBleName(bleName);
        //int currStep = LitePal.where(whereStr).limit(0).find(CommStepCountDb.class).get(0).getStepnumber();
        //有就修改，没有就保存
        commStepCountDb.saveOrUpdate(whereStr,dataStr,bleMac);
    }

    /**
     * 保存所有的数据，用于从后台下载数据后全部保存
     * @param commStepCountDbList
     */
    public void saveAllCommCountStepDate(List<CommStepCountDb> commStepCountDbList){
        LitePal.saveAll(commStepCountDbList);
    }








}
