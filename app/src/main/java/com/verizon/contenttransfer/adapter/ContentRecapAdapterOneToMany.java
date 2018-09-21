package com.verizon.contenttransfer.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.p2p.model.AcceptedClientInfo;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;

import java.util.ArrayList;

/**
 * Created by yempasu on 5/23/2017.
 */
public class ContentRecapAdapterOneToMany extends ArrayAdapter<AcceptedClientInfo> {
    public ContentRecapAdapterOneToMany(Context context, int resource) {
        super(context, resource);
    }

    private LayoutInflater mInflater;
    private static String TAG = "SenderProgressAdapterOneToMany";
    private Activity activity;

    static class ViewHolder {

        public TextView connectedDeviceName;
        public TextView recapInfo;
        public ImageView status;
    }

    public ArrayList<AcceptedClientInfo> clientInfo = new ArrayList<AcceptedClientInfo>();
    private int mViewResourceId;


    public ContentRecapAdapterOneToMany(Activity ctx, int viewResourceId, ArrayList<AcceptedClientInfo> recapListModel) {
        super(ctx, viewResourceId, recapListModel);
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(null != recapListModel) {
            clientInfo = recapListModel;
        }
        mViewResourceId = viewResourceId;
        this.activity=ctx;
    }

    @Override
    public int getCount() {
        return clientInfo.size();
    }

    @Override
    public AcceptedClientInfo getItem(int position) {
        return clientInfo.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder =null;
        final AcceptedClientInfo clientInfo = getItem(position);

        // reuse views
        if (rowView == null) {
            rowView = mInflater.inflate(mViewResourceId, null);
            // configure view holder
            holder = new ViewHolder();
            holder.connectedDeviceName = (TextView) rowView.findViewById(R.id.ct_connected_device_name);
            holder.recapInfo = (TextView) rowView.findViewById(R.id.recap_info);
            holder.status=(ImageView) rowView.findViewById(R.id.status);

            rowView.setTag(holder);

        } else{
            holder =(ViewHolder)rowView.getTag();
        }
        // fill data
        String totDataTransferred = DataSpeedAnalyzer.convertBytesToMegString(clientInfo.getCtAnalyticUtil().getTransferredBytes());
        holder.connectedDeviceName.setText(clientInfo.getDeviceName());
        holder.recapInfo.setText( clientInfo.getCtAnalyticUtil().getFileTransferredCount() +activity.getString(R.string.files)+"/" + totDataTransferred+" MB");
        if(clientInfo.getCtAnalyticUtil().getFileTransferredCount()!=DataSpeedAnalyzer.getFileCounter()){
            holder.status.setImageResource(R.mipmap.icon_ct_mobile_yellow_small_png);
        }else{
            holder.status.setImageResource(R.mipmap.icon_ct_black_tick_small);
        }
        return rowView;
    }
}
