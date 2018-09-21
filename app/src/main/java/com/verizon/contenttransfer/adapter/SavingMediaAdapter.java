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
import com.verizon.contenttransfer.model.CTSavingMediaVO;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by yempasu on 8/14/2017.
 */
public class SavingMediaAdapter extends ArrayAdapter<CTSavingMediaVO> {

    public SavingMediaAdapter(Context context, int resource) {
        super(context, resource);
    }

    private Activity activity;
    private LayoutInflater mInflater;
    private static String TAG = SavingMediaAdapter.class.getName();

    static class ViewHolder {

        public TextView mediaType, progressBarinsideText;
        public ProgressBar spinner;
        public ImageView statusIcon;
        public ProgressBar progressBar;
    }

    public ArrayList<CTSavingMediaVO> mediaList = new ArrayList<CTSavingMediaVO>();
    private int mViewResourceId;


    public SavingMediaAdapter(Activity ctx, int viewResourceId, ArrayList<CTSavingMediaVO> savingMediaModelArrayList) {
        super(ctx, viewResourceId, savingMediaModelArrayList);
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null != savingMediaModelArrayList) {
            mediaList = savingMediaModelArrayList;
        }
        mViewResourceId = viewResourceId;
        this.activity = ctx;
    }

    @Override
    public int getCount() {
        return mediaList.size();
    }

    @Override
    public CTSavingMediaVO getItem(int position) {
        return mediaList.get(position);
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
        final CTSavingMediaVO ctSavingMediaVO = getItem(position);
        // reuse views
        if (rowView == null) {
            rowView = mInflater.inflate(mViewResourceId, null);
            // configure view holder
            ViewHolder holder = new ViewHolder();

            holder.mediaType = (TextView) rowView.findViewById(R.id.ct_media_type);
            holder.statusIcon = (ImageView) rowView.findViewById(R.id.saving_status);
            holder.progressBarinsideText = (TextView) rowView.findViewById(R.id.ct_progressBarinsideText);
            holder.progressBar = (ProgressBar) rowView.findViewById(R.id.ct_saving_media_progress);
            holder.spinner = (ProgressBar) rowView.findViewById(R.id.br_round_progress);
            rowView.setTag(holder);
        }
        // fill data
        final ViewHolder holder = (ViewHolder) rowView.getTag();


        holder.mediaType.setText(ctSavingMediaVO.getUImediaType());
        holder.progressBar.setProgress(ctSavingMediaVO.getPercentageSaved());


        if (ctSavingMediaVO.getPercentageSaved() == 100) {
            holder.spinner.setVisibility(View.INVISIBLE);
            holder.statusIcon.setImageResource(R.mipmap.icon_ct_black_tick_small);
            holder.statusIcon.setVisibility(View.VISIBLE);
        }
        LogUtil.d(TAG,"progress bar show status :"+ctSavingMediaVO.getProgressShowStatus());
        if(ctSavingMediaVO.getProgressShowStatus().equals("false")){
            holder.progressBar.setVisibility(View.GONE);
            holder.progressBarinsideText.setText(activity.getString(R.string.user_denied_permission));
        }else {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBarinsideText.setText(String.valueOf(ctSavingMediaVO.getPercentageSaved()) + "% "+activity.getString(R.string.complete));
        }

        return rowView;
    }


}
