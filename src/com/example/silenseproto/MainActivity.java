package com.example.silenseproto;

import static com.example.silenseproto.Constants.*;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final int THRESHOLD = 7;
	private final long ONE_SEC = 1000;
	private static final int SAMPLE_DELAY = 75;
	private static final double SOUND_THRESHOLD = 3;

	//Declare the timer
	private Timer timer;

	private SoundDetector soundDetector;
	private Thread soundThread;

	private int seconds = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		soundDetector = new SoundDetector();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(event.getAction() ==  MotionEvent.ACTION_DOWN){
			restartTimer();
		}
		return super.onTouchEvent(event);
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	@Override
	protected void onResume() {
		super.onResume();

		detectSound();
		final TextView textView = (TextView) findViewById(R.id.time);
		textView.setText("Speak!");
		startTimer(3);

	}

	@Override
	protected void onStop() {
		super.onStop();
		stopActivity();
	}


	@Override
	protected void onPause() {
		super.onPause();
		stopActivity();
	};

	private void stopActivity(){
		timer.cancel();
		timer.purge();
		seconds = 0;
		if(soundThread != null){
			soundThread.interrupt();
			soundThread = null;
		}
		soundDetector.stopListening();
	}

	private void restartTimer(){

		Log.i(TAG, "restartTimer");
		timer.cancel();
		timer.purge();
		seconds = 0;
		startTimer(0);
	}

	private void startTimer(int delay){
		final TextView textView = (TextView) findViewById(R.id.time);
		timer = new Timer();
		timer.scheduleAtFixedRate(
				new TimerTask() {

					@Override
					public void run() {

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								String display = String.valueOf(seconds);
								if(seconds <= THRESHOLD){
									textView.setTextColor(Color.BLACK);
								} else {
									display = String.valueOf(THRESHOLD);
									textView.setTextColor(Color.RED);
								}
								textView.setText(display);

								seconds++;
							}
						}
								);

					}
				}, 
				delay * ONE_SEC, 
				ONE_SEC);
	}


	private void detectSound(){
		Log.i(TAG, "startListening");
		soundDetector.startListening();
		soundThread = new Thread(new Runnable() {
			public void run() {
				while(soundThread != null && !soundThread.isInterrupted()){
					//Let's make the thread sleep for a the approximate sampling time
					try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
					double audioLevel = soundDetector.readAudioLevel();
					Log.i(TAG, "Audio Level detected: " + audioLevel);

					if(audioLevel > SOUND_THRESHOLD){
						MainActivity.this.restartTimer();
					}
				}
			}
		});
		soundThread.start();
	}


}
