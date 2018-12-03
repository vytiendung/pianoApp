package com.piano.pianoapp.util;

public class PianoKeyHelper {
	public static boolean checkIsKeyboardC(int keyIndex){
		return (keyIndex == 4 || keyIndex == 16 || keyIndex == 28 || keyIndex == 40 || keyIndex == 52 ||
				keyIndex == 64 || keyIndex == 76 || keyIndex == 88 || keyIndex == 100);
	}
}
