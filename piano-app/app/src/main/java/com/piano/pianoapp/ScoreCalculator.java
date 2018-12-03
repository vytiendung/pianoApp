package com.piano.pianoapp;


import com.piano.pianoapp.util.Config;

import java.util.ArrayList;

public class ScoreCalculator {
	private final ScoreOnBeganTouchRule[] scoreOnBeganTouchRules = new ScoreOnBeganTouchRule[] {
			new ScoreOnBeganTouchRule(200, 5),
			new ScoreOnBeganTouchRule(400, 4),
			new ScoreOnBeganTouchRule(600, 3),
			new ScoreOnBeganTouchRule(800, 2),
			new ScoreOnBeganTouchRule(-1, 1)
	};

	private final ComboRule[] comboRules = new ComboRule[] {
			new ComboRule(60, 4),
			new ComboRule(30, 3),
			new ComboRule(10, 2),
			new ComboRule(0, 1)
	};

	private final int maxHoldingScore = 5;

	private int currentScore, totalStep, missCount;
	private int comboGain, comboChain;
	private int lastFailedStepIndex = -1;
	protected Config config;

	private static ScoreCalculator instance;
	private boolean isComboGainEnabled;
	private int currentStepIndex;
	private ArrayList<Integer> playedNoteId;
	private float timeOffset;
	private ScoreListener listener;
	private int numNoteHit, numNotePlayed;
	private float totalAccuracy;

	private ScoreCalculator() {}

	public static ScoreCalculator getInstance(ScoreListener listener) {
		if (instance == null) {
			instance = new ScoreCalculator();
			instance.config = Config.getInstance();
			instance.listener = listener;
		}
		return instance;
	}

	public void initialize(int totalStep) {
		this.totalStep = totalStep;
		currentScore = 0;
		missCount = 0;
		comboGain = 1;
		comboChain = 0;
		lastFailedStepIndex = -1;
		currentStepIndex = -1;
		playedNoteId = new ArrayList<>();
		timeOffset = 0;
		numNoteHit = 0;
		numNotePlayed = 0;
		totalAccuracy = 0;
	}

	public int calculateMaxSongScore(ArrayList<MidiStep> midiSteps) {
		int result = 0;
		int noteCount = 0;
		for (MidiStep midiStep : midiSteps) {
			for (int i = 0; i < midiStep.notes.size(); i++) {
				noteCount++;
				int comboGain = 1;
				for (ComboRule comboRule : comboRules) {
					if (noteCount >= comboRule.chain) {
						comboGain = comboRule.gain;
						break;
					}
				}
				result += comboGain * (scoreOnBeganTouchRules[0].score + maxHoldingScore);
			}
		}
		return result;
	}

	public void onBeganTouchCorrectNote(int stepIndex, int noteId, float timeOffset) {
		boolean willCalculateScore = stepIndex > currentStepIndex || playedNoteId.indexOf(noteId) == -1;
		if (stepIndex > currentStepIndex) {
			playedNoteId.clear();
			playedNoteId.add(noteId);
		} else {
			playedNoteId.add(noteId);
		}
		if (willCalculateScore) {
			this.currentStepIndex = stepIndex;
			this.timeOffset = timeOffset;
			this.numNoteHit++;
			int gainScore = comboGain * scoreOnBeganTouchRules[4].score;
			for (ScoreOnBeganTouchRule scoreOnBeganTouchRule : scoreOnBeganTouchRules) {
				if (timeOffset < scoreOnBeganTouchRule.maxOffsetTime) {
					gainScore = comboGain * scoreOnBeganTouchRule.score;
					break;
				}
			}
			increaseCurrentScore(gainScore);
			if (isComboGainEnabled) {
				increaseComboChain();
			}
		}
	}

	public void onBeganTouchOtherNoteInCurrentStep(int noteId) {
		if (playedNoteId.indexOf(noteId) == -1) {
			playedNoteId.add(noteId);
			numNoteHit++;
			int gainScore = scoreOnBeganTouchRules[4].score;
			for (ScoreOnBeganTouchRule scoreOnBeganTouchRule : scoreOnBeganTouchRules) {
				if (timeOffset < scoreOnBeganTouchRule.maxOffsetTime) {
					gainScore += comboGain * scoreOnBeganTouchRule.score;
					break;
				}
			}
			increaseCurrentScore(gainScore);
			if (isComboGainEnabled) {
				increaseComboChain();
			}
		}
	}

	public void onBeganTouchWrongNote(int currentStepIndex) {
		onMissNote(currentStepIndex);
	}

	public void onIgnoreNote(int currentStepIndex) {
		onMissNote(currentStepIndex);
		numNotePlayed++;
	}

	private void onMissNote(int currentStepIndex) {
		if (currentStepIndex != lastFailedStepIndex) {
			lastFailedStepIndex = currentStepIndex;
			missCount++;
		}
		if (isComboGainEnabled) {
			resetComboChain();
		}
	}

	private void increaseComboChain() {
		comboChain++;
		for (ComboRule comboRule : comboRules) {
			if (comboChain >= comboRule.chain) {
				comboGain = comboRule.gain;
				break;
			}
		}
		notifyOnComboUpdated();
	}

	private void resetComboChain() {
		comboChain = 0;
		comboGain = 1;
		notifyOnComboUpdated();
	}

	public void onNoteEnded(float accuracy) {
		increaseCurrentScore( comboGain * Math.round(maxHoldingScore * Math.min(accuracy, 1)) );
		totalAccuracy += accuracy;
		numNotePlayed++;
	}

	private void increaseCurrentScore(int amount) {
		currentScore += amount;
		if (listener != null) {
			listener.onScoreUpdated(currentScore);
		}
	}

	private void notifyOnComboUpdated() {
		if (listener != null) {
			listener.onComboUpdated(comboChain, comboGain);
		}
	}

	public int getFinalScore() {
		return currentScore * (1 - missCount/totalStep);
	}

	public int getCurrentScore() {
		return currentScore;
	}

	public int getNumNoteHit(){
		return numNoteHit;
	}

	public int getOnTime(){
		return Math.round( totalAccuracy * 100 / numNotePlayed );
	}

	public int getRubyReward(){
		return 0;
	}

	public int getMissCount() {
		return missCount;
	}

	public void enableComboGain() {
		this.isComboGainEnabled = true;
	}

	public void disableComboGain() {
		this.isComboGainEnabled = false;
	}

	private class ScoreOnBeganTouchRule {
		int maxOffsetTime;
		int score;

		ScoreOnBeganTouchRule(int maxOffsetTime, int score) {
			this.maxOffsetTime = maxOffsetTime;
			this.score = score;
		}
	}

	private class ComboRule {
		int gain, chain;
		ComboRule(int chain, int gain) {
			this.chain = chain;
			this.gain = gain;
		}
	}

	public interface ScoreListener {
		void onScoreUpdated(int score);
		void onComboUpdated(int comboChain, int comboGain);
	}
}
