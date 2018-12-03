package com.piano.pianoapp;


import android.content.Context;
import com.piano.pianoapp.util.Base64Utils;
import com.piano.pianoapp.util.Constant;
import com.piano.pianoapp.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;


public class SongDecoder {
	public static SongData decodeSongDataFromAssetPath(String path) {
		SongData songData = null;
		Context context = MyApplication.getInstance();
		String otherSongPath = getOtherSongPath(path);
		try {
			InputStream rightInput = context.getAssets().open(path);
			String noteData = decodSongData(rightInput);
			String bgNoteData = null;
			if (otherSongPath != null) {
				InputStream leftInput = context.getAssets().open(otherSongPath);
				bgNoteData = decodSongData(leftInput);
			}
			songData = SongDecoder.decodeSongDataToStepFormat(noteData, bgNoteData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return songData;
	}


	private static ArrayList<MidiStep> decodeSongDataToListMidiStep(String noteData) {
		ArrayList<MidiStep> result = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(noteData);

			int size = arr.length();
			float elapsedTime = 0f;
			int stepIndex = 0;
			for (int i = 0; i < size; ++i) {
				JSONArray arrayNotes = arr.getJSONArray(i);

				String name = arrayNotes.getString(0).toLowerCase();
				float length = (float) (arrayNotes.getDouble(1));
				if (name.equalsIgnoreCase("rest")) {
					elapsedTime += length;
					continue;
				}

				boolean isTie = arrayNotes.getBoolean(3);

				if (isTie && stepIndex > 0) {
					MidiStep midiStep = result.get(stepIndex - 1);
					MidiNote prevNote = null;
					for (MidiNote note : midiStep.notes) {
						if (note.name.equals(name)) {
							prevNote = note;
						}
					}
					if (prevNote != null) {
						prevNote.duration += length;
						float lastDuration = midiStep.duration;
						midiStep.duration = Math.max(lastDuration, prevNote.duration);
						elapsedTime += (-lastDuration + midiStep.duration);
						continue;
					}
				}

				boolean isChord = arrayNotes.getBoolean(2);
				int fingerIndex = 0;
				if (arrayNotes.length() == 5) {
					fingerIndex = arrayNotes.getInt(4);
				}

				MidiNote midiNote = new MidiNote();
				midiNote.name = name;
				midiNote.startTime = elapsedTime;
				midiNote.duration = length;
				midiNote.fingerIndex = fingerIndex;
				midiNote.id = NoteUtils.getInstance().getNoteIdFromNoteName(name);
				midiNote.velocity = Constant.DEFAULT_VOLUME;

				if (isChord && stepIndex > 0) {
					MidiStep midiStep = result.get(stepIndex - 1);
					midiNote.startTime = midiStep.startTime;

					float lastDuration = midiStep.duration;
					midiStep.duration = Math.max(lastDuration, length);
					midiStep.notes.add(midiNote);
					elapsedTime += (-lastDuration + midiStep.duration);
				} else {
					MidiStep midiStep = new MidiStep();

					midiStep.notes = new ArrayList<>();
					midiStep.notes.add(midiNote);
					midiStep.startTime = elapsedTime;
					midiStep.duration = length;
					midiStep.index = stepIndex++;
					result.add(midiStep);

					elapsedTime += length;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}


	private static ArrayList<MidiNote> decodeSongDataToBackgroundNote(String jsonData) {
		ArrayList<MidiNote> result = new ArrayList<>();
		try {
			JSONArray arr = new JSONArray(jsonData);
			float elapsedTime = 0f;
			for (int i = 0; i < arr.length(); i++) {
				JSONArray arrayNotes = arr.getJSONArray(i);
				String name = arrayNotes.getString(0).toLowerCase();
				float length = (float) (arrayNotes.getDouble(1));
				if (name.equalsIgnoreCase("rest")) {
					elapsedTime += length;
					continue;
				}

				boolean isTie = arrayNotes.getBoolean(3);
				if (isTie && result.size() > 0 && result.get(result.size() - 1).name.equals(name)) {
					MidiNote midiNote = result.get(result.size() - 1);
					midiNote.duration += length;
					elapsedTime += length;
					continue;
				}

				MidiNote midiNote = new MidiNote();
				midiNote.name = name;
				midiNote.startTime = elapsedTime;
				midiNote.duration = length;
				midiNote.fingerIndex = 0;
				midiNote.id = NoteUtils.getInstance().getNoteIdFromNoteName(name);
				midiNote.velocity = Constant.DEFAULT_VOLUME;
				result.add(midiNote);
				elapsedTime += length;

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static SongData decodeSongDataToStepFormat(String noteData, String bgNoteData) {
		SongData songData = new SongData();
		ArrayList<MidiStep> listStep = decodeSongDataToListMidiStep(noteData);
		songData.steps = listStep;
		if (bgNoteData == null) return songData;
		ArrayList<MidiNote> listBgNote = decodeSongDataToBackgroundNote(bgNoteData);
//		for (int i = listStep.size() - 1; i >= 0; i--) {
//			int listBgNoteSize = listBgNote.size();
//			listStep.get(i).backgroundNotes = new ArrayList<>();
//			for (int j = listBgNoteSize - 1; j >= 0; j--) {
//				MidiNote bgNote = listBgNote.get(j);
//				float offsetTime = bgNote.startTime - listStep.get(i).startTime;
//				if (offsetTime >= 0) {
//					bgNote.offsetTime = offsetTime;
//					listStep.get(i).backgroundNotes.add(bgNote);
//					listBgNote.remove(j);
//				} else break;
//			}
//		}
		songData.backgroundNotes = listBgNote;
		return songData;
	}

	public static SongData decodeSongDataToStepFormat(String noteData) {
		SongData songData = new SongData();
		songData.steps = decodeSongDataToListMidiStep(noteData);
		return songData;
	}

	public static String getOtherSongPath(String path) {
		String p = path.substring(0, path.lastIndexOf("/") + 1);
		String s = path.substring(path.lastIndexOf("/") + 1, path.length());
		int hand = Integer.valueOf(s.substring(0, s.indexOf("_")));
		s = s.substring(s.indexOf("_"), s.length());
		if (hand == Constant.RIGHT_HAND) {
			s = Constant.LEFT_HAND + s;
		} else {
			s = Constant.RIGHT_HAND + s;
		}

		String result = p + s;
		boolean isFileExists = FileUtils.isExist(MyApplication.getInstance(), result);
		return isFileExists ? result : null;
	}


	public static String decodSongData(InputStream input) {
		String result = null;
		try {
			InputStream is = input;
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			is.close();
			String jsonString = writer.toString();

			JSONObject obj = new JSONObject(jsonString);
			JSONObject obj2 = obj.getJSONObject("ruby");
			JSONObject obj3 = obj2.getJSONObject("data");

			result = obj3.getString("base64");
			result = Base64Utils.decode(result.trim());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}
}
