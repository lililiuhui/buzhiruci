package com.coolweather.android;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
   private MediaPlayer player;
   private Timer timer;
   ArrayList<String> musicList = new ArrayList<String>();



    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();

    }
    @Override
    public void onCreate() {
        super.onCreate();
        player=new MediaPlayer();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player==null)
            return;
        if(player.isPlaying())player.stop();
        player.reset();
        player=null;
    }
    public void addTimer(){
        if(timer==null){
            timer=new Timer();
            TimerTask task=new TimerTask() {
                @Override
                public void run() {
                    if (player==null)return;
                    if(!player.isPlaying()){
                    }else {
                        int duration=player.getDuration();
                        int currentPosition=player.getCurrentPosition();
                        Message msg=MusicActivity.handler.obtainMessage();
                        Bundle bundle=new Bundle();
                        bundle.putInt("duration",duration);
                        bundle.putInt("currentPosition",currentPosition);
                        msg.setData(bundle);
                        MusicActivity.handler.sendMessage(msg);
                    }


                }
            };
            timer.schedule(task,5,500);
        }
    }

    class MusicControl extends Binder{
        MusicService getService(){
            return  MusicService.this;
        }
    }

    public void play(){
        try {
            player.reset();
            player=MediaPlayer.create(getApplicationContext(),R.raw.music3);
            player.start();
            addTimer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public  void pausePlay(){
        player.pause();
    }
    public  void  continuePlay(){
        player.start();
    }
    public void seekTo(int progress){
        player.seekTo(progress);

    }

}
