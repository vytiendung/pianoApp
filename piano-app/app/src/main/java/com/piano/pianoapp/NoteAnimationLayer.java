package com.piano.pianoapp;

import android.view.MotionEvent;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCNode;

import java.util.List;

public class NoteAnimationLayer extends CCLayer {

	public NoteAnimationLayer() {
		super();
		setIsTouchEnabled(false);
	}

	@Override
	public boolean ccTouchesBegan(MotionEvent event) {
		return super.ccTouchesBegan(event);
	}

	public void updateChildScaleX(float scaleX) {
		List<CCNode> children = getChildren();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				NoteSprite noteSprite = (NoteSprite) children.get(i);
				if (noteSprite != null) {
					noteSprite.updateFingerNumberScaleX(scaleX);
				}
			}
		}
	}

	public void updateChildScaleY(float scaleY) {
		List<CCNode> children = getChildren();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				NoteSprite noteSprite = (NoteSprite) children.get(i);
				if (noteSprite != null) {
					noteSprite.updateFingerNumberScaleY(scaleY);
				}
			}
		}
	}
}
