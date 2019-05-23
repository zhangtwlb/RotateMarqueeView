package com.tw.marquee.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * 简约版
 * Created by ztw 2019.5.23
 * 平移模式+闪现模式 MarqueeView-simple
 */

public class MarqueeView extends View implements Runnable {
    private static final String TAG = "MarqueeView-simple";
    public static final int LOCATION_TOP = 1001;//
    public static final int LOCATION_BOTTOM = 1002;//
    public static final int LOCATION_CENTER = 1003;//
    public static final int LOCATION_LEFT = 1004;//
    public static final int LOCATION_RIGHT = 1005;//

    public static final int LOCATION_RIGHT_TOP = 1006;//
    public static final int LOCATION_RIGHT_BOTTOM = 1007;//
    public static final int LOCATION_LEFT_TOP = 1008;//
    public static final int LOCATION_LEFT_BOTTOM = 1009;//

    private int currentLocation = LOCATION_LEFT_TOP;

    public static final int TYPE_BLINK = 0;//闪现模式
    public static final int TYPE_DISPLACEMENT = 1;//平移模式
    private int currentType=TYPE_DISPLACEMENT;
    private boolean isRandom;//闪现模式下的 是否随机开关

    private String string;//最终绘制的文本
    private float speed = 1;//移动速度
    private int textColor = Color.BLACK;//文字颜色,默认黑色
    private float textSize = 12, textAngle = 0;//文字颜色,默认黑色 //角度
    private int textdistance;//
    private int textDistance1 = 10;//item间距，dp单位
    private String black_count = "";//间距转化成空格距离

    private int repetType = REPET_INTERVAL;//滚动模式
    public static final int REPET_ONCETIME = 0;//n次结束
    public static final int REPET_INTERVAL = 1;//一次结束以后，再继续第二次

    private boolean isClickStop = false;//点击是否暂停
    private boolean isResetLocation = true;//默认为true
    private float xLocation = 0;//文本的x坐标
    private float yLocation = 0;//文本的y坐标

    private int contentWidth;//内容的宽度
    private boolean isRoll = false;//是否继续滚动
    private TextPaint paint;//画笔
    private Rect rect;
    private int repetCounts = 1;//
    private Thread thread;
    private float textHeight;
    private int alpha = 255;//默认透明度

    private int times;//次数
    private long mInvalidata = 20;//刷新界面的频率  单位毫秒
    private long mBLINKInvalidata = 1 * 1000;//闪现间隔时间 单位毫秒
    private long mBLINKStay = 5 * 1000;//闪现停留时间 单位毫秒
    private long tempBLINKStay = 0;//闪现停留时间临时变量
    private int currenrLocationTag = 0;
    private float padding = 40;//间距
    private double buchangX, buchangY;


    public MarqueeView(Context context) {
        this(context, null);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initattrs(attrs);
        initpaint();
        initClick();
        initData();
    }

    private void initData() {
        locationModeList.add(LOCATION_CENTER);
        locationModeList.add(LOCATION_LEFT_TOP);
        locationModeList.add(LOCATION_RIGHT_TOP);
        locationModeList.add(LOCATION_RIGHT_BOTTOM);
        locationModeList.add(LOCATION_LEFT_BOTTOM);
    }

    private void initClick() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isClickStop) {
                    if (isRoll) {
                        stopRoll();
                    } else {
                        continueRoll();
                    }
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void initattrs(AttributeSet attrs) {
        TintTypedArray tta = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                R.styleable.MarqueeView);

        textColor = tta.getColor(R.styleable.MarqueeView_marqueeview_text_color, textColor);
        isClickStop = tta.getBoolean(R.styleable.MarqueeView_marqueeview_isclickalbe_stop, isClickStop);
        isResetLocation = tta.getBoolean(R.styleable.MarqueeView_marqueeview_is_resetLocation, isResetLocation);
        speed = tta.getFloat(R.styleable.MarqueeView_marqueeview_text_speed, speed);
        textSize = tta.getFloat(R.styleable.MarqueeView_marqueeview_text_size, textSize);
        textAngle = tta.getFloat(R.styleable.MarqueeView_marqueeview_text_angle, textAngle);
        textDistance1 = tta.getInteger(R.styleable.MarqueeView_marqueeview_text_distance, textDistance1);
        repetType = tta.getInt(R.styleable.MarqueeView_marqueeview_repet_type, repetType);
        tta.recycle();
    }


    /**
     * 刻字机修改
     */
    private void initpaint() {
        rect = new Rect();
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);//初始化文本画笔
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(textColor);//文字颜色值,可以不设定
        paint.setTextSize(dp2px(textSize));//文字大小

    }

    public int getCurrentType() {
        return currentType;
    }

    public void setCurrentType(int currentType) {
        this.currentType = currentType;
    }

    public int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentType==TYPE_BLINK) {//是 闪现
            switch (repetType) {
                case REPET_ONCETIME://n次后停止
                    if (times == repetCounts) {
                        stopRoll();
                        times = 0;
                    }
                    times++;
                    break;
                case REPET_INTERVAL://无限循环
                    break;
            }
        } else if (currentType==TYPE_DISPLACEMENT) {//平滑位移
            switch (repetType) {
                case REPET_ONCETIME://n次后停止
                    if (times == repetCounts+1) {
                        stopRoll();
                        times = 0;
                    }
                    break;
                case REPET_INTERVAL://无限循环
                    break;
            }
        }
        if (string != null) {   //把文字画出来
            canvas.drawText(string, xLocation, yLocation + textHeight / 2 + padding, paint);
        }
    }

    public void setmBLINKInvalidata(long mBLINKInvalidata) {
        this.mBLINKInvalidata = mBLINKInvalidata;
    }

    public void setmBLINKStay(long mBLINKStay) {
        this.mBLINKStay = mBLINKStay * 1000;
    }


    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * 设置滚动次数
     */
    public void setRepetCounts(int repetCounts) {
        this.repetCounts = repetCounts;
    }

    public void setRepetType(int repetType) {
        this.repetType = repetType;
    }

    public void setTextAngle(float textAngle) {
        this.textAngle = textAngle;
    }

    public void setXYLocation(float x, float y) {
        xLocation = x;
        yLocation = y;
    }

    /***
     * 接收集合形式的位置 （集合 pos）
     * */
    List<Integer> locationModeList = new ArrayList<Integer>() {
    };

    public void setXYLocationByModeList(List<Integer> locationModeList) {
        this.locationModeList = locationModeList;
    }

    public void setXYLocationByMode() {
        if (currenrLocationTag >= locationModeList.size()) {
            currenrLocationTag = 0;
        }
        setXYLocationByMode(locationModeList.get(currenrLocationTag));
    }

    public void setXYLocationByMode(int currentLocation) {
        if (currentType==TYPE_BLINK) {
            switch (currentLocation) {
                case LOCATION_TOP://上
                    setXYLocation(0, 0);
                    break;
                case LOCATION_CENTER://zhongjian
                    setXYLocation(getWidth() / 2 - contentWidth / 2, getHeight() / 2);
                    break;
                case LOCATION_BOTTOM://
                    setXYLocation(0, getHeight() - 2 * padding);
                    break;
                case LOCATION_LEFT:
                    setXYLocation(padding, getHeight() / 2);
                    break;
                case LOCATION_LEFT_TOP:
                    setXYLocation(padding, 0);
                    break;
                case LOCATION_LEFT_BOTTOM:
                    setXYLocation(padding, getHeight() - 2 * padding);
                    break;
                case LOCATION_RIGHT:
                    setXYLocation(getWidth() - contentWidth - padding, getHeight() / 2);
                    break;
                case LOCATION_RIGHT_TOP:
                    setXYLocation(getWidth() - contentWidth - padding, 0);
                    break;
                case LOCATION_RIGHT_BOTTOM:
                    setXYLocation(getWidth() - contentWidth - padding, getHeight() - 2 * padding);
                    break;
            }
        } else {
            switch (currentLocation) {
                case LOCATION_TOP:
                    setXYLocation(0, 0);
                    break;
                case LOCATION_CENTER:
                    setXYLocation(getWidth() / 2, getHeight() / 2);
                    break;
                case LOCATION_BOTTOM:
                    setXYLocation(0, getHeight() - 2 * padding);
                    break;
            }
        }
    }

    public void setRandom(boolean random) {
        isRandom = random;
    }


    @Override
    public void run() {
        while (isRoll && !TextUtils.isEmpty(string)) {
            try {
                if (currentType==TYPE_BLINK) {
                    Thread.sleep(mBLINKInvalidata);
                    //x 范围= 0- 宽度  y范围 = view 高度
                    int x = getWidth() - contentWidth, y = (int) (getHeight() - textHeight / 2); //view的宽高
                    if (isRandom) {
                        Random r = new Random();
                        xLocation = r.nextInt(Math.abs(x));
                        yLocation = (int) (r.nextInt(Math.abs(y)) + textHeight / 2);
                    } else {
                        if (tempBLINKStay >= mBLINKStay) {//设置停留时间
                            setXYLocationByMode();
                            currenrLocationTag++;
                            tempBLINKStay = 0;
                        } else {
                            tempBLINKStay += mBLINKInvalidata;
                        }
                    }
                } else if (currentType==TYPE_DISPLACEMENT) {
                    Thread.sleep(mInvalidata);
                    int endx=getWidth(),endy = getHeight(); //view的宽高
                    switch (currentLocation) {
                        case LOCATION_LEFT_TOP://左上->右下
                            xLocation += buchangX;
                            yLocation += buchangY;
                            if (yLocation > endy) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_LEFT://从左向右
                            xLocation += buchangX;
                            if (xLocation > endx) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_RIGHT://从又向左
                            xLocation -= buchangX;
                            if (xLocation <0) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_LEFT_BOTTOM://左下->右上
                            xLocation += buchangX;
                            yLocation -= buchangY;
                            if (yLocation <= 0) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_RIGHT_TOP://右上->左下
                            xLocation -= buchangX;
                            yLocation += buchangY;
                            if (yLocation > endy) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_RIGHT_BOTTOM://右下->左上
                            xLocation -= buchangX;
                            yLocation -= buchangY;
                            if (yLocation < 0) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_BOTTOM://下->上
                            yLocation -= buchangY;
                            if (yLocation < 0) {
                                setPosByTag(currentLocation);
                            }
                            break;
                        case LOCATION_TOP://上->下
                            yLocation += buchangY;
                            if (yLocation > endy) {
                                setPosByTag(currentLocation);
                            }
                            break;
                    }
                    setXYLocation(xLocation, yLocation);
                    Log.e(TAG, buchangX + "-x=" + xLocation + "##" + buchangY + "-&y=" + yLocation);
                }
                postInvalidate();//每隔n毫秒重绘视图
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 继续滚动
     */
    public void continueRoll() {
        if (!isRoll) {
            if (thread != null) {
                thread.interrupt();

                thread = null;
            }

            isRoll = true;
            thread = new Thread(this);
            thread.start();//开启死循环线程让文字动起来

        }
    }

    /**
     * 停止滚动
     */
    public void stopRoll() {
        isRoll = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

    }

    /**
     * 点击是否暂停，默认是不
     *
     * @param isClickStop
     */
    private void setClickStop(boolean isClickStop) {
        this.isClickStop = isClickStop;
    }

    /***
     *  设置初始位置
     *
     * */
    public void setPosByTag(int currenrLocation) {
        this.currentLocation = currenrLocation;
        int endx = getWidth(), endy = getHeight(); //view的宽高
        switch (currenrLocation) {
            case LOCATION_LEFT_TOP:
                xLocation = 0;
                yLocation = 0;
                break;
            case LOCATION_LEFT://从左向右
                xLocation = -contentWidth;
                yLocation = endy/2-getContentHeight()/2;
                break;
            case LOCATION_RIGHT://从又向左
                xLocation = endx;
                yLocation = endy/2-getContentHeight()/2;
                break;
            case LOCATION_LEFT_BOTTOM:
                xLocation = 0;
                yLocation = endy;
                break;
            case LOCATION_RIGHT_TOP:
                xLocation = endx;
                yLocation = 0;
                break;
            case LOCATION_RIGHT_BOTTOM:
                xLocation = endx;
                yLocation = endy;
                break;
            case LOCATION_BOTTOM:
                xLocation = endx / 2 - contentWidth / 2;
                yLocation = endy;
                break;
            case LOCATION_TOP:
                xLocation = endx / 2 - contentWidth / 2;
                yLocation = 0;
                break;
        }
        switch (repetType) {
            case REPET_ONCETIME://n次后停止
                times++;
                break;
            case REPET_INTERVAL://无限循环
                break;
        }

    }


    /**
     * 是否循环滚动
     *
     * @param isContinuable
     */
    private void setContinueble(int isContinuable) {
        this.repetType = isContinuable;
    }

    /**
     * 反向文字
     *
     */
    private String textReversalble(String text) {
        StringBuilder stringBuiler = new StringBuilder(text);
        return stringBuiler.reverse().toString();//文字反向
    }



    /**
     * 设置文字间距 必须在设置setContent之前调用此方法。
     *
     * @param textdistance2
     */
    public void setTextDistance(int textdistance2) {
        //设置之后就需要初始化了
        String black = " ";
        float oneBlack_width = getBlacktWidth();//空格的宽度
        textdistance2 = dp2px(textdistance2);
        int count = (int) (textdistance2 / oneBlack_width);//空格个数，有点粗略，可以精细

        if (count == 0) {
            count = 1;
        }

        textdistance = (int) (oneBlack_width * count);
        black_count = "";
        for (int i = 0; i <= count; i++) {
            black_count = black_count + black;//间隔字符串
        }
        setContent(string);//设置间距以后要重新刷新内容距离，
    }

    /**
     * 计算出一个空格的宽度
     *
     * @return
     */
    private float getBlacktWidth() {
        String text1 = "en en";
        String text2 = "enen";
        return getContentWidth(text1) - getContentWidth(text2);

    }

    private float getContentWidth(String black) {
        if (black == null || black == "") {
            return 0;
        }

        if (rect == null) {
            rect = new Rect();
        }
        paint.getTextBounds(black, 0, black.length(), rect);
        textHeight = getContentHeight();

        return rect.width();
    }

    /**
     * 获取内容高度
     *
     * @param
     * @return
     */
    private float getContentHeight() {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return Math.abs((fontMetrics.bottom - fontMetrics.top)) / 2;
    }

    /**
     * 设置文字颜色
     *
     * @param textColor
     */
    public void setTextColor(int textColor) {
        if (textColor != 0) {
            this.textColor = textColor;
            paint.setColor(getResources().getColor(textColor));//文字颜色值,可以不设定
        }
    }

    /**
     * 设置文字颜色
     *
     * @param textColorString
     */
    public void setTextColorByString(String textColorString) {
        if (textColorString.contains("#")) {
            this.textColor = Color.parseColor(textColorString);
            paint.setColor(textColor);//文字颜色值,可以不设定
        } else {
            Log.e(TAG, "颜色值格式错误！！");
        }
    }

    /**
     * 设置文字透明度
     *
     * @param alpha
     */
    public void setTextAlpha(int alpha) {
        this.alpha = alpha;
        paint.setAlpha(alpha);//文字透明度
    }

    /**
     * 设置文字大小
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        if (textSize > 0) {
            this.textSize = textSize;
            paint.setTextSize(dp2px(textSize));//文字颜色值,可以不设定
            contentWidth = (int) (getContentWidth(string) + textdistance);//大小改变，需要重新计算宽高
        }
    }

    /**
     * 设置滚动速度
     *
     * @param speed
     */
    public void setTextSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * 设置单位时间内滚动完成 既 单位时间内的速度
     * <p>
     * isBLINK 闪现模式
     *
     * @param time 时间 单位 秒
     */
    public void setTextTimeSpeed(int time) {
        if (currentType==TYPE_BLINK) {//闪现模式
            setmBLINKInvalidata(time * 1000);
        } else if (currentType==TYPE_DISPLACEMENT) {//平移模式
            // 两点间距/时间=速度
            int eX = getWidth(), eY = getHeight();//右下角
            double width = 0;
            switch (currentLocation) {
                case LOCATION_LEFT_TOP://左上->右下
                case LOCATION_LEFT_BOTTOM://左下->右上
                case LOCATION_RIGHT_TOP://右上->左下
                case LOCATION_RIGHT_BOTTOM://右下->左上
                    width = Math.sqrt(eX * eX + eY * eY);
                    break;
                case LOCATION_BOTTOM://下->上
                case LOCATION_TOP://上->下
                case LOCATION_LEFT://左->右
                case LOCATION_RIGHT://右->左
                    width = eY;
                    break;
            }
            double aTime = time * 1000 / mInvalidata;
            double speed = width / aTime;
            setTextSpeed((float) speed);

            //x 范围= 0- 宽度  y范围 = view 高度
            double h_wbi = ((double) getHeight()) / getWidth();//0.8
            buchangX = speed / 2;
            buchangY = (speed / 2) * h_wbi;
        }
        stopRoll();
        continueRoll();
    }


    /***
     * 设置闪现模式下的内容
     * */
    public void setBlinkContent(String text, boolean isBLINK) {
        currentType=TYPE_BLINK;
        setContent(text);
    }

    /**
     * 设置滚动的条目内容  字符串形式的
     *
     * @parambt_control00
     */
    public void setContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        if (currentType==TYPE_BLINK) {
            xLocation = 0;
            yLocation = 0;
        } else if (currentType==TYPE_DISPLACEMENT) {
            setPosByTag(currentLocation);
        }
        if (!content.endsWith(black_count)) {
            content = content + black_count;//避免没有后缀
        }
        contentWidth = (int) getContentWidth(content);
        this.string = content;
        if(currentType==TYPE_DISPLACEMENT){
            switch (currentLocation) {
                case LOCATION_LEFT:
                    this.string = textReversalble(string);
                    break;//从左向右
            }
        }

        if (!isRoll) {//如果没有在滚动的话，重新开启线程滚动
            continueRoll();
        }
    }

    /**
     * 新添加内容的时候，是否初始化位置
     *
     * @param isReset
     */
    private void setResetLocation(boolean isReset) {
        isResetLocation = isReset;
    }

}
