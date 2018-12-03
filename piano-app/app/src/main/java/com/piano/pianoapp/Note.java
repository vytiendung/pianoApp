package com.piano.pianoapp;

public class Note {
	String name;
	float tickPlusDuration;
	float length;
	boolean isChord;
	boolean isTie;

	private int fingerIndex;
	public int indexZOrder;

	boolean isForPrimary;
	int index;

	int start = -1;


	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public boolean isForPrimary() {
		return isForPrimary;
	}

	public void setForPrimary(boolean isForPrimary) {
		this.isForPrimary = isForPrimary;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getTickPlusDuration() {
		return tickPlusDuration;
	}

	public void setTickPlusDuration(float tickPlusDuration) {
		this.tickPlusDuration = tickPlusDuration;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public boolean isChord() {
		return isChord;
	}

	public void setChord(boolean isChord) {
		this.isChord = isChord;
	}

	public boolean isTie() {
		return isTie;
	}

	public void setTie(boolean isTie) {
		this.isTie = isTie;
	}

	public Note(String name, float tickPlusDuration, float length, boolean isChord,
	            boolean isTie) {
		this(name, tickPlusDuration, length, isChord, isTie, true);
	}


	public Note(String name, float tickPlusDuration, float length, boolean isChord,
	            boolean isTie, boolean isPrimary) {
		this(name, tickPlusDuration, length, isChord, isTie, isPrimary, 0);
	}

	public Note(String name, float tickPlusDuration, float length, boolean isChord,
	            boolean isTie, boolean isPrimary, int fingerIndex) {
		super();
		this.name = name;
		this.tickPlusDuration = tickPlusDuration;
		this.length = length;
		this.isChord = isChord;
		this.isTie = isTie;
		this.isForPrimary = isPrimary;
		this.fingerIndex = fingerIndex;
		this.indexZOrder = 0;
	}

	public int getFingerIndex(){
		return fingerIndex;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
