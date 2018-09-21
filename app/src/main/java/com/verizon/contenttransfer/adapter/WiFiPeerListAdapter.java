package com.verizon.contenttransfer.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.model.CTDeviceIteratorModel;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.ContentTransferAnalyticsMap;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.view.CTDeviceIteratorView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by c0bissh on 6/5/2017.
 */
public class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

    private List<WifiP2pDevice> items;
    private Context context;
    public WiFiPeerListAdapter(Context context, int textViewResourceId,
                               List<WifiP2pDevice> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;

        this.items = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.ct_wifi_direct_device_cell, null);
        }

        final WifiP2pDevice device = items.get(position);
        if (device != null) {

            TextView deviceNameText = (TextView) v.findViewById(R.id.ct_w_phn_name_tv);
            ImageView deviceSelected = (ImageView) v.findViewById(R.id.ct_w_phn_name_chk);
            if (!Utils.isThisServer()) {
                v.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        HashMap<String, Object> eventMap = new HashMap<String, Object>();
                        eventMap.put(ContentTransferAnalyticsMap.DEVICE_UUID, CTGlobal.getInstance().getDeviceUUID());
                        eventMap.put(ContentTransferAnalyticsMap.CONNECTIONTYPE, "WiFiDirect");
                        eventMap.put(ContentTransferAnalyticsMap.PAIRTYPE, "Manual");
                        Utils.logEvent(v, eventMap, "PairingScreen");

                        CTDeviceIteratorView.getInstance().handleListItemClickEvent(position, device, v);
                    }
                });
            }

            if (CTDeviceIteratorModel.getInstance().getDevice() != null && device.deviceName.equals(CTDeviceIteratorModel.getInstance().getDevice().deviceName)) {
                deviceSelected.setImageResource(R.mipmap.radio_checked_new);
            } else {
                deviceSelected.setImageResource(R.mipmap.icon_ct_check_grey);
            }

            if (deviceNameText != null) {
                deviceNameText.setText(device.deviceName);
            }
        }

        return v;
    }
}

