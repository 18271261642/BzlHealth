package com.bozlun.health.android.b30.bean;


import android.os.Handler;
import android.util.Log;
import com.bozlun.health.android.MyApp;
import com.bozlun.health.android.b31.model.B31HRVBean;
import com.bozlun.health.android.b31.model.B31Spo2hBean;
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


    public void findBetweenStep(String address, String date, String type){

        String startDate = "2019-03-01";
        String endDate = "2019-03-04";
        String mac = MyApp.getInstance().getMacAddress();
        //List<B30HalfHourDB> ltH = LitePal.where("date  between  ? and ?"+startDate+endDate).find(B30HalfHourDB.class);
        String where = "dateStr = ? ";
        List<B31HRVBean> resultList = LitePal.where(where,date).find(B31HRVBean.class);

        if (resultList == null) {
            for(B31HRVBean bd : resultList){
                Log.e("DB","--------bd="+bd.toString());
            }
        } else {
            Log.e("DB","------------数据查询为null---");
        }



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

//
//        B30HalfHourDB localData = getOriginData(db.getAddress(), db.getDate(), db.getType());
//        if (localData == null) {
//            result = db.save();// 本地没有,就直接新增
//            MyLogUtil.d("bobo", "saveOriginData type: " + db.getType() + ",date:"
//                    + db.getDate() + ",result:" + result + ",add:"+ db.getUpload());
//        } else {
//            //localData.setOriginData(db.getOriginData());
//            result = localData.saveOrUpdate("address=? and date =? and type=?",bMac,strDate,type);//.save();// 本地有,就更新本地
////            MyLogUtil.d("bobo", "saveOriginData type: " + db.getType() + ",date:"
////                    + db.getDate() + ",result:" + result + ",update:" + localData.getUpload());
//        }
    }


    //保存B31HRV的数据
    public synchronized void saveB31HRVData(final B31HRVBean db) {
        final String where = "bleMac = ? and dateStr = ? and currHrvDate = ?";
//        db.saveOrUpdateAsync(where,db.getBleMac(),db.getDateStr(),db.getCurrHrvDate()).listen(new SaveCallback() {
//            @Override
//            public void onFinish(boolean success) {
//                Log.e("DB","--hrc-----存储结果="+success);
//            }
//        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                db.saveOrUpdate(where,db.getBleMac(),db.getDateStr(),db.getCurrHrvDate());
//            }
//        }).start();

        //db.saveOrUpdate(where,db.getBleMac(),db.getDateStr(),db.getCurrHrvDate());

       // B31HRVBean localData = getHRVOriginData(db.getBleMac(), db.getDateStr(),db.getCurrHrvDate());




//        if(localData == null){
//
//            TempB31HRVBean tempB31HRVBean = new TempB31HRVBean();
//            tempB31HRVBean.setStrCurrDay(db.getDateStr());
//            tempB31HRVBean.setB31HRVBean(db);
//            resultList.add(tempB31HRVBean);
//            LitePal.saveAll(resultList);
//
//
//           // LitePal.findAll(B31HRVBean.class);
//        }



//        if(localData == null){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    db.save();
//                }
//            }).start();
//        }




//        boolean result;
//        B31HRVBean localData = getHRVOriginData(db.getBleMac(), db.getDateStr(),db.getCurrHrvDate());
//        //Log.e("DB","-------localData="+localData.toString());
//        if (localData == null) {
//            db.saveAsync();
////             db.saveAsync().listen(new SaveCallback() {
////                 @Override
////                 public void onFinish(boolean success) {
////                     Log.e("DB","-------存储结果="+success);
////                 }
////             });
//           // LitePal.saveAll();
//            //result = db.save();// 本地没有,就直接新增
////            MyLogUtil.d("bobo", "date:"
////                    + db.getDateStr() + " ,result:"+result );
//        }
//        else {
//            localData.setHrvDataStr(db.getHrvDataStr());
//            result = localData.save();// 本地有,就更新本地
//            MyLogUtil.d("bobo", "date:"
//                    + db.getDateStr() + ",result:" + result );
//        }
    }


    //判断HRV是否已经保存
    private B31HRVBean getHRVOriginData(String address, String date,String currDay) {
        String where = "bleMac = ? and dateStr = ? and currHrvDate = ?";
        List<B31HRVBean> resultList = LitePal.where(where, address, date,currDay).find
                (B31HRVBean.class);// 一个类型,同一天只有一条数据
        return resultList == null || resultList.isEmpty() ? null : resultList.get(0);
    }


    //保存B31血氧的数据
    public synchronized void saveB31Spo2hData(final B31Spo2hBean db) {
        //Log.e("DB","------db="+db.getSpo2currDate());
        final String where = "bleMac = ? and dateStr = ? and spo2currDate = ?";
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                db.saveOrUpdate(where,db.getBleMac(),db.getDateStr(),db.getSpo2currDate());
//            }
//        };
//        runnable.run();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                db.saveOrUpdate(where,db.getBleMac(),db.getDateStr(),db.getSpo2currDate());
//            }
//        }).start();
//        db.saveOrUpdateAsync(where,db.getBleMac(),db.getDateStr(),db.getSpo2currDate())
//                .listen(new SaveCallback() {
//            @Override
//            public void onFinish(boolean success) {
//                Log.e("DB","-----------血氧存储结果="+success);
//            }
//        });

        //db.saveOrUpdate(where,db.getBleMac(),db.getDateStr(),db.getSpo2currDate());



//        B31Spo2hBean localData = getSpo2hOriginData(db.getBleMac(), db.getDateStr(),db.getSpo2currDate());
//        if(localData == null){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    db.save();
//                }
//            }).start();
//        }



//        boolean result;
//        B31Spo2hBean localData = getSpo2hOriginData(db.getBleMac(), db.getDateStr(),db.getSpo2currDate());
//        if (localData == null) {
//            db.saveAsync();
////            db.saveAsync().listen(new SaveCallback() {
////                @Override
////                public void onFinish(boolean success) {
////                    Log.e("DB","-----------血氧存储结果="+success);
////                }
////            });
////            result =  db.save();// 本地没有,就直接新增
////            Log.e("DB","--------数据库中没有="+result);
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    db.save();// 本地没有,就直接新增
////                }
////            }).start();
//        }else{
//
//            Log.e("DB","----------else="+localData.toString());
//        }
//        else {
//            localData.setSpo2hOriginData(db.getSpo2hOriginData());
//            result = localData.save();
//            Log.e("DB","--------数据库中有="+result);
//        }
    }

    //判断血氧是否已经保存
    private B31Spo2hBean getSpo2hOriginData(String address, String date,String currDate) {
        String where = "bleMac = ? and dateStr = ? and spo2currDate = ?";
        List<B31Spo2hBean> resultBLt = LitePal.where(where,address,date,currDate).find(B31Spo2hBean.class);
        return  resultBLt == null || resultBLt.isEmpty() ? null : resultBLt.get(0);

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

}
