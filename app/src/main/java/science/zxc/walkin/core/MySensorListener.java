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
public class MySensorListener implements SensorEventListener {
    protected float[] accelerometerValues = new float[3];
    protected float[] magneticValues = new float[3];

    private MyUpdateListener myUpdateListener ;

    public void setUpdateListener(MyUpdateListener updateListener){
        myUpdateListener = updateListener;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = sensorEvent.values.clone();
                break;
        }
        calcDirection(accelerometerValues,magneticValues);//计算方向
        calcDistance(accelerometerValues);//计算距离

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

    //计算距离(步数)
    private void calcDistance(float[] accelerometerValues){
        float average = (float) Math.sqrt(Math.pow(accelerometerValues[0],2)
                +Math.pow(accelerometerValues[1],2)+Math.pow(accelerometerValues[2],2));
        avg_check_v(average);
        }



    float avg_v=0;
    float min_v=0;
    float max_v=0;


    int acc_count=0;
    int up_c=0;
    int down_c=0;
    long pre_time=0;
    void reset(){
        avg_v=0;
        acc_count=0;
        up_c=0;
        down_c=0;
    }
    void avg_check_v(float v){
        acc_count++;
//求移动平均线
        //50ms 1 second 20 , 3 sec60;
        if(acc_count<64){
            //avg_v=((acc_count-1)*avg_v+v)/acc_count;
            avg_v=avg_v+(v-avg_v)/acc_count;
        }
        else{
            //avg_v=(avg_v*99+v)/100;
            avg_v=avg_v*63/64+v/64;
        }

        if(v>avg_v){
            up_c++;
            if(up_c==1){
                //Log.e("wokao","diff:"+(max_v-min_v));
                max_v=avg_v;
            }
            else{
                max_v=Math.max(v,max_v);
            }
            if(up_c>=2){
                down_c=0;
            }
        }
        else{
            down_c++;
            if(down_c==1){
                min_v=v;
            }
            else{
                min_v=Math.min(v,min_v);
            }
            if(down_c>=2){
                up_c=0;
            }
        }

        if(up_c==2&&(max_v-min_v)>2){
            //
            long cur_time=System.currentTimeMillis();
            if(cur_time-pre_time>250){
                pre_time=cur_time;
                myUpdateListener.updateDistance();//更新距离
                //Log.d("Steps","onStep");
            }
            else{
                up_c=1;
            }
        }


    }
}
