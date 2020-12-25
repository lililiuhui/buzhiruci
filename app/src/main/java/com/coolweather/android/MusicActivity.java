package com.coolweather.android;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.coolweather.android.MusicService;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {
    private static SeekBar sb;
    private  static TextView tv_progress,tv_total;
    private ObjectAnimator animator;
    private MusicService musicService;
    MyServiceConn conn;
    private  boolean isUnbind=false;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        init();
    }
    private  void init(){
        initSeekBar();
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_total = (TextView) findViewById(R.id.tv_total);

        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_continue_play).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);

        ImageView iv_music=(ImageView)findViewById(R.id.iv_music);
        animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);

        intent=new Intent(this,MusicService.class);
        conn=new MyServiceConn();
        bindService(intent,conn,BIND_AUTO_CREATE);



    }
    public  static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData(); //获取从子线程发送过来的音乐播放进度
            int duration = bundle.getInt("duration");                  //歌曲的总时长
            int currentPostition = bundle.getInt("currentPosition");//歌曲当前进度
            sb.setMax(duration);                //设置SeekBar的最大值为歌曲总时长
            sb.setProgress(currentPostition);//设置SeekBar当前的进度位置

            Log.d("","111111111");
            //歌曲的总时长
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            String strMinute = null;
            String strSecond = null;
            if (minute < 10) {              //如果歌曲的时间中的分钟小于10
                strMinute = "0" + minute; //在分钟的前面加一个0
            } else {
                strMinute = minute + "";
            }
            if (second < 10) {             //如果歌曲的时间中的秒钟小于10
                strSecond = "0" + second;//在秒钟前面加一个0
            } else {
                strSecond = second + "";
            }
            tv_total.setText(strMinute + ":" + strSecond);
            //歌曲当前播放时长
            minute = currentPostition / 1000 / 60;
            second = currentPostition / 1000 % 60;
            if (minute < 10) {             //如果歌曲的时间中的分钟小于10
                strMinute = "0" + minute;//在分钟的前面加一个0
            } else {
                strMinute = minute + "";
            }
            if (second < 10) {               //如果歌曲的时间中的秒钟小于10
                strSecond = "0" + second;  //在秒钟前面加一个0
            } else {
                strSecond = second + "";
            }
            tv_progress.setText(strMinute + ":" + strSecond);
        }
    };

    private void initSeekBar() {
        sb=(SeekBar)findViewById(R.id.sb);

        //为滑动条添加事件监听
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==seekBar.getMax()){
                    animator.pause();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress=seekBar.getProgress();
                musicService.seekTo(progress);

            }
        });
    }
    class  MyServiceConn implements ServiceConnection{
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService=((MusicService.MusicControl) service).getService();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                musicService.play();
                animator.start();
                break;
            case R.id.btn_pause:
                musicService.pausePlay();
                animator.pause();
                Log.d("MusicActivity","停止");
                break;
            case R.id.btn_continue_play:
                musicService.continuePlay();
                animator.start();
                break;
            case R.id.btn_exit:
                unbind(isUnbind);
                isUnbind=true;
                finish();
                break;
                default:
                    break;
        }
    }
    private void unbind(boolean isUnbind){
        if(!isUnbind){
            musicService.pausePlay();
            unbindService(conn);
        }
    }
}
