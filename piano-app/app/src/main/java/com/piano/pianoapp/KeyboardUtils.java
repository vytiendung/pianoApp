package com.piano.pianoapp;

import android.util.Log;
import com.piano.pianoapp.util.Config;
import com.piano.pianoapp.util.Constant;
import com.piano.pianoapp.util.Keyboard;
import com.piano.pianoapp.util.PianoKeyHelper;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;

import java.util.HashMap;

public class KeyboardUtils implements Constant {
	IKeyboard iKeyboard;
	Config config = Config.getInstance();
	private ccColor3B nameBg1CL = ccColor3B.ccc3(255, 207, 212); //pink
	private ccColor3B nameBg2CL = ccColor3B.ccc3(156, 237, 214); //green
	private ccColor3B nameBg3CL = ccColor3B.ccc3(191, 227, 238); //blue

	public KeyboardUtils(IKeyboard iKeyboard) {
		this.iKeyboard = iKeyboard;
	}

	public void onPosResizeKeyboard() {
		float px = Math.abs(((CCLayer)iKeyboard).getPositionRef().x);
		int tmp = (int) (px / (iKeyboard.getPianoSize() * ((CCLayer)iKeyboard).getScaleX() / 52)) + 1;
		int index = iKeyboard.getKeyWhiteMapping().get(tmp);
		int startIndex = index;
		if (index > 0) {
			startIndex = Math.max(index - 2, 0);
		}
		int newKeyPerScreen = Math.round((Config.getInstance().winSize.width / (iKeyboard.getPianoSize() * Config.getInstance().keyScaleX /
				52)));
		final int MAX_COUNT = newKeyPerScreen + 4;
		int counter = 0;

		Keyboard keyboard;
		for (int i = 0; i < 88; ++i) {
			keyboard = iKeyboard.getKeyboardList().get(i);
			if (keyboard == null) continue;
			if (keyboard.getParent() != null) {
				keyboard.nodeToParentTransform();
				boolean isWhite = !keyboard.isBlack();
				index = keyboard.getIndex();
				boolean needVisible;
				if (index >= startIndex) {
					if (isWhite)
						++counter;
					if (counter <= MAX_COUNT) {
						needVisible = true;
					} else {
						needVisible = false;
					}
				} else {
					needVisible = false;
				}
				keyboard.setVisible(needVisible);

				if (keyboard.getLabel() != null) {
					keyboard.getLabel().setVisible(needVisible);
				}

				if (keyboard.getLine() != null) {
					keyboard.getLine().setVisible(needVisible);
				}
			}
		}
	}
	public String shiftStartNote(String startNote, HashMap<String, Integer> noteMaping) { // to move start note to the center
		try {
			int index = noteMaping.get(startNote.toLowerCase());
			int delta = config.keyPerScreen / 2;
			int count = 0;
			while (true) {
				index--;
				if (!config.noteList.get(index).contains("m")) {
					count++;
				}

				if (index == 1 || count == delta) {
					return config.noteList.get(index);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return startNote;
	}
	/*
	* draw piano keyboard in layer;
	*/
	public void setKeyboardPosition() {
		Config config = Config.getInstance();
		float widthWhite = config.keyWidthWhite;
		float heightWhite = config.keyHeightWhite;
		CGSize whiteSize = CGSize.make(widthWhite, heightWhite);

		float widthBlack = config.keyWidthBlack;

		float heightBlack = config.keyHeightBlack;
		CGSize blackSize = CGSize.make(widthBlack, heightBlack);

		float posXWhite = widthWhite / 2f;
		float posXBlack = 0f;

		float posYWhite = heightWhite / 2f;
		float posYBlack = heightWhite - heightBlack / 2f;

		int op = 1;
		iKeyboard.setPianoSize(0f);
		int whitecount = 1;

		float scl = ((float) config.winSize.width / 12) / 4 / CCLabel.makeLabel("AA", "DroidSans", 18).getContentSize()
				.width; // use 12 key as standard to calculate label+ rect size

		CCSpriteFrame sf;
		CCSprite fade;
		OneLabel label;
		float labelSizeWidth = ((float) config.winSize.width / 12) / 2; // use 12 key as standard to calculate label+ rect
		// size
		for (int i = 1; i <= config.noteList.size(); i++) {

			if (config.noteList.get(i - 1).contains("m")) {

				switch (op) {
					case 1:
						posXBlack += widthWhite + widthWhite * DELTA_BLACK_POS;
						break;
					case 2:
						posXBlack += widthWhite * 2 - widthWhite * DELTA_BLACK_POS * 2;
						break;
					case 3:
						posXBlack += widthWhite + widthWhite * DELTA_BLACK_POS * 2;
						break;
					case 4:
						posXBlack += widthWhite * 2 - widthWhite * DELTA_BLACK_POS * 2;
						break;
					case 5:
						posXBlack += widthWhite + widthWhite * DELTA_BLACK_POS;
						break;
					default:
						break;

				}

				sf = iKeyboard.getFrameCache().spriteFrameByName("stroke.png");
				fade = CCSprite.sprite(sf);
				fade.setScaleX((config.keyWidthBlack * 0.5f) / fade.getContentSize().width);
				fade.setScaleY((config.winSize.height - config.keyHeightWhite) / fade.getContentSize().height);
				fade.setAnchorPoint(0.5f, 0f);
				fade.setPosition(posXBlack, config.keyHeightWhite);
				iKeyboard.getLines().put(i, fade);
				if (iKeyboard.getStrokeLayer() != null)
					iKeyboard.getStrokeLayer().addChild(fade, 0);

				Keyboard keyboard = drawKeyboardBlack(config.noteList.get(i - 1), i, CGPoint.ccp(posXBlack, posYBlack),
						blackSize, op);

				iKeyboard.getKeyboardList().put(i, keyboard);
				keyboard.setLine(fade);
				op++;
				if (op > 5)
					op = 1;

			} else {
				String name = config.noteList.get(i - 1);
				Keyboard keyboard = drawKeyboardWhite(name, i, CGPoint.ccp(posXWhite, posYWhite), whiteSize);

				label = iKeyboard.getNoteLabelMapping().get(name);

				label.setLabelColor(ccColor3B.ccBLACK);
				label.setLabelScale(scl);

				if (config.noteLabelStyle == STYLE_BG_TEXT || (PianoKeyHelper.checkIsKeyboardC(i) && config.noteLabelStyle
						== STYLE_C_ONLY)) {
					sf = iKeyboard.getFrameCache().spriteFrameByName("name_bg.png");
					label.createBackgroundLabel(sf);
					if (name.contains("0") || name.contains("3") || name.contains("6")) {
						label.setBgColor(nameBg1CL);
					} else if (name.contains("1") || name.contains("4") || name.contains("7")) {
						label.setBgColor(nameBg2CL);
					} else {
						label.setBgColor(nameBg3CL);
					}

					float scr = labelSizeWidth / label.getBg().getContentSize().width;
					label.setBgScale(scr);
				} else {
					if (name.contains("0") || name.contains("3") || name.contains("6")) {
						label.setLabelColor(ccColor3B.ccc3(247, 148, 29));
					} else if (name.contains("1") || name.contains("4") || name.contains("7")) {
						label.setLabelColor(ccColor3B.ccc3(0, 114, 54));
					} else {
						label.setLabelColor(ccColor3B.ccc3(0, 114, 188));
					}
				}
				label.setLabelPosition(labelSizeWidth / 2, labelSizeWidth / 2);
				label.setPosition(posXWhite - widthWhite * 0.4f, heightWhite / 14f);
				label.setTag(i);
				iKeyboard.getLabelList().put(i, label);

				if (iKeyboard.getLabelLayer() != null)
					iKeyboard.getLabelLayer().addChild(label, 2);

				posXWhite += widthWhite;
				int newPianoSize = (int) (iKeyboard.getPianoSize() + widthWhite);
				iKeyboard.setPianoSize(newPianoSize);

				iKeyboard.getKeyboardList().put(i, keyboard);
				iKeyboard.getKeyWhiteMapping().put(whitecount, i);
				keyboard.setLabel(label);
				whitecount++;
			}
		}
	}

	private Keyboard drawKeyboardBlack(String key, int tag, CGPoint position, CGSize size, int type) {
		Keyboard keyboard = new Keyboard(position, size, iKeyboard.getFrameCache(), true);
		keyboard.setTag(tag);
		keyboard.setNote(key);
		keyboard.setIndex(tag);
		keyboard.setAnchorY(position.y);
		iKeyboard.getNoteMapping().put(key, tag);
		return keyboard;
	}

	private Keyboard drawKeyboardWhite(String key, int tag, CGPoint position, CGSize size) {
		Keyboard keyboard = new Keyboard(position, size, iKeyboard.getFrameCache(), false);
		keyboard.setTag(tag);
		keyboard.setNote(key);
		keyboard.setIndex(tag);
		keyboard.setAnchorY(position.y - position.y / 2f);
		iKeyboard.getNoteMapping().put(key, tag);
		return keyboard;
	}

	public int getTouchedKeyboardIndex(CGPoint location) {
		try {
			float touchXToKeyboard = location.x + Math.abs(((CCLayer)iKeyboard).getPosition().x);

			float oneWhiteKeyWidth = iKeyboard.getPianoSize() * ((CCLayer)iKeyboard).getScaleX() / Constant.NUM_WHITE_KEY;
			int tmp = (int) (touchXToKeyboard / oneWhiteKeyWidth) + 1;
			int whiteKeyIndex = iKeyboard.getKeyWhiteMapping().get(tmp);
			int leftOfWhiteKeyIndex = whiteKeyIndex - 1 > 0 ? whiteKeyIndex - 1 : 1;
			int rightOfWhiteKeyIndex = whiteKeyIndex + 1 <= iKeyboard.getKeyboardList().size() ? whiteKeyIndex + 1
					: iKeyboard.getKeyboardList().size();

			Keyboard whiteKeyCenter = iKeyboard.getKeyboardList().get(whiteKeyIndex);
			Keyboard leftKey = iKeyboard.getKeyboardList().get(leftOfWhiteKeyIndex);
			Keyboard rightKey = iKeyboard.getKeyboardList().get(rightOfWhiteKeyIndex);

			if (Config.getInstance().isWideTouchArea) {
				float scaleX = ((CCLayer)iKeyboard).getScaleX();
				float scaleY = ((CCLayer)iKeyboard).getScaleY();

				float distanceFromTouchToWhiteKey = (touchXToKeyboard - (whiteKeyCenter.getPos().x * scaleX)) *
						(touchXToKeyboard - (whiteKeyCenter.getPos().x * scaleX)) + (location.y - (whiteKeyCenter
						.getAnchorY() * scaleY)) * (location.y - (whiteKeyCenter.getAnchorY() * scaleY));

				float distanceFromTouchToLeftKey = (touchXToKeyboard - (leftKey.getPos().x * scaleX)) *
						(touchXToKeyboard - (leftKey.getPos().x * scaleX)) + (location.y - (leftKey.getAnchorY() *
						scaleY)) * (location.y - (leftKey.getAnchorY() * scaleY));

				float distanceFromTouchToRightKey = (touchXToKeyboard - (rightKey.getPos().x * scaleX)) *
						(touchXToKeyboard - (rightKey.getPos().x * scaleX)) + (location.y - (rightKey.getAnchorY() *
						scaleY)) * (location.y - (rightKey.getAnchorY() * scaleY));

				if (distanceFromTouchToLeftKey < distanceFromTouchToWhiteKey && distanceFromTouchToLeftKey <
						distanceFromTouchToRightKey) {
					return leftOfWhiteKeyIndex;
				} else if (distanceFromTouchToRightKey < distanceFromTouchToWhiteKey && distanceFromTouchToRightKey <
						distanceFromTouchToLeftKey) {
					return rightOfWhiteKeyIndex;
				} else {
					return whiteKeyIndex;
				}
			} else {
				if (leftKey.isBlack() && leftKey.isTouchIn(location)) {
					return leftOfWhiteKeyIndex;
				} else if (rightKey.isBlack() && rightKey.isTouchIn(location)) {
					return rightOfWhiteKeyIndex;
				} else {
					return whiteKeyIndex;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -2;
	}

	public void drawKeyboardLabel(int keyIndex) {
		OneLabel oneLabel = iKeyboard.getLabelList().get(keyIndex);
		if (iKeyboard.getLabelList() != null) {
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
					iKeyboard.getGameLayer().addChild(oneLabel, 2);
					oneLabel.showLabel(false);
					break;
				case STYLE_BG_TEXT:
					oneLabel.showLabel(true);
					break;
				case STYLE_C_ONLY:
					if (PianoKeyHelper.checkIsKeyboardC(keyIndex)) {
						iKeyboard.getGameLayer().addChild(oneLabel, 2);
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

	public boolean checkIfTouchOnKeyboard(CGPoint location) {
		float maxKeyboardY = config.keyHeightWhite * ((CCLayer)iKeyboard).getScaleY();
		if (location.y >= maxKeyboardY) {
			return false;
		} else {
			return true;
		}
	}


}
