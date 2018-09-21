package com.verizon.contenttransfer.p2p.asynctask;

import android.os.AsyncTask;

import com.verizon.contenttransfer.base.CTBatteryLevelReceiver;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.model.ServerConnectionObject;
import com.verizon.contenttransfer.utils.CTAnalyticUtil;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.SocketUtil;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by kommisu on 2/22/2016.
 */
public class CommMessageAsyncTask extends AsyncTask<Void, Void, String> {
// SENDER Device.
    private static final String TAG = CommMessageAsyncTask.class.getName();
    //public static boolean CLOSE_COMM_ASYNC_ON_CLOSE = false;
    private BufferedReader msgReader = null;
    private String clientIp = null;
    //private PrintWriter msgWriter = null;

    public CommMessageAsyncTask(Socket commClientSocket) {
        clientIp = commClientSocket.getInetAddress().getHostAddress();
        LogUtil.d(TAG, "Comm msg async task :"+clientIp);
        try {
            //msgWriter = new PrintWriter(commClientSocket.getOutputStream(), true);
            msgReader = new BufferedReader(new InputStreamReader(commClientSocket.getInputStream()));
        } catch (IOException e) {
            LogUtil.e(TAG,"CommMessageAsync Task exc :"+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(Void... params) {

        String message = null;		//P2PClient.readMessageFromCommSocket();
        do {
            if ( null != msgReader ) {

                try {
                    message = msgReader.readLine();
                }catch(IOException e) {
                    LogUtil.d(TAG,"Exception on reading msg : "+e.getMessage());
                    if(e.getMessage().contains(VZTransferConstants.SOCKET_CLOSED)
                            || e.getMessage().contains(VZTransferConstants.BAD_FILE_DESCRIPTOR) // recvfrom failed: EBADF (Bad file descriptor)
                            || CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.landingActivity)
                            || CTBatteryLevelReceiver.getTopActivityName().contains(VZTransferConstants.p2PFinishActivity)){
                        break;
                    }

                    message=null;
                }

                //LogUtil.d(TAG,"Comm msg received ="+cancelMessage);
                if ( null != message ) {
                    message = handleMessages(message);

                }else if(CTGlobal.getInstance().getExitApp()){ // based on this message we are closing this async task.
                    LogUtil.d(TAG, "exiting from while loop from sender side finish page - Close message received");
                    //message = VZTransferConstants.CLOSE_COMM_ON_FINISH_TRANSFER;
                    break;
                }
            } else {
                LogUtil.d(TAG, "Input stream is null, waiting to initialize it.....");
            }
            //LogUtil.d(TAG, "b4 sleep cancelMessage = "+cancelMessage);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while ( null == message);

        if(msgReader != null){
            try {
                msgReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String handleMessages(String message){

        LogUtil.d(TAG, "handleMessages - Comm msg received =" + message);
        if (message.contains(VZTransferConstants.DB_PAIRING_DEVICE_ID)) { // INSUFFICIENT_STORAGE_SPACE info getting using version check communication. So, not processed here.
            JSONParser parser = new JSONParser();
            JSONObject metaData = null;
            try {
                metaData = (JSONObject) parser.parse(message);
                if(null != metaData.get(VZTransferConstants.DB_PAIRING_MODEL)) {
                    CTGlobal.getInstance().setParingDeviceModel(metaData.get(VZTransferConstants.DB_PAIRING_MODEL).toString());
                    if(!SocketUtil.isClient()){
                        ServerConnectionObject.getInstance().updateAcceptedDeviceName(clientIp,CTGlobal.getInstance().getParingDeviceModel());
                    }
                }
                if(null != metaData.get(VZTransferConstants.DB_PAIRING_DEVICE_ID)) {
                    CTGlobal.getInstance().setParingDeviceID(metaData.get(VZTransferConstants.DB_PAIRING_DEVICE_ID).toString());
                }
                if(null != metaData.get(VZTransferConstants.DB_PAIRING_OS_VERSION)) {
                    CTGlobal.getInstance().setParingDeviceOSVersion(metaData.get(VZTransferConstants.DB_PAIRING_OS_VERSION).toString());
                }
                if(null != metaData.get(VZTransferConstants.DB_PAIRING_DEVICE_TYPE)) {
                    CTGlobal.getInstance().setParingDeviceDeviceType(metaData.get(VZTransferConstants.DB_PAIRING_DEVICE_TYPE).toString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            message = null;
        }else if(message.contains(VZTransferConstants.VZTRANSFER_CANCEL)){
            try {
                CTAnalyticUtil ctAnalyticUtil = SocketUtil.getCtAnalyticUtil(clientIp);
                ctAnalyticUtil.setVztransferCancelled(true);
                SocketUtil.disconnectSocketByIp(clientIp);
            }catch (Exception e){
                LogUtil.d(TAG, e.getMessage());
            }
        }else if(message.equals(VZTransferConstants.CLOSE_COMM_ON_FINISH_TRANSFER)) {
            LogUtil.d(TAG, "CommMessageAsyncTask - Close message received");
        }

        return message;
    }

    @Override
    protected void onPostExecute(String result) {
        LogUtil.d(TAG, "Comm message async task onPostExecute");
    }

    @Override
    protected void onPreExecute() {
        LogUtil.d(TAG, "Comm message async task onPreExecute ");
    }
}
