package com.ranita.BabyHunter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aprilbrother.aprilbrothersdk.Beacon;

import java.util.ArrayList;
import java.util.Collection;

public class BeaconAdapter extends BaseAdapter {

	private ArrayList<Beacon> beacons;
	private LayoutInflater inflater;
	private Context mContex = null;

	public BeaconAdapter(Context context) {
		this.inflater = LayoutInflater.from(context);
		this.beacons = new ArrayList<Beacon>();
		mContex = context;
	}

	public void replaceWith(Collection<Beacon> newBeacons) {
		this.beacons.clear();
		this.beacons.addAll(newBeacons);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return beacons.size();
	}

	@Override
	public Beacon getItem(int position) {
		return beacons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		view = inflateIfRequired(view, position, parent);
		bind(getItem(position), view);
		return view;
	}

	private void bind(Beacon beacon, View view) {
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.distanceTextView.setText(mContex.getResources().getString(R.string.dist) + ": "
				+ beacon.getDistance() + mContex.getResources().getString(R.string.dist_unit));
		String name = BeaconUtils.getSharedPref(BeaconUtils.SELECTED_USER_NAME, mContex);
		holder.usernameTextView.setText(name.equals("")? mContex.getResources().getString(R.string.give_me_a_name): name);
		boolean isSelected = BeaconUtils.getBooleanSharedPref(BeaconUtils.SELECTED_BEACON_NOTIFICATION_ENABLED, mContex) &&
				(BeaconUtils.getSharedPref(BeaconUtils.SELECTED_MAC, mContex).equals(beacon.getMacAddress())
				);
		if (isSelected) {
			holder.scanUserImageView.setImageResource(R.drawable.user_baby);
		} else {
			holder.scanUserImageView.setImageResource(R.drawable.user_baby_grey);
		}
	}

	private View inflateIfRequired(View view, int position, ViewGroup parent) {
		if (view == null) {
			view = inflater.inflate(R.layout.device_item, null);
			view.setTag(new ViewHolder(view));
		}
		return view;
	}

	static class ViewHolder {
		final TextView usernameTextView;
		final TextView distanceTextView;
		final ImageView scanUserImageView;

		ViewHolder(View view) {
			usernameTextView = (TextView) view.findViewById(R.id.scan_username);
			distanceTextView = (TextView) view.findViewById(R.id.scan_distance);
			scanUserImageView = (ImageView) view.findViewById(R.id.scan_user_icon);
		}
	}
}
