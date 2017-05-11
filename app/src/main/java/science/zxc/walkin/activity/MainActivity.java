package science.zxc.walkin.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import science.zxc.walkin.R;
import science.zxc.walkin.core.MyWalk;
import science.zxc.walkin.db.Record;

import science.zxc.walkin.util.BitmapUtil;
import science.zxc.walkin.util.DirectionUtil;

public class MainActivity extends AppCompatActivity {
    private TextView txtDistance;//距离
    private TextView txtDirection;//方向
    private TextView txtSteps;//步数
    private Button btnStart;//开始按钮
    private Button btnStop;//停止按钮
    private Button btnSave;//保存按钮
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.record_menu_item:
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        //初始化
        imgMap = (ImageView) findViewById(R.id.img_map);
        btnStart = (Button) findViewById(R.id.start_button);
        btnStop = (Button) findViewById(R.id.stop_button);
        btnSave = (Button) findViewById(R.id.save_button);
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
                    start();//开始行走
                } else if (isStopped) {
                    Toast.makeText(MainActivity.this, "请先选择起点", Toast.LENGTH_SHORT).show();//未选择起点
                }
                break;
            //停止按钮
            case R.id.stop_button:
                if (!isStopped) {
                    stop();//停止行走
                    btnSave.setVisibility(View.VISIBLE);//行走结束后可以保存
                }
                break;
            //保存按钮
            case R.id.save_button:
                saveRecord();
                btnSave.setVisibility(View.INVISIBLE);//未行走不显示保存
                break;
            default:
                break;
        }
    }

    //imaageView触摸事件
    private View.OnTouchListener onTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!isPainted) {
                //刷新bitMap
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

    //开始行走
    private void start() {
        btnStart.setVisibility(View.INVISIBLE);//开始按钮不可见
        btnStop.setVisibility(View.VISIBLE);
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
                        Thread.sleep(500);//每500ms更新
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //停止行走
    private void stop() {
        myWalk.stop();
        btnStop.setVisibility(View.INVISIBLE);//停止按钮不可见
        btnStart.setVisibility(View.VISIBLE);
        isStopped = true;
        isPainted = false;
        paintPoint(startX, startY, END);
    }

    //更新信息
    private void updateInfo(final float distance, final float direction) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtSteps.setText(String.format(Locale.CHINA, "步数:%d", (int) (distance / 0.67)));
                txtDistance.setText(String.format(Locale.CHINA, "距离:%.2f米", distance));//设置距离
                txtDirection.setText(String.format(Locale.CHINA, "方向:%s",
                        DirectionUtil.judgeDirection(direction)));//设置方向
                paintLine(distance, direction);
            }
        });
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

    //绘制路径(连线)
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

    //保存行走记录
    private void saveRecord() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String dateString = formatter.format(currentTime);
        Record record = new Record();
        record.setDatetime(dateString);
        record.setDistance(txtDistance.getText().toString());
        record.setSteps(txtSteps.getText().toString());
        record.setImage(BitmapUtil.getBitmapByte(baseBitmap));
        record.save();
        Toast.makeText(MainActivity.this, "已保存本次行走记录", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isPainted) {
            stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPainted) {
            stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPainted) {
            stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isPainted) {
            stop();
        }
    }
}