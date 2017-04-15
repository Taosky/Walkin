package science.zxc.walkin.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import science.zxc.walkin.R;
import science.zxc.walkin.core.MyWalk;

public class MainActivity extends AppCompatActivity {
    private FABToolbarLayout fabToolbarLayout;
    private TextView txtDistance;
    private TextView txtDirection;
    private ImageView imgMap;//地图
    private Canvas canvas;//画布
    private Bitmap mapBitmap;//地图位图
    private Bitmap baseBitmap;//基础位图用于绘制
    private boolean isStopped = true;// 用以判断是否已停止
    private boolean isPainted = false; //判断是否选择起点

    private float direction;//移动方向(角度)
    private float distance;//移动距离
    private float startX;//画笔起点
    private float startY;
    private float preDistance;//上一次的移动距离(用于计算相对距离)

    private MyWalk myWalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        imgMap = (ImageView) findViewById(R.id.img_map);
        //mapBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.map)
        fabToolbarLayout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        FloatingActionButton fabStart = (FloatingActionButton) findViewById(R.id.fabtoolbar_fab);
        Button btnStop = (Button) findViewById(R.id.btn_stop);
        txtDistance = (TextView) findViewById(R.id.txt_distance);
        txtDirection = (TextView) findViewById(R.id.txt_direction);
        myWalk = new MyWalk();
        //监听触屏事件
        imgMap.setOnTouchListener(touch);
        //监听点击事件
        fabStart.setOnClickListener(onClick);
        btnStop.setOnClickListener(onClick);

    }

    //按钮点击事件
    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //开始按钮
                case R.id.fabtoolbar_fab:
                    if (isStopped && isPainted) {
                        start();//开始
                    } else if (isStopped) {
                        Toast.makeText(MainActivity.this, "请先选择起点", Toast.LENGTH_SHORT).show();
                    }
                    break;
                //停止按钮
                case R.id.btn_stop:
                    if (!isStopped) {
                        stop();//停止
                    }
                    break;
                default:
                    break;
            }

        }
    };

    //触摸事件
    private View.OnTouchListener touch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!isPainted) {
                baseBitmap = Bitmap.createBitmap(imgMap.getWidth(),
                        imgMap.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(baseBitmap);
                //canvas.drawBitmap(mapBitmap,0,0,null);//绘制地图

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && isStopped) {
                    //用户按下动作
                    startX = motionEvent.getX();
                    startY = motionEvent.getY();
                    paintPoint(startX, startY);//绘制起点
                    Toast.makeText(MainActivity.this, "起点已选:" + startX + "," + startY, Toast.LENGTH_SHORT).show();
                    isPainted = true;
                }

            }
            return true;
        }
    };

    //开始
    private void start() {
        preDistance = 0;
        fabToolbarLayout.show();
        myWalk.start();
        isStopped = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStopped) {
                    distance = myWalk.getDistance();
                    direction = myWalk.getDirection();
                    updateInfo(distance, direction);//更新信息
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //停止
    private void stop() {
        fabToolbarLayout.hide();
        myWalk.stop();
        isStopped = true;
        isPainted = false;
    }


    //更新信息
    private void updateInfo(final float distance, final float direction) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtDistance.setText(String.valueOf((float) (
                        Math.round(distance * 100)) / 100 + " 米"));//设置距离(int变量会被认为是资源ID而报错，需要转换)
                txtDirection.setText(judgeDirection(direction));//设置方向
                paintLine(distance, direction);


            }
        });
    }

    //根据角度判断方向
    private String judgeDirection(final float direction) {
        String directioonText;
        if (direction < -100 && direction > -170) directioonText = "南偏西 ";
        else if (direction <= -80 && direction >= -100) directioonText = "正西";
        else if (direction < -10 && direction > -80) directioonText = "北偏西";
        else if (direction <= 10 && direction >= -10) directioonText = "正北";
        else if (direction < 80 && direction > 10) directioonText = "北偏东";
        else if (direction <= 100 && direction >= 80) directioonText = "正东";
        else if (direction < 170 && direction > 100) directioonText = "南偏东";
        else if (direction >= 170 || direction <= -170) directioonText = "正南";
        else directioonText = "出错";
        return directioonText;
    }



    //绘制起点、终点
    private void paintPoint(float X, float Y) {
        Paint paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setColor(Color.GREEN);
        canvas.drawPoint(X, Y, paint);
        imgMap.setImageBitmap(baseBitmap);//更新图片

    }

    //绘制路线(连线)
    private void paintLine(float distance, float direction) {
        //没有移动直接返回
        if (distance == 0) {
            return;
        }
        //通过相对距离计算停止点的坐标
        float reDistance = distance - preDistance;
        reDistance *= 8;
        float stopX = (float) (startX + reDistance * Math.cos((direction - 90) * Math.PI / 180));
        float stopY = (float) (startY + reDistance * Math.sin((direction - 90) * Math.PI / 180));
        //绘制
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        imgMap.setImageBitmap(baseBitmap);//更新图片
        //更新坐标和距离
        startX = stopX;
        startY = stopY;
        preDistance = distance;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myWalk != null) myWalk.stop();
    }
}