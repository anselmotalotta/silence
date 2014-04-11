package com.example.silenseproto;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

/**
 * From http://stackoverflow.com/questions/10665059/how-to-get-noise-level-using-the-built-in-microphone-in-android
 * @author Miki
 *
 */
public class NoiseRecorder1 {

	private Thread recordingThread;
	private int bufferSize = 800;
	private short[][] buffers = new short[256][bufferSize];
	private int[] averages = new int[256];
	private int lastBuffer = 0;

	private AudioRecord recorder;
	private boolean recorderStarted = false;

	private void stopListenToMicrophone()
	{
		if (recorderStarted)
		{
			if (recordingThread != null && recordingThread.isAlive()
					&& !recordingThread.isInterrupted())
			{
				recordingThread.interrupt();
			}
			recorderStarted = false;
		}
	}

	protected void startListenToMicrophone()
	{
		if (!recorderStarted)
		{

			recordingThread = new Thread()
			{
				@Override
				public void run()
				{
					int minBufferSize = AudioRecord.getMinBufferSize(8000,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					recorder = new AudioRecord(AudioSource.MIC, 8000,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 10);
					recorder.setPositionNotificationPeriod(bufferSize);

					recorder.setRecordPositionUpdateListener(
							new AudioRecord.OnRecordPositionUpdateListener() {

								@Override
								public void onPeriodicNotification(AudioRecord recorder) {
									short[] buffer = buffers[++lastBuffer
									                         % buffers.length];
									recorder.read(buffer, 0, bufferSize);
									long sum = 0;
									for (int i = 0; i < bufferSize; ++i)
									{
										sum += Math.abs(buffer[i]);
									}
									averages[lastBuffer % buffers.length] = (int) (sum / bufferSize);
									lastBuffer = lastBuffer % buffers.length;

								}

								@Override
								public void onMarkerReached(AudioRecord recorder) {
									// TODO Auto-generated method stub

								}
							}
							);



					recorder.startRecording();
					short[] buffer = buffers[lastBuffer % buffers.length];
					recorder.read(buffer, 0, bufferSize);
					while (true)
					{
						if (isInterrupted())
						{
							recorder.stop();
							recorder.release();
							break;
						}
					}
				}
			};
			recordingThread.start();

			recorderStarted = true;
		}
	}

	/* What is wrong with it? 
	 * Answer:
	 * 
	 * Just a passing thought and I may be ver very wrong but, amplitude in dB=20xlog(S1/S2). 
	 * I couldn't find this calculation anywhere in your code. what you need to do is get S1, 
	 * which will be the current recorded level and get S2 which needs to be the maximum possible value to record. 
	 * Then calculate the dB value.
	 * 
	 */

}
