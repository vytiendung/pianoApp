package com.piano.pianoapp;

import android.app.Activity;
import com.piano.pianoapp.util.Keyboard;

/**
 * Created by DUNGVT on 2/24/2017.
 */

public interface KeyboardListener {
	void onTouchesBegan(Keyboard keyboard, int pointerId);
	void onMoveInNote(Keyboard keyboard, int pointerId);
	void onTouchesUp(Keyboard keyboard, int pointerId);
	Activity getActivity();
}
