package com.verizon.contenttransfer.p2p.receiver;

/**
 * Created by yempasu on 3/31/2017.
 */

import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.p2p.service.MediaFetchingService;
import com.verizon.contenttransfer.utils.LogUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class AppReceiver {
    private static final String TAG = AppReceiver.class.getName();

    private static String FILE_NAME = VZTransferConstants.CLIENT_APPS_LIST_FILE;        //"apps";

    public static void startProcessingAppsList(Socket iosServerSocket) throws IOException {

        File client_app_file = new File(VZTransferConstants.VZTRANSFER_DIR, FILE_NAME);
        MediaFetchingService mediaService = new MediaFetchingService();

        try {
            if (LogUtil.DEBUG) {
                BufferedReader appReader = new BufferedReader(new FileReader(client_app_file));
                String appFileName = null;
                while ((appFileName = appReader.readLine()) != null) {
                    LogUtil.d(TAG, "Apps to be received : " + appFileName);
                }
            }

            JSONParser parser = new JSONParser();

            LogUtil.d(TAG, "Parsing App List....");
            JSONArray appListArray = (JSONArray) parser.parse(new FileReader(client_app_file));

            for (Object app : appListArray) {
                JSONObject jsonObject = (JSONObject) app;

                String appFileSize = (String) jsonObject.get("Size");
                LogUtil.d(TAG, "Size of the File : " + appFileSize);

                mediaService.fetchMedia(iosServerSocket, VZTransferConstants.APPS, jsonObject, appFileSize, false);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException se) {
            LogUtil.d(TAG, "Socket Exception.. " + se.getMessage());
            throw new IOException(VZTransferConstants.CT_CUSTOM_EXCEPTION+"Process app list:"+se.getMessage());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}

