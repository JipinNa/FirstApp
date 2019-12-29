package com.example.firstapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;

import com.example.firstapp.R;
import com.example.firstapp.bean.AudioDecoder;
import com.example.firstapp.bean.VideoDecoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    SurfaceView sfv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPlayer();
    }

    private void initView() {
        sfv = findViewById(R.id.sfv);
    }

    private void initPlayer() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mvtest.mp4";
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        VideoDecoder videoDecoder = new VideoDecoder(path,sfv,null);
        threadPool.execute(videoDecoder);
        AudioDecoder audioDecoder = new AudioDecoder(path);
        threadPool.execute(audioDecoder);

        videoDecoder.goOn();
        audioDecoder.goOn();
    }
}
