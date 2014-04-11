package com.example.silenseproto;

import static com.example.silenseproto.Constants.*;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class SoundDetector{

	private static final int sampleRate = 8000;

	private int bufferSize;
	private AudioRecord audio;

	public SoundDetector(){

		try {
			bufferSize = AudioRecord
					.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);



		} catch (Exception e) {
			Log.e(TAG, "SoundDetector Exception", e);

		}
	}

	public void startListening(){

		audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		audio.startRecording();

	}

	public void stopListening(){

		try {
			if (audio != null) {
				audio.stop();
				audio.release();
				audio = null;
			}
		} catch (Exception e) {e.printStackTrace();}
		

	}


	/**
	 * Functionality that gets the sound level out of the sample
	 */
	public double readAudioLevel() {
		double level = 0;
		try {
			short[] buffer = new short[bufferSize];

			int bufferReadResult = 1;

			if (audio != null) {

				// Sense the voice...
				bufferReadResult = audio.read(buffer, 0, bufferSize);
				double sumLevel = 0;
				for (int i = 0; i < bufferReadResult; i++) {
					sumLevel += buffer[i];
				}
				level = Math.abs((sumLevel / bufferReadResult));
			}

		} catch (Exception e) {
			Log.e(TAG, "SoundDetector Exception", e);
		}

		return level;
	}

}
