package com.ranita.BabyHunter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.BaseExpandableListAdapter;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.BeaconManager.MonitoringListener;
import com.aprilbrother.aprilbrothersdk.Region;

import java.util.List;

public class NotifyService extends Service {
	private static final String TAG = "NotifyService";
	private BeaconManager beaconManager;
	private static Region mBEACONS_REGION = new Region(
			"", null, null, null);
	private static NotificationManager mNotificationManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		beaconManager = new BeaconManager(this);
		startMonitoring();
		super.onCreate();
	}

	private void startMonitoring() {
		// If cannot scanned past scanned device in this period, device is leave.
		beaconManager.setMonitoringExpirationMill(5L);
		beaconManager.setRangingExpirationMill(5L);
	    // beaconManager.setBackgroundScanPeriod(10L, 1);
	    // beaconManager.setForegroundScanPeriod(10L, 1);
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					if(BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, getApplicationContext()).equals("")) {
						beaconManager.startMonitoring(mBEACONS_REGION);
						beaconManager.startRanging(mBEACONS_REGION);
					} else {
						String name = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_BEACON_NAME, getApplicationContext());
						String uuid = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_USER_UUID, getApplicationContext());
						int major = BeaconUtils.getIntSharedPref(BeaconUtils.SELECTED_USER_MAJOT, getApplicationContext());
						int minor = BeaconUtils.getIntSharedPref(BeaconUtils.SELECTED_USER_MINOR, getApplicationContext());
						beaconManager.stopRanging(mBEACONS_REGION);
						beaconManager.stopMonitoring(mBEACONS_REGION);
						mBEACONS_REGION = new Region(name, uuid, major, minor);
						beaconManager.startMonitoring(mBEACONS_REGION);
						beaconManager.startRanging(mBEACONS_REGION);
					}
				} catch (RemoteException e) {
					Log.i(TAG, "onServiceReady exception: " + e.toString() );
				}
			}
		});
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
				@Override
				public void onBeaconsDiscovered(Region region,
				final List<Beacon> beacons) {
					Log.i(TAG, "onBeaconsDiscovered");
					for (Beacon beacon : beacons) {
						Log.i(TAG, "getDistance = " + beacon.getDistance());
						Log.i(TAG, "rssi = " + beacon.getRssi());
						Log.i(TAG, "mac = " + beacon.getMacAddress());
						double target_dist = (double) BeaconUtils.getIntDistSharedPref(BeaconUtils.TARGET_NOTIFY_DISTANCE, getApplicationContext());
						Log.i(TAG, "target distance = " + target_dist);
						if ((beacon.getDistance() > target_dist) &&
							(BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, getApplicationContext()).equals(beacon.getMacAddress())) &&
							(BeaconUtils.getBooleanSharedPref(BeaconUtils.SELECTED_DISTANCE_DETECT_ENABLED, getApplicationContext()) == false)
						) {
							BeaconUtils.setBoolenSharedPref(BeaconUtils.SELECTED_DISTANCE_DETECT_ENABLED, true, getApplicationContext());
							showNotification(getApplicationContext(),
									getResources().getString(R.string.device_away));
						}
					}

					Log.i(TAG, "------------------------------beacons.size = " + beacons.size());
				}
		});

		beaconManager.setMonitoringListener(new MonitoringListener() {

			@Override
			public void onExitedRegion(Region region) {
				Log.i(TAG, "onExitedRegion");
				String mac = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, getApplicationContext());
				if(!mac.equals("")) {
					Log.i(TAG, "onExitedRegion mac: " + mac);
					showNotification(getApplicationContext(),
							getResources().getString(R.string.device_not_found));
				}
//				}
			}

			@Override
			public void onEnteredRegion(Region region, List<Beacon> beacons) {
				Log.i(TAG, "onEnteredRegion");
//				generateNotification(getApplicationContext(),
//						getResources().getString(R.string.device_coming));
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

//	private static void generateNotification(Context context, String message) {
//		Intent launchIntent = new Intent(context, BeaconList.class)
//				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		((NotificationManager) context
//				.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
//				0,
//				new NotificationCompat.Builder(context)
//						.setWhen(System.currentTimeMillis())
//						.setSmallIcon(R.drawable.search)
//						.setTicker(message)
//						.setContentTitle(context.getString(R.string.app_name))
//						.setContentText(message)
//						.setContentIntent(
//								PendingIntent.getActivity(context, 0,
//										launchIntent, 0)).setAutoCancel(true)
//						.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//						.build());
//
//	}

	public static void showNotification(Context context, String message) {
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		Resources r = context.getResources();

		// Setup fullscreen intent
		Intent fullScreenIntent = new Intent(context, MainBeaconListActivity.class);
		fullScreenIntent.putExtra("NotifyMessage", NotifyConstants.NOTIFICATION_BEACONLIST);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, fullScreenIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager nManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(message)
				.setSmallIcon(R.drawable.search)
				.setOngoing(true)
				.setAutoCancel(true)
				.setSound(alarmSound)
				.setCategory(NotificationCompat.CATEGORY_ALARM)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setContentIntent(pIntent)
				.setLocalOnly(true);

		Intent dismissReceive = new Intent();
		dismissReceive.setAction(NotifyConstants.ACTION_DISMISS_NOTIFICATION);
		PendingIntent pendingIntentDismiss = PendingIntent.getBroadcast(context, NotifyConstants.BABY_MISSING_NOTIFICATION_ID, dismissReceive, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.addAction(0, r.getString(R.string.notification_dismiss), pendingIntentDismiss);

		fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		notification.setFullScreenIntent(PendingIntent.getActivity(context,
				NotifyConstants.BABY_MISSING_NOTIFICATION_ID, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true);
		notification.setPriority(NotificationCompat.PRIORITY_MAX);

		Notification mNotification = notification.build();

		mNotification.flags |= Notification.FLAG_INSISTENT;
		nManager.notify(NotifyConstants.BABY_MISSING_NOTIFICATION_ID, mNotification);
	}

	public static void deleteNotification(Context context) {
		NotificationManager nManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
		nManager.cancel(NotifyConstants.BABY_MISSING_NOTIFICATION_ID);
	}
}
