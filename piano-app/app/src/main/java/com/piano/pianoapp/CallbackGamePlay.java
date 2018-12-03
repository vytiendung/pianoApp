package com.piano.pianoapp;

import android.app.Activity;

/**
 * Created by ANDT on 3/1/2017.
 */

public interface CallbackGamePlay {
    void onGameComplete();
	Activity getActivity();
    void setScore(int score);
    void setCombo(int combo);

}
