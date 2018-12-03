package com.piano.pianoapp;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import com.piano.pianoapp.util.CommonUtils;
import com.piano.pianoapp.util.Config;
import com.piano.pianoapp.util.Constant;
import com.piano.pianoapp.util.Keyboard;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.types.CGPoint;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import static com.piano.pianoapp.util.Constant.DEFAULT_VOLUME;
import static com.piano.pianoapp.util.Constant.PIANO_INTRUMENT_ID;

public class GamePlayController implements KeyboardListener {

	public static final String TAG = "GamePlayController";
	public static final int TIME_WAIT_NOTE_SAME_STEP = 100;
	private boolean isPlaying;
	private boolean isPausing;
	private CCSpriteFrameCache spriteFrameCache;
	private CCScene gameScene;
	private ArrayList<MidiStep> midiSteps;
	private Hashtable<Integer, ArrayList<CGPoint>> notePoints;
	private ArrayList<Float> stepDistances;
	private IKeyboard iKeyboard;
	private Config config = Config.getInstance();
	private Vibrator vibrator;
	private NoteAnimationLayer animateLayer;
	private float animateLayerPosY;

	private float topScreen;
	private float bottomScreen;
	private MidiStep currentStep;
	private MidiStep preStep;
	private int nextDrawStep;
	private int nextDeleteStep;
	private double timeFromStart;
	private float timeNeedAdd = 0;
	private Hashtable<Integer, ArrayList<NoteObject> > stepsDraw;
	private ArrayList<MidiNote> backgroundNotes;
	private double currentStepEndTime = 0;
	private long timeClickCurrentNote;
	private SongData songData;
	private ScoreCalculator challengeScore;
	private ArrayList<TouchReceiver> touchReceivers = new ArrayList<>();
	private int numNote;
	private CallbackGamePlay callbackGamePlay;
	private String songPath = "";

	public GamePlayController(CCScene gameScene, CallbackGamePlay callbackGamePlay, String songPath) {
		this.gameScene = gameScene;
		this.callbackGamePlay = callbackGamePlay;
		if (songPath != null)
			this.songPath = songPath;
		init();
	}

	private void init(){
		Log.d(TAG, "init: score");
		iKeyboard = (IKeyboard) gameScene.getChildByTag(Constant.KEYBOARD_LAYER_TAG);
		animateLayer = iKeyboard.getNoteAnimationLayer();
		((InGameKeyboard)iKeyboard).setKeyboardListener(this);
		Activity mContext = CCDirector.sharedDirector().getActivity();
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		challengeScore = ScoreCalculator.getInstance(new ScoreCalculator.ScoreListener() {
			@Override
			public void onScoreUpdated(int score) {
				Log.d(TAG, "onScoreUpdated: score"+ score);
				callbackGamePlay.setScore(score);
			}

			@Override
			public void onComboUpdated(int comboChain, int comboGain) {
				Log.d(TAG, "onComboUpdated: score"+ comboGain);

				callbackGamePlay.setCombo(comboGain);
			}
		});
		challengeScore.enableComboGain();
	}

	private void initSongData() {
		backgroundNotes = new ArrayList<>();
		midiSteps = getMidiStepsForTest(songPath);
		stepDistances = getStepDistances();
		notePoints = getNotePoints();
		stepsDraw = new Hashtable<>();
	}

	public void start() {
		initSongData();
		timeFromStart = 0;
		isPlaying = true;
		topScreen = config.winHeight + 1;
		bottomScreen = 0;
		nextDrawStep = 0;
		nextDeleteStep = 0;
		stepsDraw.clear();
		animateLayer.removeAllChildren(true);
		animateLayerPosY = 0;
		animateLayer.setPosition(0, 0);
		currentStepEndTime = 0;
		backgroundNotes.clear();
		currentStep = null;
		preStep = null;
		backgroundNotes.addAll(songData.backgroundNotes);
		challengeScore.initialize(songData.steps.size());
		touchReceivers.clear();
		callbackGamePlay.setScore(0);
	}

	public void resume(){
		if (isPausing) {
			isPlaying = true;
			isPausing = false;
		}
	}

	public void pause() {
		isPlaying = false;
		isPausing = true;
	}

	public boolean isPlaying(){
		return isPlaying;
	}

	public void onEnterFrame(float deltaTime){
		if (!isPlaying) return;
		if (timeNeedAdd > 0){
			deltaTime = timeNeedAdd;
			timeNeedAdd = 0;
		}
		timeFromStart+= deltaTime;
		float y = timeToDistance(deltaTime);
		Log.d(TAG, "onEnterFrame: " + y);
		animateLayerPosY -= y;
		animateLayer.setPosition(0, animateLayerPosY);
		topScreen += y;
		bottomScreen += y;
		calculateHoldingScoreOfNoteBeingTouched(deltaTime);
		drawNewStepIfNeed();
		if (currentStepEndTime <= timeFromStart) {
			if (currentStep != null)
				challengeScore.onIgnoreNote(currentStep.index);
			updateCurrentNote();
			Log.d(TAG, "onEnterFrame: abcxxx");
			iKeyboard.showHintNote(currentStep.notes);
		}
		playBackgroundNotes();
		deleteStepIfNeed();
		if (currentStep != null && currentStep.index == midiSteps.size()- 1 && currentStepEndTime < timeFromStart){
			gameComplete();
		}
	}

	private float timeToDistance(float time){
		return  config.speed*time/1000;
	}

	private void calculateHoldingScoreOfNoteBeingTouched(float detalTime) {
		for (Iterator<TouchReceiver> iterator = touchReceivers.iterator(); iterator.hasNext(); ) {
			TouchReceiver touchReceiver = iterator.next();
			MidiNote note = touchReceiver.midiNote;
			if (note.startTime + note.duration <= timeFromStart) {
				long holdingTime = System.currentTimeMillis() - touchReceiver.startTime;
				float accuracy = holdingTime / note.duration;
				challengeScore.onNoteEnded(accuracy);
				Log.d(TAG, "calculateHoldingScoreOfNoteBeingTouched: accuracy = " + accuracy);
				iterator.remove();
			} else {
				touchReceiver.score += detalTime;
			}
		}
	}

	public int getNoteHit(){
		return challengeScore.getNumNoteHit();
	}

	public int getNumNote(){
		return numNote;
	}

	public int getScore(){
		return challengeScore.getCurrentScore();
	}

	public int getOnTime(){
//		int percent = 0;
//		if (midiSteps != null && midiSteps.size() > 0) {
//			percent = (int) ((timeFromStart / (midiSteps.get(midiSteps.size() - 1).startTime)) * 100);
//			if (percent > 100)
//				percent = 100;
//		}
		return challengeScore.getOnTime();
	}

	public int getRubyReward(){
		return challengeScore.getRubyReward();
	}

	private void playBackgroundNotes() {
		if (backgroundNotes != null) {
			for (int i = 0; i < backgroundNotes.size(); i++) {
				MidiNote note = backgroundNotes.get(i);
				if (timeFromStart >= note.startTime) {
					if (timeFromStart - note.startTime < TIME_WAIT_NOTE_SAME_STEP) {
						SoundManager.getInstance().playSound(0, note.id,
								note.velocity);
						Log.d(TAG, "playBackgroundNotes: " + note.name );
					}
					backgroundNotes.remove(i);
				} else{
					return;
				}
			}
		}
	}

	private void gameComplete() {
		Log.d(TAG, "onGameComplete: ");
		callbackGamePlay.onGameComplete();
		currentStep = null;
		isPlaying = false;
		animateLayer.removeAllChildren(true);
		iKeyboard.clearHint();
	}

	private void updateCurrentNote() {
		if (currentStep != null && currentStep.index == midiSteps.size() - 1)
			return;
		if (currentStep == null){
			currentStep = midiSteps.get(0);
		} else {
			preStep = currentStep;
			currentStep = midiSteps.get(currentStep.index + 1);
		}
		currentStepEndTime = currentStep.startTime + currentStep.duration;
		ArrayList<NoteObject> currentNoteObj = stepsDraw.get(currentStep.index);
		for (NoteObject note : currentNoteObj){
			note.glow();
		}
	}

	private void drawNewStepIfNeed(){
		while (true){
			try {
				if (midiSteps.size() > nextDrawStep){
					MidiStep step = midiSteps.get(nextDrawStep);
					float distance = stepDistances.get(step.index);
					if (distance <= topScreen){
						drawNoteOfStep(step);
						nextDrawStep++;
					} else{
						break;
					}
				} else{
					break;
				}
			} catch (Exception e){
				Log.d(TAG, "drawNewStepIfNeed: err" + e.getMessage());
				e.printStackTrace();
			}

		}
	}

	private void deleteStepIfNeed(){
		while (true){
			try {
				if (midiSteps.size() > nextDeleteStep){
					MidiStep step = midiSteps.get(nextDeleteStep);
					float distance = stepDistances.get(step.index);
					if (distance + config.deviceHeight + timeToDistance(step.duration) + 50 <= bottomScreen){
						ArrayList<NoteObject> notesDraw = stepsDraw.get(step.index);
						for (int i = 0; i < notesDraw.size(); i++) {
							animateLayer.removeChild(notesDraw.get(i), true);
						}
						stepsDraw.remove(notesDraw);
						notesDraw.clear();
						Log.d(TAG, "deleteStepIfNeed: " + animateLayer.getChildren().size());
						nextDeleteStep++;
					} else{
						break;
					}
				} else{
					break;
				}
			} catch (Exception e){
				Log.d(TAG, "deleteStepIfNeed: err" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void drawNoteOfStep(MidiStep step){
		Log.d(TAG, "drawNoteOfStep: step: " + step.index);
		ArrayList<NoteObject> notesDraw = new ArrayList<>();
		for (int i = 0; i < step.notes.size(); i++) {
			MidiNote note = step.notes.get(i);
			NoteObject noteObject = new NoteObject(note);
			noteObject.setPosition(notePoints.get(step.index).get(i));
			addNoteSpriteFirstToLayer(noteObject);
			notesDraw.add(noteObject);
		}
		stepsDraw.put(step.index, notesDraw);
	}


	private void addNoteSpriteFirstToLayer(CCLayer note) {
		if (note.getParent() != animateLayer) {
			animateLayer.addChild(note);
		}
	}

	private Hashtable<Integer, ArrayList<CGPoint>> getNotePoints(){
		Hashtable<Integer, ArrayList<CGPoint>> notePoints = new Hashtable<>();
		for (int stepIndex = 0; stepIndex < midiSteps.size(); stepIndex++) {
			ArrayList<CGPoint> posList = new ArrayList<>();
			MidiStep step = midiSteps.get(stepIndex);
			for (int noteIndex = 0; noteIndex < step.notes.size(); noteIndex++) {
				MidiNote note = step.notes.get(noteIndex);
				String name = note.name;
				if (iKeyboard.getNoteMapping().containsKey(name)) {
					int tag = iKeyboard.getNoteMapping().get(name);
					Keyboard keyboard = iKeyboard.getKeyboardList().get(tag);
					CGPoint pos = CGPoint.make(keyboard.getPos().x, stepDistances.get(stepIndex)
							+ Config.getInstance().keyHeightWhite);
					posList.add(pos);
				} else {
					CGPoint pos = CGPoint.make(0,0);
					posList.add(pos);
				}

			}
			notePoints.put(stepIndex, posList);
		}
		return notePoints;
	}

	private ArrayList<Float> getStepDistances() {
		Config config = Config.getInstance();
		float d = (config.winHeight - config.keyHeightWhite) * 2;
		config.speed = d / 3.5f;
		float distance = 0;
		ArrayList<Float> distances = new ArrayList<>();
		for (int stepIndex = 0; stepIndex < midiSteps.size(); stepIndex++) {
			MidiStep step = midiSteps.get(stepIndex);
			distance = ((step.startTime) / 1000) * config.speed;
			distances.add(CommonUtils.round(distance, 2));
		}
		return  distances;
	}

	@Override
	public void onTouchesBegan(Keyboard keyboard, int pointerId) {
		Log.d(TAG, "onTouchesBegan: " + keyboard.getNote() + " " + keyboard.getIndex());
		MidiNote note = getMidiNoteClickIfWaitNoteSameStep(keyboard);
		if (isWaitTime() && note != null){
			doIfClickNoteWhenWaitPreStep(note, pointerId);
		} else {
			MidiNote midiNote = getMidiNoteClick(keyboard);
			if (midiNote != null) {
				doIfClickCurrentNote(pointerId, midiNote);
			} else {
				SoundManager.getInstance().playSound(PIANO_INTRUMENT_ID, (keyboard.getIndex() - 1 + 21), pointerId,
						DEFAULT_VOLUME);
				vibrate();
//				challengeScore.onBeganTouchWrongNote(currentStep.index);
			}
		}

	}

	private boolean isWaitTime() {
		long currentTime = System.currentTimeMillis();
		if ((currentTime - timeClickCurrentNote) < TIME_WAIT_NOTE_SAME_STEP)
			return true;
		else
			return false;
	}

	private void doIfClickNoteWhenWaitPreStep(MidiNote note, int pointerId){
		Log.d(TAG, "doIfClickNoteWhenWaitPreStep: ");
		SoundManager.getInstance().playSound(PIANO_INTRUMENT_ID, note.id, pointerId,
				note.velocity);
		vibrate();
		challengeScore.onBeganTouchOtherNoteInCurrentStep(note.id);
		saveFingerPointer(pointerId, note);
		if (currentStep.index > 0) {
			for (NoteObject noteObject : stepsDraw.get(currentStep.index - 1)) {
				if (noteObject.getNoteName().equals(note.name)) {
//					scoreEffectLayer.createScoreLabel(noteObject, bottomScreen, 14);
				}
			}
		}

	}

	private void doIfClickCurrentNote(int pointerId, MidiNote midiNote) {
		timeClickCurrentNote = System.currentTimeMillis();
		float delayTime = (float) (currentStep.startTime - timeFromStart);
		challengeScore.onBeganTouchCorrectNote(currentStep.index, midiNote.id, delayTime);
		saveFingerPointer(pointerId, midiNote);
		for (NoteObject noteObject : stepsDraw.get(currentStep.index)) {
			if (noteObject.getNoteName().equals(midiNote.name)) {
//				scoreEffectLayer.createScoreLabel(noteObject, bottomScreen, 14);
			}
		}

		if (delayTime > 0) {
			timeNeedAdd = delayTime;
		}
		ArrayList<NoteObject> currentNoteObj = stepsDraw.get(currentStep.index);
		for (NoteObject noteObject : currentNoteObj) {
			noteObject.release();
		}
		updateCurrentNote();
		Log.d(TAG, "doIfClickCurrentNote: abcxxx");
		iKeyboard.showHintNote(currentStep.notes);
		SoundManager.getInstance().playSound(PIANO_INTRUMENT_ID, midiNote.id, pointerId,
				midiNote.velocity);
		vibrate();
	}



	private void saveFingerPointer(int pointerId, MidiNote midiNote) {
		TouchReceiver touchReceiver = findTouchReceiver(pointerId);
		long startTime = System.currentTimeMillis();
		if (touchReceiver == null) {
			touchReceiver = new TouchReceiver(pointerId, midiNote, startTime);
			touchReceivers.add(touchReceiver);
			touchReceiver.score = 14;
		}
		else {
			touchReceiver.midiNote = midiNote;
			touchReceiver.startTime = startTime;
			touchReceiver.score = 14;
		}
	}



	private TouchReceiver findTouchReceiver(int pointerId) {
		for (TouchReceiver touchReceiver : touchReceivers) {
			if (touchReceiver.pointerId == pointerId) {
				return touchReceiver;
			}
		}
		return null;
	}

	private MidiNote getMidiNoteClick(Keyboard keyboard){
		MidiNote res = null;
		if (currentStep != null){
			for (MidiNote note : currentStep.notes) {
				if (note.name.equalsIgnoreCase(keyboard.getNote())) {
					res = note;
					break;
				}
			}
		}
		return res;
	}

	private MidiNote getMidiNoteClickIfWaitNoteSameStep(Keyboard keyboard){
		MidiNote res = null;
		if (preStep != null){
			for (MidiNote note : preStep.notes) {
				if (note.name.equalsIgnoreCase(keyboard.getNote())) {
					res = note;
					break;
				}
			}
		}
		return res;
	}

	@Override
	public void onMoveInNote(Keyboard keyboard, int pointerId) {
		Log.d(TAG, "onMoveInNote: " + keyboard.getNote() + " " + keyboard.getIndex());
		SoundManager.getInstance().stopSound(pointerId, DEFAULT_VOLUME);
		MidiNote note = getMidiNoteClickIfWaitNoteSameStep(keyboard);
		if (isWaitTime() && note != null){
			doIfClickNoteWhenWaitPreStep(note, pointerId);
		} else {
			MidiNote midiNote = getMidiNoteClick(keyboard);
			if (midiNote != null) {
				doIfClickCurrentNote(pointerId, midiNote);
			} else {
				SoundManager.getInstance().playSound(PIANO_INTRUMENT_ID,(keyboard.getIndex() - 1 + 21), pointerId,
						DEFAULT_VOLUME);
				vibrate();
				if (challengeScore != null && currentStep != null)
					challengeScore.onBeganTouchWrongNote(currentStep.index);
			}
		}
	}

	@Override
	public void onTouchesUp(Keyboard keyboard, int pointerId) {
		Log.d(TAG, "onTouchesUp: " + keyboard.getNote() + " " + keyboard.getIndex());
		if (getMidiNoteClick(keyboard) != null){
			keyboard.setDisplayState(Keyboard.ButtonState.HINT_STATE);
		}
		TouchReceiver touchReceiver = findTouchReceiver(pointerId);
		if (touchReceiver != null) {
			long holdTime = System.currentTimeMillis() - touchReceiver.startTime;
			float accuracy = holdTime / touchReceiver.midiNote.duration;
			challengeScore.onNoteEnded(accuracy);
			Log.d(TAG, "onTouchesUp: accuracy = " + accuracy);
			boolean result = touchReceivers.remove(touchReceiver);
			Log.d(TAG, "onTouchesUp: removeTouchReciver: result ========== " + result);
		}
	}

	@Override
	public Activity getActivity() {
		return callbackGamePlay.getActivity();
	}

	public void vibrate() {
		if (config.isVibrate)
			vibrator.vibrate(config.vibrateTime);
	}

	public ArrayList<MidiStep> getMidiStepsForTest(String songPath) {
		Log.d(TAG, "getMidiStepsForTest: " + songPath);
		songData =   SongDecoder.decodeSongDataFromAssetPath(songPath);
		ArrayList<MidiStep> midiSteps = songData.steps;
		for (MidiStep step : midiSteps) {
			numNote += step.notes.size();
		}
		return midiSteps;
	}



	class TouchReceiver {
		public int pointerId;
		long startTime;
		MidiNote midiNote;
		public int score = 0;

		TouchReceiver(int pointerId, MidiNote midiNote, long startTime) {
			this.pointerId = pointerId;
			this.midiNote = midiNote;
			this.startTime = startTime;
		}
	}
}
