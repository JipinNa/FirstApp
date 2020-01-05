package com.example.firstapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
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
    }

    private void initView() {
        sfv = findViewById(R.id.sfv);
        sfv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) {
                    return;
                }
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.yali);
                Matrix matrix = new Matrix();
                matrix.postScale(0.5f, 0.45f);
                Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Canvas canvas = holder.lockCanvas();
                canvas.drawBitmap(resizeBitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void initPlayer() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mvtest.mp4";
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        VideoDecoder videoDecoder = new VideoDecoder(path, sfv, null);
        threadPool.execute(videoDecoder);
        AudioDecoder audioDecoder = new AudioDecoder(path);
        threadPool.execute(audioDecoder);

        videoDecoder.goOn();
        audioDecoder.goOn();
    }
}
