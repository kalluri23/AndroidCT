package com.verizon.contenttransfer.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.verizon.contenttransfer.R;
import com.verizon.contenttransfer.adapter.ReceiverAppListAdapter;
import com.verizon.contenttransfer.base.ContentPreference;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.feedback.CTErrorReporter;
import com.verizon.contenttransfer.listener.CTReceiverAppsListListener;
import com.verizon.contenttransfer.model.CTReceiverAppsListModel;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.Utils;

import java.io.File;

/**
 * Created by yempasu on 4/7/2017.
 */
public class CTReceiverAppsListView implements ReceiverAppListAdapter.customButtonListener{
    private static String TAG = CTReceiverAppsListView.class.getName();
    private Activity activity;
    private ReceiverAppListAdapter receiverAppListAdapter;
    private ListView appListView;
    private boolean shouldExecuteOnResume = false;
    private static CTReceiverAppsListView instance;
    private TextView selectAll;
    private TextView installAll;

    public CTReceiverAppsListView(Activity activity) {
        this.activity = activity;
        instance = this;

        initView();
        CTReceiverAppsListModel.getInstance().initModel(activity);
    }

    public static CTReceiverAppsListView getInstance(){
        return instance;
    }

    private void initView() {
        CTErrorReporter.getInstance().Init(activity);
        activity.setContentView(R.layout.ct_receiver_apps);

        // Set default keep apps flag in shared preference is true.
        ContentPreference.putBooleanValue(activity, ContentPreference.KEEP_APPS, true);

        ((TextView)activity.findViewById(R.id.ct_toolbar_app_headerTV)).setText(R.string.toolbar_heading_install);


        if(Utils.isStandAloneBuild()){
            activity.findViewById(R.id.search_icon).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setVisibility(View.INVISIBLE);
            activity.findViewById(R.id.ct_toolbar_backIV).setVisibility(View.INVISIBLE);
        }


        View.OnClickListener appsPageListener = new CTReceiverAppsListListener(activity);
        activity.findViewById(R.id.search_icon).setOnClickListener(appsPageListener);
        activity.findViewById(R.id.ct_toolbar_hamburger_menuIV).setOnClickListener(appsPageListener);
        activity.findViewById(R.id.ct_toolbar_backIV).setOnClickListener(appsPageListener);
        activity.findViewById(R.id.ct_btn_done).setOnClickListener(appsPageListener);
        activity.findViewById(R.id.install_all).setOnClickListener(appsPageListener);
        activity.findViewById(R.id.ct_select_all_apps).setOnClickListener(appsPageListener);

        appListView =(ListView) activity.findViewById(R.id.ct_app_list);
        selectAll = (TextView) activity.findViewById(R.id.ct_select_all_apps);
        installAll= (TextView) activity.findViewById(R.id.install_all);


        CTAppUtil.getInstance().updateReceivedAppList(activity, false);
        receiverAppListAdapter = new ReceiverAppListAdapter(activity.getApplicationContext(), R.layout.ct_receiver_apps_list_cell, CTAppUtil.getInstance().getReceivedApps());
        receiverAppListAdapter.setCustomButtonListner(CTReceiverAppsListView.this);
        appListView.setAdapter(receiverAppListAdapter);
    }

    public String getIntentMessage(String key){
        Intent intent = activity.getIntent();
        return intent.getStringExtra(key);
    }
    @Override
    public void onButtonClickListener(int position, File apkURL) {

        final ImageView install = (ImageView) activity.findViewById(R.id.check);
        install.setVisibility(View.INVISIBLE);

        CTReceiverAppsListModel.getInstance().onInstallClicked(position,apkURL);
    }

    public void callNotifyDataSetChanged(){
        if(null != receiverAppListAdapter) {
            receiverAppListAdapter.notifyDataSetChanged();
            enableDisableSelectAll();
        }
    }

    public void handleResume() {
        LogUtil.d(TAG,"shouldExecuteOnResume ="+shouldExecuteOnResume);
        if(shouldExecuteOnResume){
            CTAppUtil.getInstance().updateReceivedAppList(activity, shouldExecuteOnResume);
        } else{
            shouldExecuteOnResume = true;
        }
    }

    public void selectAllTextChange(boolean selected){
        if(selected){
            selectAll.setText(activity.getString(R.string.deselect_all));
        }else{
            selectAll.setText(activity.getString(R.string.select_all));
        }
    }

    public void enableInstallButton(boolean isEnable) {
        LogUtil.d(TAG, "Enable the Install button");
        if(installAll!=null) {
            if (isEnable) {
                installAll.setEnabled(true);
                installAll.setBackgroundResource(R.drawable.ct_button_solid_black_bg);
            } else {
                installAll.setEnabled(false);
                installAll.setBackgroundResource(R.mipmap.ic_ct_grey_solid_button);
            }
        }
    }
    public void enableDisableSelectAll(){
        if(CTAppUtil.getInstance().showSelectAll()){
            selectAll.setVisibility(View.VISIBLE);
        }else{
            selectAll.setVisibility(View.INVISIBLE);
        }
    }
}
