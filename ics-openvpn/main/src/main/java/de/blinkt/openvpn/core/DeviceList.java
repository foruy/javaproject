package de.blinkt.openvpn.fragments;

import java.util.Collection;
import java.util.TreeSet;

import android.app.ListFragment;
import android.os.Bundle;
import android.content.Context;
import android.view.*;
import android.widget.ArrayAdapter;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.core.Device;
import de.blinkt.openvpn.core.VPNClient;


public class DeviceList extends ListFragment {
    private String userId;
    private String netId;

    class DeviceArrayAdapter extends ArrayAdapter<Device> {
        public DeviceArrayAdapter(Context context, int resource,
                int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            v.findViewById(R.id.vpn_list_item_left);
            return v;
        }
    }

    private ArrayAdapter<Device> mArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter();
    }

    private void setListAdapter() {
        //Bundle data = getIntent().getExtras();
        mArrayAdapter = new DeviceArrayAdapter(getActivity(), R.layout.vpn_list_item, R.id.vpn_item_title);
        //Collection<Device> allDevice = VPNClient.getInstance().device(getActivity(), data.getString("userId"), data.getString("netId"));
        TreeSet<Device> sortedset = new TreeSet<Device>();
        //sortedset.addAll(allDevice);
        mArrayAdapter.addAll(sortedset);
        setListAdapter(mArrayAdapter);
    }
}
