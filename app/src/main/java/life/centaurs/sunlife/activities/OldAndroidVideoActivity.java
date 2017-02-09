package life.centaurs.sunlife.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import life.centaurs.sunlife.R;
import life.centaurs.sunlife.squad.ActivitySquad;
import life.centaurs.sunlife.video.enums.MediaEnum;

import static life.centaurs.sunlife.constants.ActivitiesConstants.SPLASH_SCREEN_BACKGROUND_COLOR;
import static life.centaurs.sunlife.video.enums.MediaEnum.VIDEO;

public class OldAndroidVideoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CAMERA_PERMISSION_RESULT  = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT  = 1;

    private ImageButton imageButtonVideo, imageButtonPhoto, imageButtonRotateCamera, imageButtonFullScreen;
    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;

    private static int cameraId = 0;
    private static boolean fullScreen = false;
    private static boolean isRecording = false;

    private File mVideoFolder;
    private String mVideoFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_old_android_video);

        View viewBackground = this.getWindow().getDecorView();
        viewBackground.setBackgroundColor(SPLASH_SCREEN_BACKGROUND_COLOR);

        createVideoFolder();

        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);

        imageButtonVideo = (ImageButton) findViewById(R.id.imageButtonVideo);
        imageButtonPhoto = (ImageButton) findViewById(R.id.imageButtonPhoto);
        imageButtonRotateCamera = (ImageButton) findViewById(R.id.imageButtonRotateCamera);
        imageButtonFullScreen = (ImageButton) findViewById(R.id.imageButtonFullScreen);

        if (isRecording){
            imageButtonVideo.setImageResource(R.mipmap.btn_video_busy_65);
        } else {
            imageButtonVideo.setImageResource(R.mipmap.btn_video_online_65);
        }
        if (fullScreen){
            imageButtonFullScreen.setImageResource(R.mipmap.btn_full_screen_off);
        } else {
            imageButtonFullScreen.setImageResource(R.mipmap.btn_full_screen_on);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imageButtonVideo:
                if (isRecording){
                    isRecording = false;
                    imageButtonVideo.setImageResource(R.mipmap.btn_video_online_65);
                } else {
                    checkWriteStoragePermission();
                }
                break;
            case R.id.imageButtonRotateCamera:
                if (cameraId == 0){
                    cameraId = 1;
                } else {
                    cameraId = 0;
                }
                onPause();
                onStart();
                ActivitySquad.goFromCurrentActivityToNewActivity(OldAndroidVideoActivity.this
                        , OldAndroidVideoActivity.class);
                break;
            case R.id.imageButtonPhoto:

                break;
            case R.id.imageButtonFullScreen:
                if (fullScreen){
                    fullScreen = false;
                } else {
                    fullScreen = true;
                }
                ActivitySquad.goFromCurrentActivityToNewActivity(OldAndroidVideoActivity.this
                        , OldAndroidVideoActivity.class);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(cameraId);
        setPreviewSize(fullScreen);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(cameraId);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "app will not run without camera services", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isRecording = true;
                imageButtonVideo.setImageResource(R.mipmap.btn_video_busy_65);
                try {
                    createVideoFilename(MediaEnum.VIDEO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "permission successfully granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "app needs to save video to run", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ***********file preparing**************

    private void createVideoFolder(){
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "cameraVideoImage");
        if (!mVideoFolder.exists()){
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFilename(MediaEnum mediaEnum) throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileExpansion = null;
        String prependPref = null;
        switch (mediaEnum){
            case VIDEO:
                prependPref = VIDEO.toString();
                fileExpansion = ".mp4";
                break;
            case PHOTO:
                prependPref = MediaEnum.PHOTO.toString();
                fileExpansion = ".jpg";
                break;
        }
        String prepend = prependPref.concat("_").concat(timestamp).concat("_");;
        File mediaFile = File.createTempFile(prepend, fileExpansion, mVideoFolder);
        return mediaFile;
    }

    private void checkWriteStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                isRecording = true;
                imageButtonVideo.setImageResource(R.mipmap.btn_video_busy_65);
                try {
                    createVideoFilename(MediaEnum.VIDEO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "app need to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            isRecording = true;
            imageButtonVideo.setImageResource(R.mipmap.btn_video_busy_65);
            try {
                createVideoFilename(MediaEnum.VIDEO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
