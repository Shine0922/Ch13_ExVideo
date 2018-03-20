package com.example.win7.exvideo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private ImageView imgFront,imgStop,imgPlay,imgPause,imgNext,imgEnd;
    private TextView txtVideo;
    private ListView listVideo;
    private SurfaceView sufVideo;
    private MediaPlayer mediaplayer;
    private SurfaceHolder sufHolder;

    //  宣告 songpath 常數儲存SD卡路徑
    private final String VIDEOPATH= Environment.getExternalStorageDirectory().getPath() + "/";

    //  影片名稱
    String[] videoname=new String[] {"robot", "post"};
    //  影片檔案
    String[] videofile=new String[] {"robot.3gp", "post.3gp"};
    //  目前播放影片
    private int cListItem = 0;
    //暫停旗標
    private Boolean falgPause = false;

    private ArrayAdapter<String> adaVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFront = (ImageView)findViewById(R.id.imgFront);
        imgStop = (ImageView)findViewById(R.id.imgStop);
        imgPlay = (ImageView)findViewById(R.id.imgPlay);
        imgPause = (ImageView)findViewById(R.id.imgPause);
        imgNext = (ImageView)findViewById(R.id.imgNext);
        imgEnd = (ImageView)findViewById(R.id.imgEnd);
        txtVideo = (TextView)findViewById(R.id.txtVideo);
        sufVideo = (SurfaceView)findViewById(R.id.sufVideo);
        listVideo = (ListView)findViewById(R.id.listVideo);

        imgFront.setOnClickListener(listener);
        imgStop.setOnClickListener(listener);
        imgPlay.setOnClickListener(listener);
        imgPause.setOnClickListener(listener);
        imgNext.setOnClickListener(listener);
        imgEnd.setOnClickListener(listener);

        listVideo.setOnItemClickListener(listListener);

        mediaplayer = new MediaPlayer();
        //  建立 Surface 相關物件
        sufHolder = sufVideo.getHolder();

        adaVideo = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, videoname);
        //  檢查是否取得執行時授權
        requestStoragePermission();

    }

    //檢查驗證
    private void requestStoragePermission()
    {
        if(Build.VERSION.SDK_INT >=23)
        {
            //  判斷是否取得驗證
            int hasPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if(hasPermission != PackageManager.PERMISSION_GRANTED)
            {   //  未取得驗證
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }
        }
        //  已取得驗證
        listVideo.setAdapter(adaVideo);

    }

    //  requestPermissions 觸發的事件
    @Override
    public void onRequestPermissionsResult(int requestCode,String[]permissions,int[]grantResults)
    {
        if(requestCode == 1)
        {   //  按允許鈕
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                listVideo.setAdapter(adaVideo);
            }
            else
            {
                Toast.makeText(this,"未取得權限",Toast.LENGTH_LONG).show();
                finish();   //  結束應用程式
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    //  ImageView的監聽事件
    private ImageView.OnClickListener listener = new ImageView.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case R.id.imgFront:     //  上一首
                    frontVideo();
                    break;

                case R.id.imgStop:  //  停止
                    if(mediaplayer.isPlaying()) // 是否正在播放
                    {
                        mediaplayer.reset();    //  重置MediaPlayer
                    }
                    break;

                case R.id.imgPlay:  // 播放
                    if(falgPause)   //  如果是暫停狀態就繼續播放
                    {
                        mediaplayer.start();
                        falgPause = false;
                    }
                    else    //  非暫停狀態則重頭撥放
                    {
                        playVideo(VIDEOPATH + videofile[cListItem]);
                    }
                    break;

                case R.id.imgPause: //  暫停
                    mediaplayer.pause();
                    falgPause = true;
                    break;

                case R.id.imgNext:  // 下一首
                    nextVideo();
                    break;

                case R.id.imgEnd:   //結束
                    mediaplayer.release();
                    finish();
                    break;
            }
        }
    };

    //  ListView 監聽事件
    private ListView.OnItemClickListener listListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            cListItem = position;   // 取得點選位置
            playVideo(VIDEOPATH + videofile[cListItem]);   //  播放
        }
    };

    private void playVideo(String path)
    {
        mediaplayer.reset();
        mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaplayer.setDisplay(sufHolder);

        try
        {
            mediaplayer.setDataSource(path);    //  播放影片路徑
            mediaplayer.prepare();
            mediaplayer.start();    //  開始播放
            txtVideo.setText(" 影片名稱 : " + videoname[cListItem]);    //  更新名稱
            mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer arg0)
                {
                    nextVideo();    //  播放完後撥下一片
                }
            });
        }
        catch(IOException e)
        {
            falgPause =false;
        }
    }

    //  nextVideo (下一部) 方法
    private void nextVideo()
    {
        cListItem++;
        if(cListItem >= listVideo.getCount())   //若到最後就移到第一首
            cListItem = 0;
        playVideo(VIDEOPATH + videofile[cListItem]);
    }

    //  上一部 方法
    private void frontVideo()
    {
        cListItem--;
        if(cListItem<0)
            cListItem = listVideo.getCount()-1; // 若到第一首就移到最後
        playVideo(VIDEOPATH + videofile[cListItem]);

    }
}
