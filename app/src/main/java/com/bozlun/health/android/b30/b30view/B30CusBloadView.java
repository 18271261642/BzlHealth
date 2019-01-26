package com.bozlun.health.android.b30.b30view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

import com.bozlun.health.android.R;

import java.util.Map;

/**
 * Created by Administrator on 2018/8/4.
 */

public class B30CusBloadView extends View {

    //低压的画笔
    private Paint lowPaint;
    //高压的画笔
    private Paint highPaint;
    //连线的画笔
    private Paint linPaint;
    //绘制日期的画笔
    private Paint timePaint;
    //绘制无数据时显示的txt
    private Paint emptyPaint;


    //低压画笔的颜色
    private int lowColor;
    //高压画笔的颜色
    private int hightColor;
    //连线的画笔颜色
    private int linColor;
    //日期的画笔颜色
    private int timeColor;

    //画刻度尺的画笔
    private Paint scalePaint;

    /**
     * 是否绘制刻度和横线
     */
    private boolean isScale = false;

    /**
     * 画笔大小:线,时间,圆半径
     */
    private int linStroke, timeStroke, radioStroke;
    /**
     * 点列表
     */
    private SparseArray<Point> pointList = new SparseArray<>();
    /**
     * 左边空出来,右边空出来,可用宽度
     */
    private int valStart, valRight, valWidth;
    /**
     * 高度比例
     */
    private float ratio;

    /**
     * 时间刻度
     */
    private final String[] timeStr = new String[]{"00:00", "03:00", "06:00", "09:00", "12:00",
            "15:00", "18:00", "21:00", "23:59"};

    public B30CusBloadView(Context context) {
        super(context);
    }

    public B30CusBloadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public B30CusBloadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.B30CusBloadView);
        if (typedArray != null) {
            lowColor = typedArray.getColor(R.styleable.B30CusBloadView_lowPointColor, 0);
            hightColor = typedArray.getColor(R.styleable.B30CusBloadView_highPointColor, 0);
            linColor = typedArray.getColor(R.styleable.B30CusBloadView_linPaintColor, 0);
            timeColor = typedArray.getColor(R.styleable.B30CusBloadView_timeColor, 0);
            linStroke = typedArray.getDimensionPixelSize(R.styleable.B30CusBloadView_linStroke, dp2px(1));
            timeStroke = typedArray.getDimensionPixelSize(R.styleable.B30CusBloadView_timeStroke, sp2px(10));
            radioStroke = typedArray.getDimensionPixelSize(R.styleable.B30CusBloadView_radioStroke, dp2px(2));
            typedArray.recycle();
        }
        initPaint();
    }

    private void initPaint() {
        valRight = dp2px(5);

        lowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lowPaint.setColor(lowColor);
        lowPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        highPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highPaint.setColor(hightColor);
        highPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        linPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linPaint.setColor(linColor);
        linPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linPaint.setStrokeWidth(linStroke);

        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(timeColor);
        timePaint.setTextSize(timeStroke);
        timePaint.setTextAlign(Paint.Align.LEFT);

        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setTextAlign(Paint.Align.CENTER);
        emptyPaint.setColor(timeColor);
        emptyPaint.setTextSize(timeStroke);

        scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scalePaint.setStrokeWidth(linStroke);
        scalePaint.setColor(timeColor);
        scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0, getHeight());
        canvas.save();
        valStart = isScale ? dp2px(18) : dp2px(5);
        valWidth = getWidth() - valStart - valRight;
        ratio = (float) getHeight() / 230;

        drawScaleList(canvas);//绘制刻度和横线
        drawTimeList(canvas);//绘制日期
        if (pointList.size() == 0) {// 绘制无数据
            canvas.translate(getWidth() / 2, -getHeight() / 2);
            canvas.drawText("No Data", 0, 0, emptyPaint);
        } else {
            drawPointList(canvas);//绘制点
        }
    }

    /**
     * 绘制横线和刻度
     */
    private void drawScaleList(Canvas canvas) {
        if (!isScale) return;
        for (int i = 0; i < 5; i++) {
            float height = -(30 + i * 50) * ratio + timeStroke / 2;
            canvas.drawLine(valStart, height, getWidth() - valRight, height, scalePaint);
            canvas.drawText(30 + i * 50 + "", 0, height + timeStroke / 2, timePaint);
        }
    }

    /**
     * 绘制底部日期
     */
    private void drawTimeList(Canvas canvas) {
        for (int i = 0; i < timeStr.length; i++) {
            int startX = valStart + i * valWidth / timeStr.length;
            canvas.drawText(timeStr[i], startX, -dp2px(3), timePaint);
        }
    }

    /**
     * 绘制点集合
     */
    private void drawPointList(Canvas canvas) {
        for (int i = 0; i < pointList.size(); i++) {
            int hour = pointList.keyAt(i);
            Point point = pointList.valueAt(i);
            int startX = valStart + hour * valWidth / 25;
            float yLow = -point.x * ratio + timeStroke / 2;
            float yHigh = -point.y * ratio + timeStroke / 2;
            canvas.drawCircle(startX, yLow, radioStroke, lowPaint);
            canvas.drawCircle(startX, yHigh, radioStroke, highPaint);

            Path path = new Path();//绘制连线
            path.moveTo(startX, yLow);
            path.lineTo(startX, yHigh);
            path.close();
            canvas.drawPath(path, linPaint);
        }
    }

    /**
     * 设置统计过的血压数据源
     *
     * @param dataMap String:日期 Point:x低压_y高压
     */
    public void setDataMap(Map<String, Point> dataMap) {
        pointList.clear();
        if (dataMap != null) {// 把数据源按小时分开,每小时最多一条
            String currHour = "";
            for (String time : dataMap.keySet()) {
                String hour = time.substring(0, 2);// 取前两位
                if (hour.equals(currHour)) continue;// 同一小时内的数据
                int hourInt = Integer.parseInt(hour);
                pointList.append(hourInt, dataMap.get(time));// 同一小时内,取第一条数据
            }
        }
        invalidate();
    }

    public void setScale(boolean scale) {
        isScale = scale;
        invalidate();
    }

    /**
     * dp转换px
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * sp转换px
     */
    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

}
