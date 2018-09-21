package com.verizon.contenttransfer.p2p.receiver;

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

/**
 * IOSCalendarReceiver processes /Download/vztransfer/client_calendar_list.txt , fetches .ics files
 * from sending device and imports into receiving device
 * @author  palanpr
 * @version 1.0
 * @since   2016-04-20
 *
 */
public class CalendarReceiver {
    //
    private static final String TAG = CalendarReceiver.class.getName();
    //private static final Context context = P2PStartupActivity.contentTransferContext;

    /**
     * This method parses /Download/vztransfer/client_calendar_list.txt on receiving device.
     * It gets path (of .ics file) and size from json and requests to download .ics residing on sending device.
     * Once .ics files are downloaded to /Download/vztransfer/calendar, it begins to import calendar to receiving device.
     */
    public static void startProcessingCalendarList(Socket iosServerSocket) throws IOException {
        LogUtil.d(TAG, "startProcessingCalendarList Begin");

        File client_calendar_file = new File(VZTransferConstants.VZTRANSFER_DIR, VZTransferConstants.CLIENT_CALENDAR_FILE); ///Download/vztransfer/client_calendar_list.txt
        MediaFetchingService mediaService = new MediaFetchingService();
        String calendarFileName = null;
        try {
            BufferedReader calendarReader = new BufferedReader(new FileReader(client_calendar_file));
            while ((calendarFileName = calendarReader.readLine()) != null) {
                //LogUtil.d(TAG, "Calendar to be received : " + calendarFileName);
            }

            JSONParser parser = new JSONParser();
            JSONArray calendarListArray = (JSONArray) parser.parse(new FileReader(client_calendar_file));

            for (Object calendar : calendarListArray) {
                JSONObject jsonObject = (JSONObject) calendar;

                String calendarFilePath = (String) jsonObject.get("Path");
                LogUtil.d(TAG, "File to be processed : " + calendarFilePath); // /storage/emulated/0/Download/vztransfer/calendar/abc.ics

                String calendarFileSize = (String) jsonObject.get("Size");
                LogUtil.d(TAG, "Size of the File : " + calendarFileSize);

                mediaService.fetchMedia(iosServerSocket, VZTransferConstants.CALENDAR, jsonObject, calendarFileSize,false);//gets .ics files from sending device.
            }
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, "Exception FNF");
            e.printStackTrace();
        } catch (ParseException e) {
            LogUtil.e(TAG, "Exception Parse");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        LogUtil.d(TAG, "startProcessingCalendarList End");
    }
}
