package com.piano.pianoapp.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.piano.pianoapp.CallbackGamePlay;
import com.piano.pianoapp.GamePlayController;
import com.piano.pianoapp.GameScene;
import com.piano.pianoapp.R;
import com.piano.pianoapp.util.Config;
import com.piano.pianoapp.util.Constant;
import com.vtd.cocos2d.EnterFrameListener;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.types.CGSize;

public class InGameActivity extends AppCompatActivity implements Constant, EnterFrameListener,
		CallbackGamePlay {

	static final String TAG = InGameActivity.class.getSimpleName();
	private CCGLSurfaceView mGLSurfaceView;
	private RelativeLayout rlGame;
	private Config config;
	private GamePlayController gamePlayController;
	private CCScene gameScene;
	public boolean isShowDialogPaused = false;
	private boolean isPlayGameCompleted = false;
	String songPath = "";
	String songTitle = "";
	String songAuthor = "";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLayoutNoMenuBar();
		setContentView(R.layout.ingame_layout);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey("songPath"))
				songPath = bundle.getString("songPath");
			if (bundle.containsKey("songTitle"))
				songTitle = bundle.getString("songTitle");
			if (bundle.containsKey("songAuthor"))
				songAuthor = bundle.getString("songAuthor");
		}
		songPath = "song/1_Brahms_Lullaby.ruby";
		config = Config.getInstance();
		config.winSize = CGSize.make(config.winWidth, config.winHeight);
		rlGame = findViewById(R.id.gameview);
		mGLSurfaceView = new CCGLSurfaceView(this);
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		rlGame.addView(mGLSurfaceView, 0);
		CCDirector.sharedDirector().attachInView(mGLSurfaceView);
		CCDirector.sharedDirector().setScreenSize(config.winWidth, config.winHeight);
		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60);
		gameScene = GameScene.getChallengeGameScene(config.beginPosition);
		CCDirector.sharedDirector().runWithScene(gameScene);
		mGLSurfaceView.setEnterFrameListener(this);
		gamePlayController = new GamePlayController(gameScene, this, songPath);
		gamePlayController.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		onPauseClick();
	}



	private void setLayoutNoMenuBar() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams
				.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void onPauseClick() {
		if (!isPlayGameCompleted && gamePlayController.isPlaying()) {
			isShowDialogPaused = true;
			gamePlayController.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		gamePlayController.resume();
	}

	@Override
	public void setScore(final int score) {
		Log.d(TAG, "setScore: score" + score);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				tvScoreValue.setText(String.valueOf(score));
			}
		});
	}

	@Override
	public void setCombo(final int combo) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "run: score setCombo : " + combo);
//				tvComboValue.setText("x" + combo);
			}
		});
	}


	public void resumePlay() {
		gamePlayController.resume();
	}

	public void restartPlay() {
		Log.d(TAG, "restartPlay: start");
		gamePlayController.start();
	}

	public void quitGame() {
		goBackChallengeActivity();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "==============================Configuration change");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onEnterFrame(long deltaTime) {
		gamePlayController.onEnterFrame(deltaTime);
	}

	@Override
	public void onGameComplete() {
		int noteHit = gamePlayController.getNoteHit();
		int numNote = gamePlayController.getNumNote();
		int onTime = gamePlayController.getOnTime();
		int totalScore = gamePlayController.getScore();
		int bonus = gamePlayController.getRubyReward();
		isPlayGameCompleted = true;
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	public void nextPlayMusic() {
		goBackChallengeActivity();
	}

	private void goBackChallengeActivity() {
		Intent data = new Intent();
		try {
			if (isPlayGameCompleted) {
				int score = gamePlayController.getScore();
				Log.d("", "goBackChallengeActivity: score" + score);
				data.putExtra("score", score);
			}
		} catch (Exception ex) {
		}
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}