package com.vp.cso.hrvreport;

/**
 * Created by zhangchong on 2018/3/2.
 */
public class JNIChange {
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native int[] hrvAnalysisReport(int[] dataBuffer, int length);
    public native int ecgFilter(int[] ecgData,
                                int[] outData,
                                int Fs, int N, int Fc1, int Fc2, float beta,
                                int filterLength, int opol, int smooth_points, int smooth_times);
}