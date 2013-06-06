/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.streaming.rtp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Environment;
import android.util.Log;

/**
 * 
 *   RFC 3984.
 *   
 *   H.264 streaming over RTP.
 *   
 *   Must be fed with an InputStream containing H.264 NAL units preceded by their length (4 bytes).
 *   The stream must start with mpeg4 or 3gpp header, it will be skipped.
 *   
 */
public class HeartRatePacketizer extends AbstractPacketizer implements Runnable{

	public final static String TAG = "HeartRatePacketizer";

	private final static int MAXPACKETSIZE = 1400;
	private int samplingRate = 8000;

	private Thread t = null;
	private int naluLength = 0;
	private long delay = 0;
	private Statistics stats = new Statistics();
	BufferedReader b;

	public HeartRatePacketizer() throws IOException {
		super();
	    

	}

	public void start() throws IOException {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	public void stop() {
		try {
			is.close();
		} catch (IOException ignore) {}
		t.interrupt();
		t = null;
	}

	public void run() 
	{

		long oldtime = System.nanoTime(), now = oldtime, measured = 0, lastmeasured = 5000, expected = 0;
		
		// We read a NAL units from the input stream and we send them
		try 
		{
			while (!Thread.interrupted()) 
			{

				int i;
				char c;

				int countchars = 0;
				while((i=is.read()) != -1)
				// try to send an rtp packet for each line
				{
					c= (char) i;

					if(i == 10)
					{
						break;
					}
					else
					{
						buffer [countchars] = (byte)c;
						countchars++;
					}
				}

				

				// Number of AAC frames in the ADTS frame
				//nbau = (buffer[rtphl+6]&0x03) + 1;
				
				// The number of RTP packets that will be sent for this ADTS frame
				//nbpk = frameLength/MAXPACKETSIZE + 1;

				// We update the RTP timestamp
				ts +=  1024; //stats.average()*samplingRate/1000000000;
				socket.updateTimestamp(ts);
				
				// We send one RTCP Sender Report every 5 secs
				if (intervalBetweenReports>0) {
					if (delta>=intervalBetweenReports) {
						delta = 0;
						report.send(now,ts);
					}
				}
				
				socket.markNextPacket();
				
				//Log.d(TAG,"frameLength: "+frameLength+" protection: "+protection+ " length: "+length);
				
				send(countchars);

				// We wait a little to avoid sending to many packets too quickly
				now = System.nanoTime();
				measured = (now-oldtime)/1000000;
				delta += measured;
				oldtime = now;
				int nbpk = 1, nbau = 1;

				expected = nbau*1024*1000 / (nbpk*samplingRate);
				//Log.d(TAG,"expected: "+ expected + " measured: "+measured);
				measured -= lastmeasured<2*expected/3 ? 2*expected/3-lastmeasured : 0;
				lastmeasured = measured;
				if (measured<2*expected/3) {
					Thread.sleep( 2*expected/3-measured );
				}
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.d(TAG,"H264 packetizer stopped !");

	}

	/*
	// Reads a NAL unit in the FIFO and sends it
	// If it is too big, we split it in FU-A units (RFC 3984)
	private void send() throws IOException, InterruptedException {
		int sum = 1, len = 0, type;

		// Read out a line of the file
		// A buffer
		//byte[] buffer = new byte[20];

	 	//len = fill();
		//System.out.println("Packetize the input stream from the file");

		//Toast.makeText(getApplicationContext(), "File contains: " + new String(buffer), Toast.LENGTH_LONG).show();
			//Toast.makeText(getApplicationContext(), "File contains: " + line, Toast.LENGTH_LONG).show();
		// Read the input stream into a buffer
		//is.read(buffer, 0, 20);
		
		
		//type = buffer[rtphl]&0x1F;

		ts += delay*9/100000;
		socket.updateTimestamp(ts);

		//Log.d(TAG,"- Nal unit length: " + naluLength + " delay: "+delay+" type: "+type);

		socket.markNextPacket();
		super.send(len);
		//Log.d(TAG,"----- Single NAL unit - len:"+len+" header:"+printBuffer(rtphl,rtphl+3)+" delay: "+delay+" newDelay: "+newDelay);
		
	}*/
	
	/*
		private int fill() throws IOException {
			int sum = 0, len;
			
			String line = new String();
			while((line = b.readLine()) != null)
			{
				len = is.read(buffer, 0, line.length());

				if (len<0) {
					throw new IOException("End of stream");
				}
				else
				sum+=len;
			}

			return sum;

		}
*/
/*		
		private int fill(int offset,int length) throws IOException {
		int sum = 0, len;

		while (sum<length) {
			len = is.read(buffer, offset+sum, length-sum);
			if (len<0) {
				throw new IOException("End of stream");
			}
			else sum+=len;
		}

		return sum;

	}*/


}