package science.zxc.walkin.core;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import static org.litepal.LitePalApplication.getContext;

/**
 * AUTH: Taosky
 * TIME: 2017/4/4 0004:下午 7:30.
 * MAIL: t@firefoxcn.net
 * DESC: 获取传感器数据的行走类
 */
public class MyWalk {

    //实现更新数据的接口
    private MyUpdateListener myUpdateListener = new MyUpdateListener() {
        @Override
        public void updateDistance() {
            distance += 0.67;
        }

        @Override
        public void updateDirection(float degree) {
            direction = degree;

        }
    };
    private float distance;//行人移动距离
    private float direction;//行人移动方向(角度)
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magneticSensor;
    private MySensorListener mySensorListener;


    public MyWalk() {
        mySensorListener = new MySensorListener();
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mySensorListener.setUpdateListener(myUpdateListener);
    }

    //获取行人移动距离
    public float getDistance() {
        return distance;
    }

    //获取行人移动方向
    public float getDirection() {
        return direction;
    }

    //开始行走
    public void start() {
        //注册监听器
        distance = 0;
        sensorManager.registerListener(mySensorListener, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(mySensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(mySensorListener);
        //
        distance = 0;
    }


}
