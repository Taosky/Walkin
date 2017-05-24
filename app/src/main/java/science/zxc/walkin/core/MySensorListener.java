package science.zxc.walkin.core;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


/**
 * AUTH: Taosky
 * TIME: 2017/4/4 0004:下午 7:54.
 * MAIL: t@firefoxcn.net
 * DESC: 传感器监听类
 */
class MySensorListener implements SensorEventListener {
    private float lastAcceleration = 0; //上次加速度
    private boolean isDirectionUp = false; //是否为上升状态
    private int continueUpCount = 0; //持续上升次数
    private  int continueUpFormerCount = 0; //上一个点的持续上升次数
    private float peakOfWave = 0; //波峰值
    private float valleyOfWave = 0; //波谷值
    private long timeOfThisPeak = 0; //此次波峰的时间
    private float threadThreshold = (float) 2.0;// 动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    private final int valueNum = 5;//用于存放计算阈值的波峰波谷差值
    private float[] tempValue = new float[valueNum];
    private int tempCount = 0;

    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];

    private MyUpdateListener myUpdateListener ;

    void setUpdateListener(MyUpdateListener updateListener){
        myUpdateListener = updateListener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometerValues = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticValues = sensorEvent.values.clone();
                    break;

            }
            calcDirection(accelerometerValues, magneticValues);//计算方向
            getAcceleration(accelerometerValues);//计算距离
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //计算方向角度
    private void calcDirection(float[] accelerometerValues,float[] magneticValues){
        float[] R = new float[9];
        float[] values = new float[3];
        SensorManager.getRotationMatrix(R,null,accelerometerValues,magneticValues);
        SensorManager.getOrientation(R, values);
        float degree = (float) Math.toDegrees(values[0]);
        myUpdateListener.updateDirection(degree);//更新方向数据
    }


    /**
     * 计算加速度
     */
    synchronized private void getAcceleration(float[] accelerometerValues) {
        //忽略加速度方向，取绝对值
        float acceleration = (float) Math.sqrt(Math.pow(accelerometerValues[0], 2)
                + Math.pow(accelerometerValues[1], 2) + Math.pow(accelerometerValues[2], 2));
        detectorNewStep(acceleration);
    }
    /**
     * 检测步子，并开始计步
     * 如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
     * 符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
     */
    private void detectorNewStep(float values) {
        if (lastAcceleration == 0) {
            lastAcceleration = values;
        } else {
            if (detectorPeak(values, lastAcceleration)) {
                long timeOfLastPeak = timeOfThisPeak;
                long timeOfNow = System.currentTimeMillis(); //当前时间
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= threadThreshold) && (timeOfNow - timeOfLastPeak) <= 2000) {
                    timeOfThisPeak = timeOfNow;
                    //视为一步，更新步数
                    myUpdateListener.updateDistance();
                }
                float initialThreshold = (float) 1.7;
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= initialThreshold)) {
                    timeOfThisPeak = timeOfNow;
                    threadThreshold = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        lastAcceleration = values;
    }

    /**
     * 检测波峰
     * 以下四个条件判断为波峰：
     *  1.目前点为下降的趋势：isDirectionUp为false
     *  2.之前的点为上升的趋势：lastStatus为true
     *  3.到波峰为止，持续上升大于等于2次
     *     - 这是因为：加速度传感器采集的频率比较高，一般大于30Hz，2次还算少的了
     *  4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * @param newValue 本次的加速度
     * @param oldValue 上次的加速度
     */
    private boolean detectorPeak(float newValue, float oldValue) {
        boolean lastStatus = isDirectionUp;
        if (newValue >= oldValue) {//可以换成差值大于某一值也可
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;//持续上升次数清零
            isDirectionUp = false;
        }
        float minValue = 11f;
        float maxValue = 19.6f;
        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }
    /**
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     */
    private float Peak_Valley_Thread(float value) {
        float tempThread = threadThreshold;
        if (tempCount < valueNum) { //存储过程
            tempValue[tempCount] = value;
            tempCount++;
        } else { //计算过程
            tempThread = gradientThreshold(tempValue, valueNum);//梯度化阈值
            System.arraycopy(tempValue, 1, tempValue, 0, valueNum - 1);
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }
    /**
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     */
    private float gradientThreshold(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        String TAG = "MySensorListener";
        if (ave >= 8) {
            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
            Log.v(TAG, "else");
            ave = (float) 1.7;
        }
        return ave;
    }




}
