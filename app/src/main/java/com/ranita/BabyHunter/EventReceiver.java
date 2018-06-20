package com.ranita.BabyHunter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class EventReceiver extends BroadcastReceiver {
    private static final String TAG = "EventReceiver";

    private static Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(NotifyConstants.ACTION_DISMISS_NOTIFICATION)) {
            NotifyService.deleteNotification(mContext);
            Intent i = new Intent(mContext, MainBeaconListActivity.class);
            i.putExtra("NotifyMessage", NotifyConstants.NOTIFICATION_BEACONLIST);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
            BeaconUtils.clearNotificationFlags(mContext);
        }
    }
}
