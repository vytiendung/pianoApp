package com.piano.pianoapp;


import java.util.ArrayList;
import java.util.List;

public class NoteUtils {
	private String[] names = new String[] {"c", "d", "e", "f", "g", "a", "b"};
	private int[] sharpNotes = new int[] {1, 3, 6, 8, 10};

	public List<String> notes;

	private NoteUtils() {}

	private static NoteUtils instance;
	public static NoteUtils getInstance() {
		if (instance == null) {
			instance = new NoteUtils();
			instance.notes = new ArrayList<>();
			instance.getNoteData();
		}
		return instance;
	}

	private void getNoteData() {
		ArrayList<String> temp = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			temp.add(names[i]);
			if (i != 2 && i != 6) {
				temp.add(names[i]);
			}
		}

		boolean hasIndex = true;
		for (int i = 1; i <= 7; i++) {
			for (int j = 0; j < temp.size(); j++) {
				String key = temp.get(j);
				if (hasIndex) {
					key += i;
				}
				if (isSharpNote(j)) {
					key += "m";
				}
				notes.add(key);
			}
		}
		String note1 = names[5];
		String note2 = names[5];
		String note3 = names[6];
		String note108 = names[0];
		if (hasIndex) {
			note1 += "0";
			note2 += "0";
			note3 += "0";
			note108 += "8";
		}
		note2 += "m";
		notes.add(0, note1);
		notes.add(1, note2);
		notes.add(2, note3);
		notes.add(note108);
	}

	private boolean isSharpNote(int index) {
		for (int sharpNote : sharpNotes) {
			if (index == sharpNote)
				return true;
		}
		return false;
	}

	public int getNoteIdFromNoteName(String noteName) {
		return notes.indexOf(noteName) + 1 + 20;
	}
}
