package net.majorkernelpanic.streaming.heart;

import java.io.IOException;

import net.majorkernelpanic.streaming.FileStream;
import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.video.VideoQuality;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

/** 
 * Don't use this class directly.
 */
public abstract class HeartStream extends FileStream {

	protected final static String TAG = "HeartStream";
	protected int mSamplingRate;


	/** 
	 * Don't use this class directly.
	 * Uses CAMERA_FACING_BACK by default.
	 */
	public HeartStream() {
	}	

	


	public void setHeartSamplingRate(int samplingRate) {
		mSamplingRate = samplingRate;
	}


	/** Stops the stream. */
	public synchronized void stop() {
		super.stop();
	}

	/**
	 * Prepare the HeartStream, you can then call {@link #start()}.
	 * The underlying Camera will be opened and configured when you call this method so don't forget to deal with the RuntimeExceptions !
	 * Camera.open, Camera.setParameter, Camera.unlock may throw one !
	 */
	public void prepare() throws IllegalStateException, IOException {
		
			super.prepare();
	}

	public abstract String generateSessionDescription() throws IllegalStateException, IOException;

	/** 
	 * Releases ressources associated with the {@link HeartStream}. 
	 * The object can't be reused once this function has been called. 
	 **/
	public void release() {
		stop();
		super.release();
	}

}
