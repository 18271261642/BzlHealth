package com.bozlun.health.android.b30.b30view;

import android.content.Context;
import android.graphics.Paint;

/**
 *
 */

public class DimenUtil {
    /**
     * dp转px
     */
    public static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * px转dp
     */
    public static float px2dp(Context context, int px) {
        return (px / context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 将sp值转换为px值
     */
    public static int sp2px(Context context, float sp) {
        return (int) (sp * context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    /**
     * 获取文字的宽度
     *
     * @param paint
     * @param text
     * @return
     */
    public static int getTextWidth(Paint paint, String text) {
        return (int) paint.measureText(text);
    }
}