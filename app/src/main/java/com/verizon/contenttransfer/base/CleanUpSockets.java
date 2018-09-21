package com.verizon.contenttransfer.base;

import com.verizon.contenttransfer.model.CTSelectContentModel;
import com.verizon.contenttransfer.model.SetupModel;
import com.verizon.contenttransfer.p2p.model.CollectionTaskVO;
import com.verizon.contenttransfer.p2p.model.HotSpotInfo;
import com.verizon.contenttransfer.p2p.receiver.ReceiveMetadata;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.p2p.service.P2PServerIos;
import com.verizon.contenttransfer.utils.CTAppUtil;
import com.verizon.contenttransfer.utils.CustomDialogs;
import com.verizon.contenttransfer.utils.DataSpeedAnalyzer;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.MediaFileNameGenerator;
import com.verizon.contenttransfer.utils.QRCodeUtil;
import com.verizon.contenttransfer.utils.SocketUtil;
import com.verizon.contenttransfer.wifidirect.DeviceIterator;

/**
 * Created by c0bissh on 3/15/2016.
 */
public class CleanUpSockets  {
    private static String TAG = CleanUpSockets.class.getName();
    // Called from P2pFinishActivity and P2PStartupActivity.
    public static void resetVariablesOnStartUp(){
        ReceiveMetadata.IS_HOME_PAGE = false;
        DeviceIterator.goToMVMHome = false;
        MediaFetchingService.isP2PFinishActivityLaunched = false;
        CTBatteryLevelReceiver.isBatteryLevelAlertDialogDisplay = false;
        CollectionTaskVO.getInstance().reset();
        CTAppUtil.getInstance().reset();
        QRCodeUtil.getInstance().reset();
        cleanUpAllVariables();


    }


    // Called from P2pFinishActivity and P2PStartupActivity.
    public static void cleanUpAllVariables() {
        try {

            MediaFileListGenerator.resetVariables();
            ReceiveMetadata.resetVariables();
            DataSpeedAnalyzer.resetValues();
            CustomDialogs.resetValues();
            MediaFileNameGenerator.resetVariables();
            P2PServerIos.resetVariables();
            MediaFetchingService.isContactReceived = false;
            MediaFetchingService.isCalllogsReceived = false;
            HotSpotInfo.resetHotspotInfo(); //clear everything
            CTSelectContentModel.getInstance().resetVariables();

            cleanupThread();

        }catch (Exception e){
            e.getStackTrace();
        }
    }

    private static void cleanupThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtil.d(TAG, "Clean up sockets..Thread");
                    SetupModel.getInstance().releaseWakelock();

                    SocketUtil.destroyAllSocket();
                }catch(Exception e){
                    LogUtil.d(TAG, "Clean up sockets..Thread exception :"+e.getMessage());
                }
            }
        };
        new Thread(runnable).start();
    }
}
