//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

package com.freshplanet.nativeExtensions;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.adobe.fre.FREContext;
import com.distriqt.extension.util.Resources;

public class C2DMBroadcastReceiver extends BroadcastReceiver {

	private static String TAG = "c2dmBdcastRcvr";

	private static int notificationIcon;
	private static int applicationIcon;
	
	private static C2DMBroadcastReceiver instance;
	
	public C2DMBroadcastReceiver() {
		
		Log.d(TAG, "Broadcast receiver started!!!!!");
	}

	public static C2DMBroadcastReceiver getInstance()
	{
		return instance != null ? instance : new C2DMBroadcastReceiver();
	}
	
	/**
	 * When a cd2m intent is received by the device.
	 * Filter the type of intent.
	 * <ul>
	 * <li>REGISTRATION : get the token and send it to the AS</li>
	 * <li>RECEIVE : create a notification with the intent parameters</li>
	 * </ul>
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				"com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals(
				"com.google.android.c2dm.intent.RECEIVE") && !C2DMExtension.isInForeground) { 
			handleMessage(context, intent);//display the notification only when in background
		}
	}

	/**
	 * Check if there is a registration_id, and pass it to the AS.
	 * Send a TOKEN_FAIL event if there is an error.
	 * @param context 
	 * @param intent
	 */
	private void handleRegistration(Context context, Intent intent) {
		FREContext freContext = C2DMExtension.context;
		String registration = intent.getStringExtra("registration_id");

		if (intent.getStringExtra("error") != null) {
			String error = intent.getStringExtra("error");
//			Log.d(TAG, "Registration failed with error: " + error);
			if (freContext != null) {
				freContext.dispatchStatusEventAsync("TOKEN_FAIL", error);
			}
		} else if (intent.getStringExtra("unregistered") != null) {
//			Log.d(TAG, "Unregistered successfully");
			if (freContext != null) {
				freContext.dispatchStatusEventAsync("UNREGISTERED",
						"unregistered");
			}
		} else if (registration != null) {
//			Log.d(TAG, "Registered successfully");
			if (freContext != null) {
				freContext.dispatchStatusEventAsync("TOKEN_SUCCESS", registration);
			}
		}
	}
	
	
	private static int NotifId = 1;
		
	public static void registerResources(Context context)
	{
		notificationIcon = Resources.getResourseIdByName(context.getPackageName(), "drawable", "icon_status");
		applicationIcon = Resources.getResourseIdByName(context.getPackageName(), "drawable", "app_icon");
	}
	
	
	/**
	 * Get the parameters from the message and create a notification from it.
	 * @param context
	 * @param intent
	 */
	public void handleMessage(Context context, Intent intent) {
		try {
			registerResources(context);
//			Log.d(TAG, "GOT HERE 1");
			FREContext ctxt = C2DMExtension.context;
			
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			
			// icon is required for notification.
			// @see http://developer.android.com/guide/practices/ui_guidelines/icon_design_status_bar.html
			
			int icon = notificationIcon;
			int appIcon = applicationIcon;
			long when = System.currentTimeMillis();
//Log.d(TAG, "GOT HERE 2");
			
			// json string

			String parameters = intent.getStringExtra("parameters");
			String facebookId = null;
			JSONObject object = null;
			if (parameters != null)
			{
				try
				{
					object = (JSONObject) new JSONTokener(parameters).nextValue();
				} catch (Exception e)	
				{
					Log.d(TAG, "cannot parse the object");
				}
			}
			if (object != null && object.has("facebookId"))
			{
				facebookId = object.getString("facebookId");
			}
//			Log.d(TAG, "GOT HERE 3");
			CharSequence tickerText = intent.getStringExtra("tickerText");
			CharSequence contentTitle = intent.getStringExtra("contentTitle");
			CharSequence contentText = intent.getStringExtra("contentText");
						
			Intent notificationIntent = new Intent(context, 
					Class.forName(context.getPackageName() + ".AppEntry"));
			

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);

			

			if (facebookId != null) {
				Log.d(TAG, "facebookId not null");
		        new RetrieveFacebookProfilePictureTask().execute(context, new Integer(notificationIcon), facebookId, contentTitle, contentText, tickerText, contentIntent, this);
			} else {
				Log.d(TAG, "facebookId null");
				Bitmap myBitmap = BitmapFactory.decodeResource(context.getResources(), appIcon);
				sendNotification(context, notificationIcon, myBitmap, contentTitle, contentText, tickerText, contentIntent);
			}


			//Log.d(TAG, contentTitle);
			//Log.d(TAG, contentText);



//			Log.d(TAG, "GOT HERE 5");
			if (ctxt != null)
			{
				parameters = parameters == null ? "" : parameters;
				ctxt.dispatchStatusEventAsync("COMING_FROM_NOTIFICATION", parameters);
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Error activating application:", e);
		}
	}

	public void sendNotification(Context context, int smallIcon, Bitmap largeIcon, CharSequence contentTitle, CharSequence contentText, CharSequence tickerText, PendingIntent contentIntent) {
		if (largeIcon == null) {
			Log.d(TAG, "largeIcon is null");
		}

		Notification notification = new Notification.Builder(context)
			.setSmallIcon(smallIcon)
			.setLargeIcon(largeIcon)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setTicker(tickerText)
			.setContentIntent(contentIntent)
			.build();

		NotifId++;

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NotifId, notification);
	}

	class RetrieveFacebookProfilePictureTask extends AsyncTask<Object, Void, Bitmap>
	{

		private Exception exception;
		private Context context;
		private Integer smallIcon;
		private String facebookId;
		private CharSequence contentTitle;
		private CharSequence contentText;
		private CharSequence tickerText;
		private PendingIntent contentIntent;
		private C2DMBroadcastReceiver receiver;

		@Override
		protected Bitmap doInBackground(Object... params) {
			this.context = (Context) params[0];
			this.smallIcon = (Integer) params[1];
			this.facebookId = (String) params[2];
			this.contentTitle = (CharSequence) params[3];
			this.contentText = (CharSequence) params[4];
			this.tickerText = (CharSequence) params[5];
			this.contentIntent = (PendingIntent) params[6];
			this.receiver = (C2DMBroadcastReceiver) params[7];

			try {
				// String src = "http://graph.facebook.com/"+this.facebookId+"/picture";
				
				// URL url = new URL(src);
		  //       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		  //       connection.setDoInput(true);
		  //       connection.connect();
		  //       InputStream input = connection.getInputStream();
				String src = "https://graph.facebook.com/"+this.facebookId+"/picture?type=large";
				Log.d(TAG, "src: " + src);
				Bitmap b = BitmapFactory.decodeStream((new URL(src)).openConnection().getInputStream());
		        //Bitmap b = BitmapFactory.decodeStream(input);
		        if (b == null) {
		        	Log.d(TAG, "b is null");
		        }
		        return b;
		    } catch (Exception e) {
		    	Log.d(TAG, "nope! chuck testa!");
		    	this.exception = e;
		    	return null;
		    }
		}

		@Override
		protected void onPostExecute(Bitmap b) {
			if (this.exception != null) {
				Log.e(TAG, "Error retrieving profile picture:", this.exception);
			} else {
				Log.d(TAG, "sending notification after retrieving profile picture");
				this.receiver.sendNotification(this.context, this.smallIcon.intValue(), b, this.contentTitle, this.contentText, this.tickerText, this.contentIntent);
			}
		}

	}
	
}