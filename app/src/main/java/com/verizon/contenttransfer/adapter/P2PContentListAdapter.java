package com.verizon.contenttransfer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.CTSelectContentListener;
import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.p2p.model.ContentSelectionVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.view.CTSelectContentView;

import java.util.ArrayList;

public class P2PContentListAdapter extends ArrayAdapter<ContentSelectionVO> {

    private LayoutInflater mInflater;
    private static String TAG = P2PContentListAdapter.class.getName();
    private Context context;

    static class ViewHolder {

        public ImageView checkStatus;
        public TextView contentSize;
       // public TextView mediaType;
        public TextView UImedia;
        public TextView cloudContent;
    }

    public static ArrayList<ContentSelectionVO> contentselctionlist = new ArrayList<ContentSelectionVO>();
    private int mViewResourceId;


    public P2PContentListAdapter(Context ctx, int viewResourceId, ArrayList<ContentSelectionVO> strings) {
        super(ctx, viewResourceId, strings);

        this.contentselctionlist.clear();
        this.context=ctx;
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentselctionlist = strings;
        mViewResourceId = viewResourceId;
    }

    @Override
    public int getCount() {
        return contentselctionlist.size();
    }

    @Override
    public ContentSelectionVO getItem(int position) {
        return contentselctionlist.get(position);
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

        // reuse views
        if (rowView == null) {
            rowView = mInflater.inflate(mViewResourceId, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.checkStatus = (ImageView) rowView.findViewById(R.id.ct_content_chk);
            viewHolder.contentSize = (TextView) rowView.findViewById(R.id.ct_total_gb_tv);
            viewHolder.UImedia = (TextView) rowView.findViewById(R.id.ct_content_tv);
            viewHolder.cloudContent = (TextView) rowView.findViewById(R.id.ct_cloud_content_tv);
            rowView.setTag(viewHolder);
        }

        // fill data
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        final ContentSelectionVO mediaContentSelection = getItem(position);
        LogUtil.d(TAG, "mediaContentSelection.getContentType =" + mediaContentSelection.getContentType() + " getContentsize =" + mediaContentSelection.getContentsize());
        if (!mediaContentSelection.isPermissionFlag()) {
            holder.contentSize.setTextColor(CTGlobal.getInstance().getContentTransferContext().getResources().getColor(R.color.vz_red_color));
            holder.contentSize.setText(R.string.user_denied_permission);
        }else{
            holder.contentSize.setText(mediaContentSelection.getContentStorage());
        }
        holder.cloudContent.setText(mediaContentSelection.getCloudContent());
        if(mediaContentSelection.getContentflag()){
            holder.checkStatus.setImageResource(R.mipmap.ct_checked);
        } else {
            holder.checkStatus.setImageResource(R.mipmap.ct_unchecked);
        }


        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "row clicked..." + position);
                listItemCliked(mediaContentSelection, holder);
            }

        });

        updateTransferSelectionList(holder, mediaContentSelection);
        return rowView;
    }

    private void updateTransferSelectionList(ViewHolder holder, ContentSelectionVO mediaContentSelection) {
        if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.CONTACTS_STR)) {
            if(CTGlobal.getInstance().isContactsPermitted()) {
                holder.UImedia.setText(context.getString(R.string.contacts_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
            }else{
                holder.UImedia.setText(context.getString(R.string.contacts_str));
            }
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.PHOTOS_STR)) {
            holder.UImedia.setText(context.getString(R.string.photos_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.VIDEOS_STR)) {
            holder.UImedia.setText(context.getString(R.string.videos_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.AUDIO_STR)) {
            holder.UImedia.setText(context.getString(R.string.musics_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.CALLLOG_STR)) {
            if(CTGlobal.getInstance().isCalllogsPermitted()) {
                holder.UImedia.setText(context.getString(R.string.callLogs_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
            }else{
                holder.UImedia.setText(context.getString(R.string.callLogs_str) );
            }
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.SMS_STR)) {
            if(CTGlobal.getInstance().isSmsPermitted()) {
                holder.UImedia.setText(context.getString(R.string.messages_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
            }else{
                holder.UImedia.setText(context.getString(R.string.messages_str) );
            }
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.CALENDAR_STR)) {
            if(CTGlobal.getInstance().isCalendarPermitted()) {
                holder.UImedia.setText(context.getString(R.string.calendars_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
            }else{
                holder.UImedia.setText(context.getString(R.string.calendars_str) );
            }
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.APPS_STR)) {
            if(VZTransferConstants.SUPPORT_APPS) {
                if (!CTGlobal.getInstance().isCross()) {
                    holder.UImedia.setText(context.getString(R.string.apps_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
                } else {
                    holder.UImedia.setText(context.getString(R.string.apps_list) + "(" + Integer.toString(mediaContentSelection.getContentsize()) + ")" );
                }
            }
        } else if (mediaContentSelection.getContentType().equalsIgnoreCase(VZTransferConstants.DOCUMENTS_STR)) {
            if (VZTransferConstants.SUPPORT_DOCS) {
                holder.UImedia.setText(context.getString(R.string.documents_str) + " (" + Integer.toString(mediaContentSelection.getContentsize()) + ")");
            }
        }
    }

    private void listItemCliked(ContentSelectionVO contentSelectionVO, ViewHolder holder) {
        // Your code that you want to execute on this button click
        if (!contentSelectionVO.getContentflag()) {

            if (contentSelectionVO.getContentsize() > 0
                    || contentSelectionVO.getContentStorage().equals(CTGlobal.getInstance().getContentTransferContext().getString(R.string.ct_content_tv))) {
                contentSelectionVO.setContentflag(true);
                holder.checkStatus.setImageResource(R.mipmap.ct_checked);
                CTSelectContentModel.getInstance().samSelectAllChangeText(true, false); //sang may2016
                CTSelectContentView.getInstance().callNotifyDataSetChanged();
            }
        } else {
            contentSelectionVO.setContentflag(false);
            holder.checkStatus.setImageResource(R.mipmap.ct_unchecked);
            CTSelectContentModel.getInstance().samSelectAllChangeText(false, false); //sang may2016
            CTSelectContentListener.getCtSelectContentListener().setCheckFlag(false);
        }

        updateTransferSelectionList(holder, contentSelectionVO);
    }

}
