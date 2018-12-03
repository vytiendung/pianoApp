package com.piano.pianoapp;

import android.util.SparseArray;
import android.util.SparseIntArray;
import com.piano.pianoapp.util.Keyboard;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrameCache;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DUNGVT on 2/24/2017.
 */

public interface IKeyboard {
	void onPosResizeKeyboard();
	float getPianoSize();
	void setPianoSize(float size);
	SparseIntArray getKeyWhiteMapping();
	SparseArray<Keyboard> getKeyboardList();
	HashMap<String, Integer> getNoteMapping();
	NoteAnimationLayer getNoteAnimationLayer();
	CCSpriteFrameCache getFrameCache();
	SparseArray<CCSprite> getLines();
	StrokeLayer getStrokeLayer();
	HashMap<String, OneLabel> getNoteLabelMapping();
	LabelLayer getLabelLayer();
	SparseArray<OneLabel> getLabelList();
	CCLayer getGameLayer();
	ArrayList<Keyboard> getKeyForTouch();
	void showHintNote(ArrayList<MidiNote> notes);
	void clearHint();
	void moveNoteToCenter(String note);

}
