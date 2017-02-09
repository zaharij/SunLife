package life.centaurs.sunlife.activities;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.IOException;

import life.centaurs.sunlife.R;
import life.centaurs.sunlife.squad.ActivitySquad;

import static life.centaurs.sunlife.constants.ActivitiesConstants.SPLASH_SCREEN_BACKGROUND_COLOR;

public class OldAndroidVideoActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton imageButtonVideoOnline, imageButtonVideoBusy, imageButtonPhoto
            , imageButtonRotateCamera, imageButtonFullScreenOn, imageButtonFullScreenOff;
    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;

    private static int CAMERA_ID = 0;
    private static boolean FULL_SCREEN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_old_android_video);

        View viewBackground = this.getWindow().getDecorView();
        viewBackground.setBackgroundColor(SPLASH_SCREEN_BACKGROUND_COLOR);

        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);

        imageButtonFullScreenOn = (ImageButton) findViewById(R.id.imageButtonFullScreenOn);
        imageButtonFullScreenOff = (ImageButton) findViewById(R.id.imageButtonFullScreenOff);
        imageButtonRotateCamera = (ImageButton) findViewById(R.id.imageButtonRotateCamera);
        imageButtonVideoOnline = (ImageButton) findViewById(R.id.imageButtonVideoOnline);
        imageButtonVideoBusy = (ImageButton) findViewById(R.id.imageButtonVideoBusu);
        imageButtonPhoto = (ImageButton) findViewById(R.id.imageButtonPhoto);
        imageButtonVideoBusy.setVisibility(View.INVISIBLE);
        if (FULL_SCREEN){
            imageButtonFullScreenOn.setVisibility(View.INVISIBLE);
            imageButtonFullScreenOff.setVisibility(View.VISIBLE);
        } else {
            imageButtonFullScreenOff.setVisibility(View.INVISIBLE);
            imageButtonFullScreenOn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imageButtonVideoOnline:
                imageButtonVideoOnline.setVisibility(View.INVISIBLE);
                imageButtonVideoBusy.setVisibility(View.VISIBLE);
                break;
            case R.id.imageButtonVideoBusu:
                imageButtonVideoBusy.setVisibility(View.INVISIBLE);
                imageButtonVideoOnline.setVisibility(View.VISIBLE);
                break;
            case R.id.imageButtonPhoto:
                break;
            case R.id.imageButtonRotateCamera:
                if(CAMERA_ID == 0){
                    CAMERA_ID = 1;
                } else {
                    CAMERA_ID = 0;
                }
                ActivitySquad.goFromCurrentActivityToNewActivity(OldAndroidVideoActivity.this
                        , OldAndroidVideoActivity.class);
                break;
            case R.id.imageButtonFullScreenOn:
                imageButtonFullScreenOff.setVisibility(View.INVISIBLE);
                imageButtonFullScreenOn.setVisibility(View.VISIBLE);
                FULL_SCREEN = true;
                ActivitySquad.goFromCurrentActivityToNewActivity(OldAndroidVideoActivity.this
                        , OldAndroidVideoActivity.class);
                break;
            case R.id.imageButtonFullScreenOff:
                imageButtonFullScreenOn.setVisibility(View.INVISIBLE);
                imageButtonFullScreenOff.setVisibility(View.VISIBLE);
                FULL_SCREEN = false;
                ActivitySquad.goFromCurrentActivityToNewActivity(OldAndroidVideoActivity.this
                        , OldAndroidVideoActivity.class);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        setPreviewSize(FULL_SCREEN);
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
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
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

        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        if (widthIsMax) {
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        if (!fullScreen) {
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        matrix.mapRect(rectPreview);

        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
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

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }
}
