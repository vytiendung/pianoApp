package com.piano.pianoapp;

import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.piano.pianoapp.util.Config;
import com.piano.pianoapp.util.Constant;
import com.piano.pianoapp.util.Keyboard;
import com.piano.pianoapp.util.PianoKeyHelper;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.*;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

public class InGameKeyboard extends CCLayer implements Constant, IKeyboard {
	private static final String TAG = "InGameKeyboard";
	KeyboardListener keyboardListener;
	private Config config = Config.getInstance();
	private KeyboardUtils keyboardUtils;
	private SoundManager soundManager;
	private NoteAnimationLayer noteAnimationLayer;
	private LabelLayer labelLayer;
	private StrokeLayer strokeLayer;

	private Hashtable<Integer, CCSprite> blinking;
	private Hashtable<Integer, CGPoint> listRecentPoint;
	private Hashtable<Integer, Boolean> listMoveState;
	private SparseIntArray touchedKeyboards;
	private ArrayList<Keyboard> keyForTouch;
	public HashMap<String, Integer> noteMaping;
	public SparseArray<Keyboard> keyboardList;
	public SparseArray<CCSprite> line;

	int firstKeyIndex;
	private int lastKeyIndex;
	private SparseArray<OneLabel> labelList;
	private float pianoSize;
	private CCSpriteFrameCache spriteFrameCache;
	private HashMap<String, OneLabel> noteLabelMapping;

	private SparseIntArray keyWhiteMapping;
	protected PhaseScalingKeyboard phaseScalingKeyboardSize = PhaseScalingKeyboard.NO_SCALE;
	private float touchSlop;
	private float baseScaleKeyboardWidth;
	private float baseScaleKeyboardHeight;
	private boolean moveFlag;

	public InGameKeyboard(String startNote) {
		setIsTouchEnabled(true);
		config = Config.getInstance();
		soundManager = SoundManager.getInstance();
		noteAnimationLayer = new NoteAnimationLayer();
		addChild(noteAnimationLayer, 1);
		labelLayer = new LabelLayer();
		labelLayer.setTag(Constant.LABEL_LAYER_TAG);
		strokeLayer = new StrokeLayer();
		strokeLayer.setTag(Constant.STROKE_LAYER_TAG);
		strokeLayer.setAnchorPoint(0.0f, 1.0f);
		setAnchorPoint(0.0f, 0.0f);
		setScaleX(config.keyScaleX);
		setScaleY(config.keyScaleY);
		init(startNote);
	}

	public void setKeyboardListener(KeyboardListener keyboardListener){
		this.keyboardListener = keyboardListener;
	}

	private void init(String startNote) {
		keyboardUtils = new KeyboardUtils(this);
		blinking = new Hashtable<>();
		listRecentPoint = new Hashtable<>();
		listMoveState = new Hashtable<>();
		touchedKeyboards = new SparseIntArray();
		keyForTouch = new ArrayList<>();
		noteMaping = new HashMap<>();
		keyboardList = new SparseArray<>();
		line = new SparseArray<>(36);
		keyWhiteMapping = new SparseIntArray(NUM_WHITE_KEY);
		labelList = new SparseArray<>(NUM_WHITE_KEY);
		touchSlop = ViewConfiguration.get(MyApplication.getInstance()).getScaledTouchSlop();

		initNoteLabelMapping();
		initSprite();
		initKeyboard(startNote);
		int tag;

		tag = firstKeyIndex + (Constant.MAX_KEY_NUM + Constant.EXTRA_KEY_NUM - config.keyPerScreen)
				/ 2;
		setPosition((-((Keyboard) getChildByTag(tag)).getPos().x + ((Keyboard) getChildByTag(tag)).getSize().width /
				2f) * getScaleX(), 0f);
		onPosResizeKeyboard();

	}

	public void initSprite() {
		spriteFrameCache = CCSpriteFrameCache.sharedSpriteFrameCache();
		CCTextureCache.sharedTextureCache().removeTexture(config.imgPath + "keyboard.png");
		spriteFrameCache.addSpriteFrames(config.imgPath + "keyboard.plist");
	}

	private void initNoteLabelMapping() {
		noteLabelMapping = new HashMap<String, OneLabel>(NUM_WHITE_KEY);
		int i;
		NoteLabelManager lbMng = NoteLabelManager.getInstance();
		if (lbMng.labelList == null)
			lbMng.initLabelList();
		noteLabelMapping.clear();
		for (i = 0; i < config.noteList.size(); i++) {
			String s = config.noteList.get(i);
			if (!s.contains("m")) {
				OneLabel label;
				s = s.toUpperCase();
				label = new OneLabel(s, "DroidSans", 18);
				noteLabelMapping.put(config.noteList.get(i), label);
			}
		}
	}

	private void initKeyboard(String startNote) {
		config.keyPerScreen = config.tmpKeyPerScreen = config.keyPerScreen > 0 ? config.keyPerScreen : 10;
		int delta = Constant.MAX_KEY_NUM + Constant.EXTRA_KEY_NUM - config.keyPerScreen; // allow max 20 key per screen
		keyboardUtils.setKeyboardPosition();
		int count = 0;
		int countK = 0;
		int index = noteMaping.get(startNote.toLowerCase()) - delta / 2 >= 1 ? noteMaping.get(startNote) - delta / 2 : 1;
		firstKeyIndex = index;

		while (true) {
			Keyboard keyboard = keyboardList.get(index);
			if (keyboard.getNote().contains("m")) {
				if (getChildByTag(keyboard.getTag()) == null)
					addChild(keyboard, 2);
				if (strokeLayer != null) {
					line.get(index).setVisible(true);
				} else {
					addChild(line.get(index), 0);
				}
			} else {
				if (getChildByTag(keyboard.getTag()) == null)
					addChild(keyboard, 1);
				count++;
				keyboardUtils.drawKeyboardLabel(index);
			}
			countK++;
			if (index + 1 > config.noteList.size()) {
				index = firstKeyIndex;
				break;
			} else {
				index++;
			}
			if (count == config.keyPerScreen + delta)
				break;
		}

		while (count < config.keyPerScreen + delta) {
			index--;
			Keyboard keyboard = keyboardList.get(index);
			Log.d(TAG, "initKeyboard:2 abcxxx" + keyboard.getSize().height + " " + keyboard.getSize().width);
			if (keyboard.getNote().contains("m")) {
				if (getChildByTag(keyboard.getTag()) == null)
					addChild(keyboard, 2);
				if (strokeLayer != null) {
					line.get(index).setVisible(true);
				} else {
					addChild(line.get(index), 0);
				}
			} else {
				if (getChildByTag(keyboard.getTag()) == null)
					addChild(keyboard, 1);
				count++;
				keyboardUtils.drawKeyboardLabel(index);
			}
			countK++;
			if (count == config.keyPerScreen + delta) {
				firstKeyIndex = index;
				break;
			}
		}

		if (labelLayer != null)
			labelLayer.updateChildPos(this);

		lastKeyIndex = firstKeyIndex + countK <= config.noteList.size() ? firstKeyIndex + countK - 1 : config.noteList
				.size();

		Keyboard key = keyboardList.get(lastKeyIndex);
		Log.d(TAG, "==== Last key = " + key.getNote());
		key = keyboardList.get(firstKeyIndex);
		Log.d(TAG, "===== First key = " + key.getNote());
	}

	@Override
	public boolean ccTouchesBegan(MotionEvent event) {
		try {
			touchDownProcess(event);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private void touchDownProcess(MotionEvent event) throws Exception {
		if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN ||
				event.getActionMasked() == MotionEvent.ACTION_DOWN) {

			int pointerActionIndex = event.getActionIndex();
			int pointerId = event.getPointerId(pointerActionIndex);
			CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(pointerActionIndex),
					event.getY(pointerActionIndex)));

			boolean isTouchOnKeyboard = keyboardUtils.checkIfTouchOnKeyboard(location);
			if (isTouchOnKeyboard) {
				int touchedKeyboardIndex = getTouchedKeyboardIndex(location);
				onTouchDownIntoKeyboardEvent(event, touchedKeyboardIndex);
			} else {
				addTouchDownInfoToDataForScaling(pointerId, location);
			}
		}
	}

	private int getTouchedKeyboardIndex(CGPoint location) {
		return keyboardUtils.getTouchedKeyboardIndex(location);
	}

	private void onTouchDownIntoKeyboardEvent(MotionEvent event, int touchedKeyboardIndex) {
		int pointerId = event.getPointerId(event.getActionIndex());
		if (!config.isLockScreen) {
			keyForTouch.clear();
		}
		Keyboard keyboard = ((Keyboard) getChildByTag(touchedKeyboardIndex));
		changeStateKeyboardOnTouchDown(keyboard);
		touchedKeyboards.put(pointerId, touchedKeyboardIndex);
		if (config.isLockScreen && keyForTouch.size() > 0) {
			return;
		}
		actionOnTouchDownIntoKeyboardForNormalMode(event, touchedKeyboardIndex);
	}

	private void changeStateKeyboardOnTouchDown(Keyboard keyboard) {
		keyboard.setDisplayState(Keyboard.ButtonState.CLICKED_STATE);
	}

	private void actionOnTouchDownIntoKeyboardForNormalMode(MotionEvent event, int touchedKeyboardIndex) {
		int pointerId = event.getPointerId(event.getActionIndex());
		Keyboard keyboard = ((Keyboard) getChildByTag(touchedKeyboardIndex));
		keyboardListener.onTouchesBegan(keyboard, pointerId);
		changeStateKeyboardOnTouchDown(keyboard);
		touchedKeyboards.put(pointerId, touchedKeyboardIndex);
		if (keyForTouch.contains(keyboard)) {
			keyForTouch.remove(keyboard);
			processWhenTouchingRightKeyboard();
		}
	}

	private void processWhenTouchingRightKeyboard() {
		stopAllBlinking();
	}

	private void stopAllBlinking() {
		if (!config.isShowAnimGfx)
			return;
		ArrayList<CCSprite> spa = new ArrayList<>(blinking.values());
		for (int i = 0; i < spa.size(); i++) {
			CCSprite sp = spa.get(i);
			sp.removeFromParentAndCleanup(true);
			blinking.remove(i);
		}

	}


	private void addTouchDownInfoToDataForScaling(int pointerId, CGPoint location) {
		listRecentPoint.put(pointerId, location);
		listMoveState.put(pointerId, false);
	}

	@Override
	public boolean ccTouchesMoved(MotionEvent event) {
		try {
			return touchMoveProcess(event);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private boolean touchMoveProcess(MotionEvent event) throws Exception {
		ArrayList<Integer> touchMoveOnBlackBkgIndexes = getTouchMoveOnBlackBkgIndexes(event);
		boolean needCheckMovingOnBlackBkg = true;
		if (touchMoveOnBlackBkgIndexes.size() > 1) {
			needCheckMovingOnBlackBkg = false;
			checkMultiTouchScaleKeyboard(event, touchMoveOnBlackBkgIndexes);
		}

		for (int i = 0; i < event.getPointerCount(); i++) {
			int pointerId = event.getPointerId(i);
			CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(i), event.getY(i)));
			if (listMoveState.containsKey(pointerId)) {
				if (needCheckMovingOnBlackBkg) {
					boolean isMoveStage = listMoveState.get(pointerId);
					float currentX = location.x;
					CGPoint recentPoint = listRecentPoint.get(pointerId);
					float deltaX = currentX - recentPoint.x;

					if (!isMoveStage) {
						if (Math.abs(deltaX) > touchSlop) {
							isMoveStage = true;
							needCheckMovingOnBlackBkg = false;
							listMoveState.put(pointerId, true);
						}
					}

					if (isMoveStage) {
						moveKeyboardHorizontal(deltaX);
						listRecentPoint.put(pointerId, CGPoint.ccp(currentX, recentPoint.y));
					}
				}
			} else {
				//jump here so the touch must be lay on keyboard, not in black bkg
				int touchedKeyboardIndex = getTouchedKeyboardIndex(location);
				if (checkIfTouchMoveToOtherKeyboard(pointerId, touchedKeyboardIndex)) {
					changeTouchedKeyboardOnTouchMove(pointerId, touchedKeyboardIndex);
					stopBlinking();
					Keyboard newTouchedKeyboard = ((Keyboard) getChildByTag(touchedKeyboardIndex));
					keyboardListener.onMoveInNote(newTouchedKeyboard, pointerId);
					if (keyForTouch.remove(newTouchedKeyboard)) {
						processWhenTouchingRightKeyboard(newTouchedKeyboard);
					}

				}
			}
		}
		return true;
	}

	private ArrayList<Integer> getTouchMoveOnBlackBkgIndexes(MotionEvent event) {
		ArrayList<Integer> result = new ArrayList<>();
		for (int indexTouch = 0; indexTouch < event.getPointerCount(); indexTouch++) {
			int pointerId = event.getPointerId(indexTouch);
			if (listMoveState.containsKey(pointerId)) {
				result.add(indexTouch);
				if (result.size() == 2) {
					if (phaseScalingKeyboardSize == PhaseScalingKeyboard.SCALE_WIDTH) {
						break;
					}
				} else if (result.size() == 3) {
					break;
				}
			}
		}
		return result;
	}

	private void checkMultiTouchScaleKeyboard(MotionEvent event, ArrayList<Integer> touchMoveOnBlackBkgIndexes) {
		if (touchMoveOnBlackBkgIndexes.size() == 2 && phaseScalingKeyboardSize != PhaseScalingKeyboard.SCALE_HEIGHT) {
			checkMultiTouchScaleKeyboardWidth(event, touchMoveOnBlackBkgIndexes.get(0),
					touchMoveOnBlackBkgIndexes.get(1));
		} else if (touchMoveOnBlackBkgIndexes.size() == 3 &&
				phaseScalingKeyboardSize != PhaseScalingKeyboard.SCALE_WIDTH) {
			checkMultiTouchScaleKeyboardHeight(event, touchMoveOnBlackBkgIndexes.get(0),
					touchMoveOnBlackBkgIndexes.get(1), touchMoveOnBlackBkgIndexes.get(2));
		}
	}

	private void checkMultiTouchScaleKeyboardWidth(MotionEvent event, int indexTouch1, int indexTouch2) {
		int pointerId1 = event.getPointerId(indexTouch1);
		int pointerId2 = event.getPointerId(indexTouch2);
		CGPoint location1 = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(indexTouch1), event.getY
				(indexTouch1)));
		CGPoint location2 = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(indexTouch2), event.getY
				(indexTouch2)));
		boolean isMoveStage1 = listMoveState.get(pointerId1);
		boolean isMoveStage2 = listMoveState.get(pointerId2);
		float recentX1 = listRecentPoint.get(pointerId1).x;
		float recentX2 = listRecentPoint.get(pointerId2).x;
		float tmpX1 = location1.x;
		float tmpX2 = location2.x;

		boolean needScale = false;
		if (isMoveStage1 || isMoveStage2) {
			needScale = true;
		} else {
			//check if has at least oneTouch has moving stage

			float deltaX1 = tmpX1 - recentX1;
			if (Math.abs(deltaX1) > touchSlop) {
				needScale = true;
				listMoveState.put(pointerId1, true);
			} else {
				float deltaX2 = tmpX2 - recentX2;
				if (Math.abs(deltaX2) > touchSlop) {
					needScale = true;
					listMoveState.put(pointerId2, true);
				}
			}
		}

		if (needScale) {
			float baseDistance = Math.abs(recentX1 - recentX2);
			float currentDistance = Math.abs(tmpX1 - tmpX2);
			if (baseScaleKeyboardWidth == 0) {
				baseScaleKeyboardWidth = getScaleX();
			}
			float scaleX = currentDistance / baseDistance * baseScaleKeyboardWidth;
			phaseScalingKeyboardSize = PhaseScalingKeyboard.SCALE_WIDTH;
			doAdjustKeyboardWidth(scaleX);
		}
	}

	private void doAdjustKeyboardWidth(float scaleX) {
		float oldSc = getScaleX();
		float halfScreen = config.winSize.width / 2f;
		int newKeyPerScreen = Math.round((config.winSize.width / (pianoSize * scaleX / 52)));
		final int maxKeyPerScreen = Constant.MAX_KEY_NUM;
		final int minKeyPerScreen = Constant.MIN_KEY_NUM;
		if (newKeyPerScreen > maxKeyPerScreen || newKeyPerScreen < minKeyPerScreen)
			return;
		config.tmpKeyPerScreen = newKeyPerScreen;
		// scale and move the keyboard, the center of screen seem to be the anchor point
		float newPosX = getPosition().x / oldSc * scaleX - ((halfScreen / oldSc * scaleX) - halfScreen);
		if (newPosX > 0) newPosX = 0;
		if (Math.abs(newPosX) + halfScreen * 2 >= pianoSize * scaleX)
			newPosX = -(pianoSize * scaleX - halfScreen * 2);
		this.setPosition(newPosX, 0);
		this.setScaleX(scaleX);

		if (labelLayer != null)
			labelLayer.updateChildPos(this);

		if (noteAnimationLayer != null) {
			noteAnimationLayer.updateChildScaleX(scaleX);
		}
	}


	private void checkMultiTouchScaleKeyboardHeight(MotionEvent event, int indexTouch1, int indexTouch2, int
			indexTouch3) {
		int pointerId1 = event.getPointerId(indexTouch1);
		int pointerId2 = event.getPointerId(indexTouch2);
		int pointerId3 = event.getPointerId(indexTouch3);
		CGPoint location1 = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(indexTouch1), event.getY
				(indexTouch1)));
		CGPoint location2 = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(indexTouch2), event.getY
				(indexTouch2)));
		CGPoint location3 = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(indexTouch3), event.getY
				(indexTouch3)));
		boolean isMoveStage1 = listMoveState.get(pointerId1);
		boolean isMoveStage2 = listMoveState.get(pointerId2);
		boolean isMoveStage3 = listMoveState.get(pointerId3);
		float recentY1 = listRecentPoint.get(pointerId1).y;
		float recentY2 = listRecentPoint.get(pointerId2).y;
		float recentY3 = listRecentPoint.get(pointerId3).y;
		float tmpY1 = location1.y;
		float tmpY2 = location2.y;
		float tmpY3 = location3.y;

		boolean needScale = false;
		if (isMoveStage1 || isMoveStage2 || isMoveStage3) {
			needScale = true;
		} else {
			//check if has at least oneTouch has moving stage

			float deltaY1 = tmpY1 - recentY1;
			if (Math.abs(deltaY1) > touchSlop) {
				needScale = true;
				listMoveState.put(pointerId1, true);
			} else {
				float deltaY2 = tmpY2 - recentY2;
				if (Math.abs(deltaY2) > touchSlop) {
					needScale = true;
					listMoveState.put(pointerId2, true);
				} else {
					float deltaY3 = tmpY3 - recentY3;
					if (Math.abs(deltaY3) > touchSlop) {
						needScale = true;
						listMoveState.put(pointerId3, true);
					}
				}
			}
		}

		if (needScale) {
			float baseAverageY = Math.abs(recentY1 + recentY2 + recentY3) / 3;
			float currentAverageY = Math.abs(tmpY1 + tmpY2 + tmpY3) / 3;
			if (baseScaleKeyboardHeight == 0) {
				baseScaleKeyboardHeight = getScaleY();
			}
			float scaleY = (currentAverageY - baseAverageY) / 50 * 0.1f + baseScaleKeyboardHeight;
			phaseScalingKeyboardSize = PhaseScalingKeyboard.SCALE_HEIGHT;
			doAdjustKeyboardHeight(scaleY);
		}
	}

	private void doAdjustKeyboardHeight(float scaleY) {
		CCNode k = getChildByTag(keyboardList.get(firstKeyIndex).getTag());
		if (k instanceof Keyboard) {
			if (((Keyboard) k).getNote().contains("m"))
				k = getChildByTag(keyboardList.get(firstKeyIndex + 1).getTag());
		}
		if (k instanceof Keyboard) {
			CGSize ks = ((Keyboard) k).getSize(); // get white key
			if (ks.height * scaleY <= config.winSize.height / Constant.MAX_KEY_HEIGHT_RATIO && ks.height * scaleY >=
					config.winSize.height / Constant.MIN_KEY_HEIGHT_RATIO) {
				this.setScaleY(scaleY);
				if (labelLayer != null)
					labelLayer.updateChildPos(this);
				if (noteAnimationLayer != null)
					noteAnimationLayer.updateChildScaleY(this.getScaleY());
				this.onPostResizeKeyboard();
			}
		}
	}
	private void onPostResizeKeyboard() {
	}

	private void moveKeyboardHorizontal(float deltaX) {
		Log.d(TAG, "moveKeyboardHorizontal: " + deltaX);
		float d = this.getPosition().x + deltaX;
		float maxX = -keyboardList.get(firstKeyIndex).getPos().x *
				getScaleX();
		float minX = -keyboardList.get(lastKeyIndex).getPos().x *
				getScaleX() + config.winSize.width;
		if (d <= (config.winSize.width - pianoSize * getScaleX() - 1))
			d = config.winSize.width - pianoSize * getScaleX();
		else if (d >= 1)
			d = 0;
		else if (d > maxX)
			d = maxX;
		else if (d < minX)
			d = minX;
		this.setPosition(d, 0f);

		if (deltaX < 0) {
			redrawScreenLeft();
		} else {
			redrawScreenRight();
		}
	}

	private void redrawScreenLeft() {
		int extra = (Constant.MAX_KEY_NUM + Constant.EXTRA_KEY_NUM - config.keyPerScreen) / 2;
		float a = Math.abs(getPosition().x) + config.winSize.width;
		Keyboard key = keyboardList.get(lastKeyIndex - extra);
		Keyboard previousKey = keyboardList.get(lastKeyIndex - extra - 1);
		boolean needUpdatePos = true;

		float b = key.getPos().x * getScaleX() - key.getSize().width / 2f * getScaleX() - previousKey
				.getSize().width * getScaleX();
		if (lastKeyIndex != config.noteList.size() && a > b) {

			// dich sang trai
			lastKeyIndex += 1;

			if (lastKeyIndex > config.noteList.size()) {
				lastKeyIndex = config.noteList.size();
				return;
			}

			Keyboard keyboard = keyboardList.get(lastKeyIndex);

			if (keyboard.getNote().contains("m")) {
				if (keyboard.getParent() != this)
					addChild(keyboard, 2);

				if (strokeLayer != null) {
					if (line.get(lastKeyIndex).getParent() != strokeLayer)
						line.get(lastKeyIndex).setVisible(true);
				} else {
					if (line.get(lastKeyIndex).getParent() != this)
						addChild(line.get(lastKeyIndex), 0);
				}
			} else {
				if (keyboard.getParent() != this)
					this.addChild(keyboard, 1);

				redrawKeyboardLabel(lastKeyIndex);
			}

			firstKeyIndex += 1;
			if (labelLayer != null) {
				needUpdatePos = false;
				labelLayer.updateChildPosWithoutScale(this);
			}
		}
		if (needUpdatePos)
			onPosResizeKeyboard();
	}

	private void redrawKeyboardLabel(int keyIndex) {
		OneLabel oneLabel = labelList.get(keyIndex);
		if (labelLayer != null) {
			switch (config.noteLabelStyle) {
				case STYLE_TEXT_ONLY:
					oneLabel.showLabel(false);
					break;
				case STYLE_BG_TEXT:
					oneLabel.showLabel(true);
					break;
				case STYLE_C_ONLY:
					if (PianoKeyHelper.checkIsKeyboardC(keyIndex)) {
						oneLabel.showLabel(true);
					} else {
						oneLabel.hideLabel();
					}
					break;
				case STYLE_NONE:
					oneLabel.hideLabel();
					break;
			}
		} else {
			switch (config.noteLabelStyle) {
				case STYLE_TEXT_ONLY:
					if (oneLabel.getParent() != this)
						addChild(oneLabel, 2);
					oneLabel.showLabel(false);
					break;
				case STYLE_BG_TEXT:
					oneLabel.showLabel(true);
					break;
				case STYLE_C_ONLY:
					if (PianoKeyHelper.checkIsKeyboardC(keyIndex)) {
						if (oneLabel.getParent() != this)
							addChild(oneLabel, 2);
						oneLabel.showLabel(true);
					} else {
						oneLabel.hideLabel();
					}
					break;
				case STYLE_NONE:
					oneLabel.hideLabel();
					break;
			}
		}
	}

	private void redrawScreenRight() {
		int extra = (Constant.MAX_KEY_NUM + Constant.EXTRA_KEY_NUM - config.keyPerScreen) / 2;
		float a = Math.abs(getPosition().x);
		Keyboard key = keyboardList.get(firstKeyIndex + extra);
		Keyboard secondKey = keyboardList.get(firstKeyIndex + extra + 1);
		float b = key.getPos().x * getScaleX() + key.getSize().width / 2f * getScaleX() + secondKey
				.getSize().width * getScaleX();
		boolean needUpdatePos = true;
		if (firstKeyIndex != 1 && a < b) {

			firstKeyIndex -= 1;
			if (firstKeyIndex < 1) {
				firstKeyIndex = 1;
				return;
			}

			Keyboard keyboard = keyboardList.get(firstKeyIndex);

			if (keyboard.getNote().contains("m")) {
				if (keyboard.getParent() != this)
					addChild(keyboard, 2);

				if (strokeLayer != null) {
					if (line.get(firstKeyIndex).getParent() != strokeLayer)
						line.get(firstKeyIndex).setVisible(true);
				} else {
					if (line.get(firstKeyIndex).getParent() != this)
						addChild(line.get(firstKeyIndex), 0);
				}

			} else {
				if (keyboard.getParent() != this)
					addChild(keyboard, 1);

				redrawKeyboardLabel(firstKeyIndex);
			}

			lastKeyIndex -= 1;
			if (labelLayer != null) {
				needUpdatePos = false;
				labelLayer.updateChildPosWithoutScale(this);
			}
		}
		if (needUpdatePos)
			this.onPosResizeKeyboard();
	}

	private boolean checkIfTouchMoveToOtherKeyboard(int pointerId, int touchedKeyboardIndex) {
		if (touchedKeyboardIndex >= firstKeyIndex &&
				touchedKeyboardIndex <= lastKeyIndex && moveFlag == false) {
			if (touchedKeyboards.get(pointerId) == 0 || touchedKeyboardIndex != touchedKeyboards.get(pointerId)) {
				return true;
			}
		}
		return false;
	}


	private void changeTouchedKeyboardOnTouchMove(int pointerId, int touchedKeyboardIndex) {
		changeStateKeyboardOnTouchMove(pointerId, touchedKeyboardIndex);
		touchedKeyboards.put(pointerId, touchedKeyboardIndex);
	}

	private void changeStateKeyboardOnTouchMove(int pointerId, int touchedKeyboardIndex) {
		try {
			Keyboard oldTouchedKeyboard = ((Keyboard) getChildByTag(touchedKeyboards.get(pointerId)));
			changeStateKeyboardOnTouchUp(oldTouchedKeyboard);
		} catch (Exception e) {
		}
		Keyboard newTouchedKeyboard = ((Keyboard) getChildByTag(touchedKeyboardIndex));
		changeStateKeyboardOnTouchDown(newTouchedKeyboard);
	}

	private void changeStateKeyboardOnTouchUp(Keyboard keyboard) {
		if (keyboard.getCurState() != Keyboard.ButtonState.HINT_STATE) {
			keyboard.setDisplayState(Keyboard.ButtonState.NORMAL_STATE);
		}
		if (keyForTouch.contains(keyboard)) {
			keyboard.setDisplayState(Keyboard.ButtonState.HINT_STATE);
		}
	}

	public void stopBlinking() {
		if (!config.isShowAnimGfx)
			return;
		for (Enumeration<Integer> keys = blinking.keys(); keys.hasMoreElements(); ) {
			int index = keys.nextElement();
			CCSprite sp = blinking.get(index);
			if (sp != null) {
				if (touchedKeyboards.indexOfValue(index) < 0 || touchedKeyboards.size() == 0) {
					sp.removeFromParentAndCleanup(true);
					blinking.remove(index);
				}
			}
		}
	}

	private void processWhenTouchingRightKeyboard(Keyboard keyboard) {
		stopAllBlinking();
	}


	@Override
	public boolean ccTouchesEnded(MotionEvent event) {
		try {
			return touchUpProcess(event);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private boolean touchUpProcess(MotionEvent event) throws Exception {
		if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP || event
				.getActionMasked() == MotionEvent.ACTION_UP) {
			int pointerActionIndex = event.getActionIndex();
			int pointerId = event.getPointerId(pointerActionIndex);
			CGPoint location = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(pointerActionIndex),
					event.getY(pointerActionIndex)));
			boolean isTouchOnKeyboard = keyboardUtils.checkIfTouchOnKeyboard(location);
			if (isTouchOnKeyboard) {
				int touchedKeyboardIndex = getTouchedKeyboardIndex(location);
				Keyboard keyboard = ((Keyboard) getChildByTag(touchedKeyboardIndex));
				keyboardListener.onTouchesUp(keyboard, pointerId);
			}
			processPointerUp(pointerId);
			return true;
		}
		return true;
	}

	private void processPointerUp(int pointerId) {
		int keyboardIndex = touchedKeyboards.get(pointerId);
		if (keyboardIndex != 0) {
			soundManager.stopSound(pointerId, DEFAULT_VOLUME);
			Keyboard keyboard = ((Keyboard) getChildByTag(keyboardIndex));
			try {
				changeStateKeyboardOnTouchUp(keyboard);
				touchedKeyboards.delete(pointerId);
				stopBlinking();
			} catch (Exception e) {
			}

			if (touchedKeyboards.size() == 0) {
				stopAllBlinking();
				moveFlag = false;
			}
		} else {
		}
		removeTouchInfoFromDataForScaling(pointerId);
	}
	private void removeTouchInfoFromDataForScaling(int pointerId) {
		if (listMoveState.containsKey(pointerId)) {
			listRecentPoint.remove(pointerId);
			listMoveState.remove(pointerId);

			baseScaleKeyboardWidth = 0;
			baseScaleKeyboardHeight = 0;

			if (listMoveState.size() == 0) {
				phaseScalingKeyboardSize = PhaseScalingKeyboard.NO_SCALE;
			}
		}
	}

	@Override
	public void showHintNote(ArrayList<MidiNote> noteList) {
		try {
			if (noteList == null || noteList.size() == 0)
				return;
			clearHint(keyboardList);
			showHint(noteList);
		} catch (Exception e){
			Log.d(TAG, "showHintNote: err" + e.getMessage());
			e.printStackTrace();
		}

	}

	private void showHint(ArrayList<MidiNote> noteList) {
		MidiNote note;
		Keyboard keyboard = null;
		String name;
		int index = 0;
		int keyboardListSize = keyboardList.size();
		int noteListSize = noteList.size();
		float minPos = 1000000000;
		float maxPos = -1000000000;
		boolean flag = false;
		for (int i = 0; i < noteListSize; ++i) {
			note = noteList.get(i);
			name = note.name;
			for (int j = 0; j < keyboardListSize; j++) {
				keyboard = keyboardList.get(j);
				if (keyboard != null) {
					if (keyboard.getNote().equalsIgnoreCase(name)) {
						if (touchedKeyboards.indexOfValue(index) < 0 && keyboard.getCurState() != Keyboard.ButtonState.CLICKED_STATE) {
							keyboard.setDisplayState(Keyboard.ButtonState.HINT_STATE);
						}
						break;
					}
				}
			}
			if (keyboard != null) {
				flag = true;
				if (keyboard.getPos().x > maxPos) {
					maxPos = keyboard.getPos().x;
				}
				if (keyboard.getPos().x < minPos) {
					minPos = keyboard.getPos().x;
				}
			}
			if (!keyForTouch.contains(keyboard)) {
				keyForTouch.add(keyboard);
			}
		}
		if (flag) {
			focusHint(minPos, maxPos);
		}
		Log.d(TAG, "showHint  : " + this.getPosition().x);
	}

	private void focusHint(float minPos, float maxPos) {
		final float finalMinPos = minPos;
		final float finalMaxPos = maxPos;
		(keyboardListener.getActivity()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						try {
							moveLayer(finalMinPos, finalMaxPos);
						} catch (Exception ex){
							ex.printStackTrace();
						}
					}
				}, 100);
			}
		});
	}

	private void moveLayer(float minPos, float maxPos) {
		minPos = minPos * getScaleX();
		maxPos = maxPos * getScaleX();
		float d;
		float layerPos = this.getPosition().x;
		float absLayerPos = Math.abs(layerPos);

		float newPianoSize = pianoSize * getScaleX();
		float keyWidthWhite = newPianoSize / 52;
		float keyWidthBlack = keyWidthWhite * WIDTH_BLACK_RATIO;
		float keyHalfWidthWhite = keyWidthWhite * 0.5f;
		if (minPos - absLayerPos - keyHalfWidthWhite < 0) {
			d = layerPos + (absLayerPos - minPos) + keyHalfWidthWhite;
			if (d >= 1)
				d = 0f;
			for (float i = layerPos; i <= d + keyWidthBlack; i += keyWidthBlack) {
				moveKeyboardHorizontal(keyWidthBlack);
			}
		} else if (maxPos - absLayerPos + keyHalfWidthWhite > config.winSize.width) {
			d = layerPos - (maxPos - absLayerPos - config.winSize.width + keyHalfWidthWhite);
			if (d <= (config.winSize.width - newPianoSize - 1))
				d = config.winSize.width - newPianoSize;
			for (float i = layerPos; i >= d - keyWidthBlack; i -= keyWidthBlack) {
				moveKeyboardHorizontal(-keyWidthBlack);
			}
		}
	}
	@Override
	public void moveNoteToCenter(String note){
		try {
			Keyboard keyboard = getKeyboardByNote(note);
			if (keyboard != null) {
				float posX = Math.abs(this.getPosition().x);
				float noteX = keyboard.getPos().x * this.getScaleX() + config.deviceWidth / 2;
				Log.d(TAG, "moveNoteToCenter: " + (posX - noteX));
				moveKeyboardHorizontal(posX - noteX);
			}
		} catch (Exception ex){
			Log.d(TAG, "moveNoteToCenter: err;");
		}
	}

	private Keyboard getKeyboardByNote(String note) {
		Keyboard keyboard = null;
		for (int j = 0; j < keyboardList.size(); j++) {
			keyboard = keyboardList.get(j);
			if (keyboard != null) {
				if (keyboard.getNote().equalsIgnoreCase(note)) {
					break;
				}
			}
		}
		return keyboard;
	}

	@Override
	public void clearHint() {
		clearHint(keyboardList);
	}

	private void clearHint(Keyboard keyboard) {
		if (keyboard != null)
			keyboard.setDisplayState(Keyboard.ButtonState.NORMAL_STATE);
		keyForTouch.remove(keyboard);
	}

	private void clearHint(SparseArray<Keyboard> keyboardList) {
		Keyboard keyboard;
		for (int i = 0; keyboardList != null && i < keyboardList.size(); i++) {
			keyboard = keyboardList.get(i);
			if (keyboard != null) {
				if (keyboard.getCurState() != Keyboard.ButtonState.CLICKED_STATE)
					keyboard.setDisplayState(Keyboard.ButtonState.NORMAL_STATE);
			}
		}
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		if (labelLayer != null) {
			labelLayer.setPosition(x, y);
		}
		if (strokeLayer != null) {
			strokeLayer.setPosition(x, y);
		}
	}

	@Override
	public void setScaleX(float sx) {
		super.setScaleX(sx);
		config.keyScaleX = sx;
		SettingsManager.getInstance().putFloat(KEY_SCALE_X, sx);
		if (strokeLayer != null)
			strokeLayer.setScaleX(sx);
	}

	@Override
	public void setScaleY(float sy) {
		super.setScaleY(sy);
		config.keyScaleY = sy;
		SettingsManager.getInstance().putFloat(KEY_SCALE_Y, sy);
		if (sy < 1 && strokeLayer != null) strokeLayer.setScaleY(1 / sy);

	}

	@Override
	public void onPosResizeKeyboard() {
		keyboardUtils.onPosResizeKeyboard();
	}

	@Override
	public float getPianoSize() {
		return pianoSize;
	}

	@Override
	public void setPianoSize(float size) {
		pianoSize = size;
	}

	@Override
	public SparseIntArray getKeyWhiteMapping() {
		return keyWhiteMapping;
	}

	@Override
	public SparseArray<Keyboard> getKeyboardList() {
		return keyboardList;
	}

	@Override
	public HashMap<String, Integer> getNoteMapping() {
		return noteMaping;
	}

	@Override
	public NoteAnimationLayer getNoteAnimationLayer(){
		return noteAnimationLayer;
	}

	@Override
	public CCSpriteFrameCache getFrameCache() {
		return spriteFrameCache;
	}

	@Override
	public SparseArray<CCSprite> getLines() {
		return line;
	}

	@Override
	public StrokeLayer getStrokeLayer() {
		return strokeLayer;
	}

	@Override
	public HashMap<String, OneLabel> getNoteLabelMapping() {
		return noteLabelMapping;
	}

	public LabelLayer getLabelLayer() {
		return labelLayer;
	}

	@Override
	public SparseArray<OneLabel> getLabelList() {
		return labelList;
	}

	@Override
	public CCLayer getGameLayer() {
		return this;
	}

	@Override
	public ArrayList<Keyboard> getKeyForTouch() {
		return keyForTouch;
	}

	protected enum PhaseScalingKeyboard {
		NO_SCALE, SCALE_WIDTH, SCALE_HEIGHT
	}

}
