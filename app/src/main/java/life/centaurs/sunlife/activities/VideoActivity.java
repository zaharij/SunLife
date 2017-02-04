package life.centaurs.sunlife.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import life.centaurs.sunlife.R;
import life.centaurs.sunlife.video.VideoEditor;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        String str = "-version";
        String[] strarr = new String[]{str};
        VideoEditor videoEditor = new VideoEditor(this);
        videoEditor.executeCommands(strarr);
    }
}
