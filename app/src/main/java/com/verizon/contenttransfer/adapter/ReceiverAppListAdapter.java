package com.verizon.contenttransfer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.model.CTReceiverAppsListVO;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.view.CTReceiverAppsListView;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by yempasu on 4/8/2017.
 */
public class ReceiverAppListAdapter extends ArrayAdapter<CTReceiverAppsListVO> {
    customButtonListener customListner;

    public interface customButtonListener {
        public void onButtonClickListener(int position,File value);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }

    private LayoutInflater mInflater;
    private static String TAG = "ReceiverAppListAdapter";

    static class ViewHolder {

        public ImageView appIcon;
        public TextView appName;
        public ImageView checkstatus;
        public TextView installedIV;
    }

    public ArrayList<CTReceiverAppsListVO> appsModelList = new ArrayList<CTReceiverAppsListVO>();
    private int mViewResourceId;


    public ReceiverAppListAdapter(Context ctx, int viewResourceId, ArrayList<CTReceiverAppsListVO> appsListModel) {
        super(ctx, viewResourceId, appsListModel);
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(null != appsListModel) {
            appsModelList = appsListModel;
        }
        mViewResourceId = viewResourceId;
    }

    @Override
    public int getCount() {
        return appsModelList.size();
    }

    @Override
    public CTReceiverAppsListVO getItem(int position) {
        return appsModelList.get(position);
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder =null;
        final CTReceiverAppsListVO appsListModelData = getItem(position);

        // reuse views
        if (rowView == null) {
            rowView = mInflater.inflate(mViewResourceId, null);
            // configure view holder
            holder = new ViewHolder();
            holder.appIcon = (ImageView) rowView.findViewById(R.id.app_icon);
            holder.appName = (TextView) rowView.findViewById(R.id.app_name);
            holder.checkstatus = (ImageView) rowView.findViewById(R.id.check);
            holder.installedIV = (TextView) rowView.findViewById(R.id.installed_with_a_right_mark);
            rowView.setTag(holder);

        } else{
            holder =(ViewHolder)rowView.getTag();
        }
        holder.checkstatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!appsListModelData.isChecked()) {
                    CTAppUtil.getInstance().updateCheckedFlag(position,true);
                    appsListModelData.setChecked(true);
                } else {
                    CTAppUtil.getInstance().updateCheckedFlag(position,false);
                    appsListModelData.setChecked(false);
                }
                CTReceiverAppsListView.getInstance().callNotifyDataSetChanged();
                CTReceiverAppsListView.getInstance().enableInstallButton(CTAppUtil.getInstance().isAnyItemSelected());
                CTAppUtil.getInstance().selectAllChange();
            }
        });

        if(appsListModelData.isChecked()){
            holder.checkstatus.setImageResource(R.mipmap.ct_checked);
        }else{
            holder.checkstatus.setImageResource(R.mipmap.ct_unchecked
            );
        }

        // fill data
        holder.appIcon.setImageDrawable(appsListModelData.getAppIcon());
        holder.appName.setText(appsListModelData.getAppName());


        if(null != CTAppUtil.getInstance().getInstalledAppNames()
                && CTAppUtil.getInstance().getInstalledAppNames().contains(appsListModelData.getAppName())){
            holder.checkstatus.setVisibility(View.INVISIBLE);
            holder.installedIV.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.checkstatus.setVisibility(View.VISIBLE);
            holder.installedIV.setVisibility(View.INVISIBLE);
        }
        return rowView;
    }

}

