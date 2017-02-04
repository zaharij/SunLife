package life.centaurs.sunlife.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import life.centaurs.sunlife.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View viewBackground = this.getWindow().getDecorView();
        viewBackground.setBackgroundResource(R.color.colorBackground);
    }
}
