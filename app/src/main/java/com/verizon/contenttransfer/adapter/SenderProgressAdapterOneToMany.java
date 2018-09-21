package com.verizon.contenttransfer.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.AcceptedClientInfo;
import com.verizon.contenttransfer.utils.Utils;

import java.util.ArrayList;

/**
 * Created by yempasu on 5/23/2017.
 */
public class SenderProgressAdapterOneToMany extends ArrayAdapter<AcceptedClientInfo>{

    public SenderProgressAdapterOneToMany(Context context, int resource) {
        super(context, resource);
    }

    private LayoutInflater mInflater;
    private static String TAG = SenderProgressAdapterOneToMany.class.getName();
    private Activity activity;
    static class ViewHolder {

        public TextView connectedDeviceName, progressBarinsideText;
        public ProgressBar spinner;
        public ImageView statusIcon;
        public ProgressBar progressBar;
    }

    public ArrayList<AcceptedClientInfo> clientInfo = new ArrayList<AcceptedClientInfo>();
    private int mViewResourceId;


    public SenderProgressAdapterOneToMany(Activity ctx, int viewResourceId, ArrayList<AcceptedClientInfo> progressListModel) {
        super(ctx, viewResourceId, progressListModel);
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(null != progressListModel) {
            clientInfo = progressListModel;
        }
        mViewResourceId = viewResourceId;
        this.activity = ctx;
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
        // reuse views
        if (rowView == null) {
            rowView = mInflater.inflate(mViewResourceId, null);
            // configure view holder
            holder = new ViewHolder();
            holder.connectedDeviceName = (TextView) rowView.findViewById(R.id.ct_connected_device_name);
            holder.progressBar = (ProgressBar) rowView.findViewById(R.id.ct_sender_loadingPB_onetomany);
            holder.spinner = (ProgressBar) rowView.findViewById(R.id.br_round_progress);
            holder.statusIcon = (ImageView) rowView.findViewById(R.id.one_to_many_status_iv);
            holder.progressBarinsideText = (TextView) rowView.findViewById(R.id.ct_progressBarinsideText);
            rowView.setTag(holder);

        } else{
            holder =(ViewHolder)rowView.getTag();
        }

        AcceptedClientInfo clientInfo = getItem(position);
        int currentProgress = clientInfo.getCtAnalyticUtil().getProgressUpdate();
        long fileCount = clientInfo.getCtAnalyticUtil().getFileTransferredCount();
        String progressInsideText = fileCount +activity.getString(R.string.files)+"/"+" "+ Utils.bytesToMeg(clientInfo.getCtAnalyticUtil().getTransferredBytes()) + " MB";
        if (fileCount <= 0) {
            progressInsideText = activity.getString(R.string.progress_start_text);
        }
        // fill data
        holder.connectedDeviceName.setText(clientInfo.getDeviceName());
        holder.progressBar.setProgress(currentProgress);
        holder.progressBarinsideText.setText(progressInsideText);
        if (currentProgress == 100) {
            //LogUtil.d(TAG,"clientInfo.getStatus(): "+clientInfo.getStatus());
            holder.spinner.setVisibility(View.INVISIBLE);
            holder.statusIcon.setImageResource(R.mipmap.icon_ct_black_tick_small);
            holder.statusIcon.setVisibility(View.VISIBLE);
        } else if (clientInfo.getCtAnalyticUtil().getDataTransferStatusMsg().length() > 0 &&
                !clientInfo.getCtAnalyticUtil().getDataTransferStatusMsg().equals(VZTransferConstants.TRANSFER_SUCCESSFULLY_COMPLETED)) {
            //LogUtil.d(TAG,"clientInfo.getStatus(): "+clientInfo.getStatus());
            holder.spinner.setVisibility(View.INVISIBLE);
            holder.statusIcon.setImageResource(R.mipmap.icon_ct_mobile_yellow_small_png);
            holder.statusIcon.setVisibility(View.VISIBLE);
        }
        return rowView;
    }
}
