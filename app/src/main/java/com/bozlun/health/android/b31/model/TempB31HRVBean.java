package com.bozlun.health.android.b31.model;

import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * Created by Admin
 * Date 2019/2/15
 */
public class TempB31HRVBean extends LitePalSupport {
    private String strCurrDay;
    private String b31HRVBeanStr;
    private List<TempHrvBean> hrvBeanList;

    public String getStrCurrDay() {
        return strCurrDay;
    }

    public void setStrCurrDay(String strCurrDay) {
        this.strCurrDay = strCurrDay;
    }

    public String getB31HRVBeanStr() {
        return b31HRVBeanStr;
    }

    public void setB31HRVBeanStr(String b31HRVBeanStr) {
        this.b31HRVBeanStr = b31HRVBeanStr;
    }

    public List<TempHrvBean> getHrvBeanList() {
        return hrvBeanList;
    }

    public void setHrvBeanList(List<TempHrvBean> hrvBeanList) {
        this.hrvBeanList = hrvBeanList;
    }

    @Override
    public String toString() {
        return "TempB31HRVBean{" +
                "strCurrDay='" + strCurrDay + '\'' +
                ", b31HRVBeanStr='" + b31HRVBeanStr + '\'' +
                ", hrvBeanList=" + hrvBeanList +
                '}';
    }

    class TempHrvBean extends LitePalSupport{
        private String spo2hOriginData;

        public String getSpo2hOriginData() {
            return spo2hOriginData;
        }

        public void setSpo2hOriginData(String spo2hOriginData) {
            this.spo2hOriginData = spo2hOriginData;
        }

        @Override
        public String toString() {
            return "TempHrvBean{" +
                    "spo2hOriginData='" + spo2hOriginData + '\'' +
                    '}';
        }
    }
}
