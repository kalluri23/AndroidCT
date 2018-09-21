package com.verizon.contenttransfer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.activity.CTTransferStatusActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.listener.TransferSummaryListener;
import com.verizon.contenttransfer.p2p.model.ContentRecapVO;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;

import java.util.List;

/**
 * Created by c0bissh on 9/20/2016.
 */
public class ContentRecapAdapter extends ArrayAdapter<ContentRecapVO> {
    private List<ContentRecapVO> items;
    private Activity context;
    private static String TAG = ContentRecapAdapter.class.getName();
    public ContentRecapAdapter(Activity context, int textViewResourceId,
                               List<ContentRecapVO> objects) {
        super(context, textViewResourceId,objects);
        this.items = objects;
        this.context=context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.ct_transfer_summary_cell, null);
        }
        final ContentRecapVO contentRecapVO = items.get(position);
        if (contentRecapVO != null) {
            v.setTag(contentRecapVO);
            if(contentRecapVO.getContentType().equals(VZTransferConstants.APPS_STR) && CTGlobal.getInstance().isCross()){
                ((TextView)v.findViewById(R.id.ct_tr_summ_cell_content_tv)).setText(context.getString(R.string.apps_list));
                ((TextView)v.findViewById(R.id.ct_tr_summ_cell_total_count_tv)).setText("");
            }else {
                ((TextView) v.findViewById(R.id.ct_tr_summ_cell_content_tv)).setText(contentRecapVO.getUImedia());
                ((TextView) v.findViewById(R.id.ct_tr_summ_cell_total_count_tv)).setText(contentRecapVO.getTransferSize() + " " + context.getString(R.string.of) + " " + contentRecapVO.getContentSize());
            }
            if(!contentRecapVO.isCheckStatus()){
                ((ImageView)v.findViewById(R.id.ct_tr_summ_cell_content_chk)).setImageResource(R.mipmap.icon_ct_mobile_yellow_small_png);
                v.setTag(contentRecapVO);
                v.setOnClickListener(new TransferSummaryListener(context));

            }else{
                ((ImageView)v.findViewById(R.id.ct_tr_summ_cell_content_chk)).setImageResource(R.mipmap.icon_ct_black_tick_small);
                v.setTag(contentRecapVO);
                v.setOnClickListener(new TransferSummaryListener(context));
            }
            v.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    LogUtil.d(TAG, "row clicked..." + position);
                    Intent intent = new Intent( context, CTTransferStatusActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putString("UImedia",contentRecapVO.getUImedia());
                    bundle.putString("media",contentRecapVO.getContentType());
                    bundle.putBoolean("mediaPermission",contentRecapVO.isMediaPermitted());
                    if(contentRecapVO.getTransferSize()!=contentRecapVO.getContentSize()){
                        LogUtil.d("RECAP", "not equal...");
                        bundle.putBoolean("isComplete", false);
                    }else {
                        LogUtil.d("RECAP", "is equal...");
                        bundle.putBoolean("isComplete", true);
                    }
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }

            });
        }
        return v;
    }
}

