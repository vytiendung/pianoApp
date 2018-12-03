package com.piano.pianoapp.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.piano.pianoapp.R;
import com.piano.pianoapp.SoundManager;
import com.piano.pianoapp.UserConfig;
import com.piano.pianoapp.util.Constant;

public class StartupActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.startup_activity);
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				UserConfig.getInstance().configWindowSize(StartupActivity.this);
				UserConfig.getInstance().initConfig(StartupActivity.this);
				SoundManager.getInstance().loadDefaultSound(0, Constant.PIANO_INTRUMENT_ID);
				startActivity(new Intent(StartupActivity.this, InGameActivity.class));
				finish();
			}
		});

	}
}
