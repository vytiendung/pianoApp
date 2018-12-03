package com.piano.pianoapp;

import com.piano.pianoapp.util.Config;
import com.piano.pianoapp.util.Constant;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCSpriteFrameCache;

public class GameScene extends CCScene {

	private static String TAG = GameScene.class.getSimpleName();


	public static CCScene getChallengeGameScene(String startNote) {
		CCSpriteFrameCache spriteFrameCache = CCSpriteFrameCache.sharedSpriteFrameCache();
		spriteFrameCache.addSpriteFrames(Config.getInstance().imgPath + "guideNote.plist");
		CCScene scene = CCScene.node();
		InGameKeyboard keyboard = new InGameKeyboard(startNote);
		keyboard.setTag(Constant.KEYBOARD_LAYER_TAG);
		scene.addChild(keyboard.getStrokeLayer());
		scene.addChild(keyboard, 1);
		scene.addChild(keyboard.getLabelLayer(), 2);
		scene.setTag(999);
		return scene;
	}
}
