package com.ranita.BabyHunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.BeaconManager.MonitoringListener;
import com.aprilbrother.aprilbrothersdk.BeaconManager.RangingListener;
import com.aprilbrother.aprilbrothersdk.Region;
import com.aprilbrother.aprilbrothersdk.utils.AprilL;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainBeaconListActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final String TAG = "MainBeaconListActivity";
    private static Region mBEACONS_REGION = new Region(
            "", null, null, null);
    private BeaconAdapter adapter;
    private BeaconManager beaconManager;
    private ArrayList<Beacon> myBeacons;
    private static int mTargetDistance = 8;
    private static int mTargetDistanceID = 0;
    private static int mPower = -51;
    private static int mPowerID = 0;
    private static int mSelectedBeaconPosition;
    private TextView mScanTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
         mScanTextView = (TextView) findViewById(R.id.tv_scanning);
        if(BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, MainBeaconListActivity.this).equals("")) {
            mScanTextView.setText(getResources().getString(R.string.scanning));
        } else {
            mScanTextView.setText(getResources().getString(R.string.scanning_target));
        }
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_distance:
                displayDistanceDialog();
                return true;
            case R.id.action_name:
                displayModifyUserNameDialog();
                return true;
            case R.id.action_delete:
                resetTrackBeaconPref();
                return true;
            case R.id.action_settings:
                displayUserGuideDialog();
                return true;
            case R.id.action_qrcode:
                displayQrCodeDialog();
                return true;
//            case R.id.action_txPower_settings:
//                displayTxPowerDialog();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayDistanceDialog() {
        int target_dist = BeaconUtils.getIntDistSharedPref(BeaconUtils.TARGET_NOTIFY_DISTANCE, getApplicationContext());
        String title = getResources().getString(R.string.select_dist) +
                " (" + getResources().getString(R.string.current) +
                ": " + target_dist + getResources().getString(R.string.dist_unit) +
                ")";
        AlertDialog.Builder builder = new AlertDialog.Builder(MainBeaconListActivity.this);
        builder.setTitle(title);

        String[] dist_items = {
                getResources().getString(R.string.dist_item1),
                getResources().getString(R.string.dist_item2),
                getResources().getString(R.string.dist_item3),
        };

        int checkedItemID = BeaconUtils.getIntSharedPref(BeaconUtils.TARGET_NOTIFY_DISTANCE_ID, getApplicationContext());
        builder.setSingleChoiceItems(dist_items, checkedItemID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mTargetDistance = 4;
                        mTargetDistanceID = 0;
                        break;
                    case 1:
                        mTargetDistance = 6;
                        mTargetDistanceID = 1;
                        break;
                    case 2:
                        mTargetDistance = 9;
                        mTargetDistanceID = 2;
                        break;
                    default:
                        mTargetDistance = 3;
                        mTargetDistanceID = 0;
                }
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BeaconUtils.setIntSharedPref(BeaconUtils.TARGET_NOTIFY_DISTANCE, mTargetDistance, getApplicationContext());
                BeaconUtils.setIntSharedPref(BeaconUtils.TARGET_NOTIFY_DISTANCE_ID, mTargetDistanceID, getApplicationContext());
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayTxPowerDialog() {
        int target_power = BeaconUtils.getIntPowerSharedPref(BeaconUtils.TARGET_TX_POWER, getApplicationContext());
        String title = getResources().getString(R.string.select_power) +
                " (" + getResources().getString(R.string.current) +
                ": " + target_power + getResources().getString(R.string.tx_power_unit) +
                ")";
        int target_power_id = BeaconUtils.getIntSharedPref(BeaconUtils.TARGET_TX_POWER_ID, getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(MainBeaconListActivity.this);
        builder.setTitle(title);

        String[] dist_items = {
                getResources().getString(R.string.power_item1),
                getResources().getString(R.string.power_item2),
                getResources().getString(R.string.power_item3),
                getResources().getString(R.string.power_item4),
                getResources().getString(R.string.power_item5)
        };

        int checkedItemID = target_power_id;
        builder.setSingleChoiceItems(dist_items, checkedItemID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mPower = -51;
                        mPowerID = 0;
                        break;
                    case 1:
                        mPower = -52;
                        mPowerID = 1;
                        break;
                    case 2:
                        mPower = -53;
                        mPowerID = 2;
                        break;
                    case 3:
                        mPower = -54;
                        mPowerID = 3;
                        break;
                    case 4:
                        mPower = -55;
                        mPowerID = 4;
                        break;
                    default:
                        mPower = -51;
                        mPowerID = 0;
                }
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BeaconUtils.setIntSharedPref(BeaconUtils.TARGET_TX_POWER, mPower, getApplicationContext());
                BeaconUtils.setIntSharedPref(BeaconUtils.TARGET_TX_POWER_ID, mPowerID, getApplicationContext());
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayModifyUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainBeaconListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_username, null);
        builder.setView(view);
        final EditText edittext = (EditText) view.findViewById(R.id.inputUsername);
        builder.setTitle(getResources().getString(R.string.input_user_name_title));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String val = edittext.getText().toString();
                        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_USER_NAME, val, getApplicationContext());
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayUserGuideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainBeaconListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_userguide, null);
        builder.setView(view);
        final TextView txtView = (TextView) view.findViewById(R.id.user_guide);
        txtView.setBackgroundResource(R.drawable.family_bg);
        String guide_str = getResources().getString(R.string.guide) + ":\n" +
                getResources().getString(R.string.user_guide1) + "\n" +
                getResources().getString(R.string.user_guide2) + "\n\n" +
                getResources().getString(R.string.user_guide3) + "\n" +
                getResources().getString(R.string.user_guide4) + "\n\n" +
                getResources().getString(R.string.user_guide5) + "\n" +
                getResources().getString(R.string.user_guide6);
        txtView.setText(guide_str);
        builder.setNegativeButton(R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayQrCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainBeaconListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_qrcode, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetTrackBeaconPref() {
        Log.i(TAG, "resetTrackBeaconPref");
        if(!BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, MainBeaconListActivity.this).equals("")) {
            Log.i(TAG, "resetTrackBeaconPref start reset region");
            String name = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_BEACON_NAME, MainBeaconListActivity.this);
            String uuid = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_USER_UUID, MainBeaconListActivity.this);
            int major = BeaconUtils.getIntSharedPref(BeaconUtils.SELECTED_USER_MAJOT, MainBeaconListActivity.this);
            int minor = BeaconUtils.getIntSharedPref(BeaconUtils.SELECTED_USER_MINOR, MainBeaconListActivity.this);
            Log.i(TAG, "resetTrackBeaconPref name: " + name);
            mBEACONS_REGION = new Region(name, uuid, major, minor);
            try {
                beaconManager.stopRanging(mBEACONS_REGION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                beaconManager.stopMonitoring(mBEACONS_REGION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBEACONS_REGION = new Region("", null, null, null);
            try {
                beaconManager.startRanging(mBEACONS_REGION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                beaconManager.startMonitoring(mBEACONS_REGION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "resetTrackBeaconPref start reset pref");
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_MAC, "", getApplicationContext());
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_BEACON_NAME, "", getApplicationContext());
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_USER_NAME, "", getApplicationContext());
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_USER_UUID, "", getApplicationContext());
        BeaconUtils.setIntSharedPref(BeaconUtils.SELECTED_USER_MAJOT, 0, getApplicationContext());
        BeaconUtils.setIntSharedPref(BeaconUtils.SELECTED_USER_MINOR, 0, getApplicationContext());
        BeaconUtils.setBoolenSharedPref(BeaconUtils.SELECTED_BEACON_NOTIFICATION_ENABLED, false, getApplicationContext());
        BeaconUtils.setBoolenSharedPref(BeaconUtils.SELECTED_DISTANCE_DETECT_ENABLED, false, getApplicationContext());
        Toast.makeText(MainBeaconListActivity.this, getResources().getString(R.string.data_clean), Toast.LENGTH_SHORT).show();
        mScanTextView.setText(getResources().getString(R.string.scanning));
    }


    private void init() {
        Log.i(TAG, "init");
        myBeacons = new ArrayList<Beacon>();
        ListView lv = (ListView) findViewById(R.id.lv);
        adapter = new BeaconAdapter(this);
        lv.setAdapter(adapter);
        AprilL.enableDebugLogging(true);
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setMonitoringExpirationMill(5L);
        beaconManager.setRangingExpirationMill(5L);
        // beaconManager.setBackgroundScanPeriod(10L, 1);
        // beaconManager.setForegroundScanPeriod(10L, 1);
        beaconManager.setRangingListener(new RangingListener() {

            @Override
            public void onBeaconsDiscovered(Region region,
                                            final List<Beacon> beacons) {
                Log.i(TAG, "onBeaconsDiscovered beacons.size = " + beacons.size());
                myBeacons.clear();
                myBeacons.addAll(beacons);
                getActionBar().setSubtitle(getResources().getString(R.string.device_found_num) + ": " + beacons.size());
                ComparatorBeaconByRssi com = new ComparatorBeaconByRssi();
                // Collections.sort(myBeacons, com);
                Collections.sort(myBeacons, new Comparator<Beacon>() {
                    @Override
                    public int compare(Beacon o1, Beacon o2) {
                        String mac1 = o1.getMacAddress();
                        String mac2 = o2.getMacAddress();
                        String current_selected_mac = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, getApplicationContext());
                        Boolean result1 = current_selected_mac.equals(mac1);
                        Boolean result2 = current_selected_mac.equals(mac2);
                        return Boolean.compare(result2, result1);
                    }
                });
                adapter.replaceWith(myBeacons);
            }
        });

//        beaconManager.setMonitoringListener(new MonitoringListener() {
//
//            @Override
//            public void onExitedRegion(Region arg0) {
//                Toast.makeText(MainBeaconListActivity.this, "Notify in", 0).show();
//
//            }
//
//            @Override
//            public void onEnteredRegion(Region arg0, List<Beacon> arg1) {
//                Toast.makeText(MainBeaconListActivity.this, "Notify out", 0).show();
//            }
//        });


        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mScanTextView.setText(getResources().getString(R.string.scanning_target));
                adapter.replaceWith(Collections.<Beacon>emptyList());
                mSelectedBeaconPosition = arg2;
                final Thread thread=new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.i(TAG, "setOnItemClickListener set device, position: " + mSelectedBeaconPosition);
                        Beacon beacon = myBeacons.get(mSelectedBeaconPosition);
                        stopService(new Intent(MainBeaconListActivity.this, NotifyService.class));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        writeSelectedBeaconData(beacon, getApplicationContext());
                        Intent intent = new Intent(MainBeaconListActivity.this, NotifyService.class);
                        startService(intent);
                        startTargetRegion();
                    }
                });
                thread.start();
            }
        });

//        final TextView tv = (TextView) findViewById(R.id.tv_swith);
//        tv.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (tv.getText().equals(R.string.start_scan)) {
//                    try {
//                        tv.setText(R.string.stop_scan);
//                        beaconManager.startRanging(mBEACONS_REGION);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        tv.setText(R.string.start_scan);
//                        beaconManager.stopRanging(mBEACONS_REGION);
//                    } catch (RemoteException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }

    public void writeSelectedBeaconData(Beacon beacon, Context ctx){
        Log.i(TAG, "writeSelectedBeaconData");
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_MAC, beacon.getMacAddress(), ctx);
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_BEACON_NAME, beacon.getName(), ctx);
        BeaconUtils.setSharedPref(BeaconUtils.SELECTED_USER_UUID, beacon.getProximityUUID(), ctx);
        BeaconUtils.setIntSharedPref(BeaconUtils.SELECTED_USER_MAJOT, beacon.getMajor(), ctx);
        BeaconUtils.setIntSharedPref(BeaconUtils.SELECTED_USER_MINOR, beacon.getMinor(), ctx);
        BeaconUtils.setBoolenSharedPref(BeaconUtils.SELECTED_BEACON_NOTIFICATION_ENABLED, true, ctx);
        BeaconUtils.setBoolenSharedPref(BeaconUtils.SELECTED_DISTANCE_DETECT_ENABLED, false, ctx);
    }

    private void startAllRegion() {
        Log.i(TAG, "onServiceReady start all");
        try {
            beaconManager.startMonitoring(new Region("", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            beaconManager.startRanging(new Region("", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startTargetRegion() {
        Log.i(TAG, "onServiceReady start selected");
        String name = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_BEACON_NAME, MainBeaconListActivity.this);
        String uuid = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_USER_UUID, MainBeaconListActivity.this);
        int major = BeaconUtils.getIntSharedPref(BeaconUtils.SELECTED_USER_MAJOT, MainBeaconListActivity.this);
        int minor = BeaconUtils.getIntSharedPref(BeaconUtils.SELECTED_USER_MINOR, MainBeaconListActivity.this);
        try {
            beaconManager.stopRanging(new Region("", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            beaconManager.stopMonitoring(new Region("", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBEACONS_REGION = new Region(name, uuid, major, minor);
        try {
            beaconManager.startMonitoring(mBEACONS_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            beaconManager.startRanging(mBEACONS_REGION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * beacon connect service start scan beacons
     */
    private void connectToService() {
        Log.i(TAG, "connectToService");
        adapter.replaceWith(Collections.<Beacon>emptyList());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
            if(BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, MainBeaconListActivity.this).equals("")) {
                startAllRegion();
            } else {
                startTargetRegion();
            }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, getResources().getString(R.string.bt_not_enabled), Toast.LENGTH_LONG)
                        .show();
                getActionBar().setSubtitle(getResources().getString(R.string.bt_not_enabled));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        startWork();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        stopWork();
        super.onStop();
    }

    private void stopWork() {
        try {
            myBeacons.clear();
            beaconManager.stopRanging(mBEACONS_REGION);
            beaconManager.stopMonitoring(mBEACONS_REGION);
            beaconManager.disconnect();
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }
    }

    private void startWork() {
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy",
                    Toast.LENGTH_LONG).show();
            Log.i(TAG, "!hasBluetooth");
            return;
        }
        if (!beaconManager.isBluetoothEnabled()) {
            Log.i(TAG, "!isBluetoothEnabled");
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if(BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, MainBeaconListActivity.this).equals("")) {
            mScanTextView.setText(getResources().getString(R.string.scanning));
        } else {
            mScanTextView.setText(getResources().getString(R.string.scanning_target));
        }
        Log.i(TAG, "Enable baby notify");
        Intent intent = new Intent(MainBeaconListActivity.this, NotifyService.class);
        startService(intent);
        connectToService();
    }
}
