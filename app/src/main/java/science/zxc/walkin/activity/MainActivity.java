package science.zxc.walkin.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import science.zxc.walkin.R;
import science.zxc.walkin.core.MyWalk;

public class MainActivity extends AppCompatActivity {
    private TextView txtDistance;//距离
    private TextView txtDirection;//方向
    private TextView txtSteps;//步数
    private Button btnStart;//开始按钮
    private Button btnStop;//停止按钮
    private ImageView imgMap;//地图
    private Canvas canvas;//画布
    private Bitmap baseBitmap;//基础位图用于绘制
    private boolean isStopped = true;// 用以判断是否已停止
    private boolean isPainted = false; //判断是否选择起点
    private float direction;//移动方向(角度)
    private float distance;//移动距离
    private float startX;//画笔起点
    private float startY;
    private final int BEGIN = 0;
    private final int END = 1;
    private float preDistance;//上一次的移动距离(用于计算相对距离)

    private MyWalk myWalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        imgMap = (ImageView) findViewById(R.id.img_map);
        btnStart = (Button) findViewById(R.id.start_button);
        btnStop = (Button) findViewById(R.id.stop_button);
        txtDistance = (TextView) findViewById(R.id.distances);
        txtDirection = (TextView) findViewById(R.id.direction);
        txtSteps = (TextView) findViewById(R.id.steps);
        myWalk = new MyWalk();
        //监听触屏事件
        imgMap.setOnTouchListener(onTouch);
    }

    //按钮点击事件
    public void onClick(View view) {
        switch (view.getId()) {
            //开始按钮
            case R.id.start_button:
                if (isStopped && isPainted) {
                    btnStart.setVisibility(View.GONE);
                    btnStop.setVisibility(View.VISIBLE);
                    start();//开始
                } else if (isStopped) {
                    Toast.makeText(MainActivity.this, "请先选择起点", Toast.LENGTH_SHORT).show();
                }
                break;
            //停止按钮
            case R.id.stop_button:
                if (!isStopped) {
                    btnStop.setVisibility(View.GONE);
                    btnStart.setVisibility(View.VISIBLE);
                    stop();//停止
                }
                break;
            default:
                break;
        }
    }


    //触摸事件
    private View.OnTouchListener onTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!isPainted) {
                baseBitmap = Bitmap.createBitmap(imgMap.getWidth(),
                        imgMap.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(baseBitmap);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && isStopped) {
                    //用户按下动作
                    startX = motionEvent.getX();
                    startY = motionEvent.getY();
                    paintPoint(startX, startY, BEGIN);//绘制起点
                    Toast.makeText(MainActivity.this, "起点已选", Toast.LENGTH_SHORT).show();
                    isPainted = true;
                }

            }
            return true;
        }
    };

    //开始

    private void start() {
        preDistance = 0;
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
        myWalk.stop();
        isStopped = true;
        isPainted = false;
        paintPoint(startX, startY, END);
    }


    //更新信息
    private void updateInfo(final float distance, final float direction) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtSteps.setText( + (int) (distance / 0.67));
                txtDistance.setText(String.format(Locale.CHINA,"距离:%.2f米",distance));//设置距离
                txtDirection.setText(String.format(Locale.CHINA,"方向:%s",judgeDirection(direction)));//设置方向
                paintLine(distance, direction);


            }
        });
    }

    //根据角度判断方向
    private String judgeDirection(final float direction) {
        String directionText;
        if (direction < -100 && direction > -170) directionText = "南偏西 ";
        else if (direction <= -80 && direction >= -100) directionText = "正西";
        else if (direction < -10 && direction > -80) directionText = "北偏西";
        else if (direction <= 10 && direction >= -10) directionText = "正北";
        else if (direction < 80 && direction > 10) directionText = "北偏东";
        else if (direction <= 100 && direction >= 80) directionText = "正东";
        else if (direction < 170 && direction > 100) directionText = "南偏东";
        else if (direction >= 170 || direction <= -170) directionText = "正南";
        else directionText = "未知";
        return directionText;
    }

    //绘制起点、终点
    private void paintPoint(float X, float Y, int status) {
        Paint paint = new Paint();
        if (status == BEGIN) {
            paint.setColor(Color.rgb(122, 195, 99));
        } else if (status == END) {
            paint.setColor(Color.RED);
        }
        canvas.drawCircle(X, Y, 13, paint);
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
        paint.setStrokeWidth(3);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        imgMap.setImageBitmap(baseBitmap);//更新图片
        //更新坐标和距离
        startX = stopX;
        startY = stopY;
        preDistance = distance;
    }

    @Override
    protected void onPause() {
        super.onDestroy();
        if (myWalk != null) myWalk.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myWalk != null) myWalk.stop();
    }
}