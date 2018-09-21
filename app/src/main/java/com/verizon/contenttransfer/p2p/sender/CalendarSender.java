package com.verizon.contenttransfer.p2p.sender;

import android.content.ContentResolver;
import android.database.Cursor;

import com.verizon.contenttransfer.activity.P2PSetupActivity;
import com.verizon.contenttransfer.base.VZTransferConstants;
import com.verizon.contenttransfer.utils.CTGlobal;
import com.verizon.contenttransfer.utils.CalendarUtil.AndroidCalendar;
import com.verizon.contenttransfer.utils.CalendarUtil.CalendarContractWrapper.Events;
import com.verizon.contenttransfer.utils.CalendarUtil.SaveCalendar;
import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MediaFileListGenerator;
import com.verizon.contenttransfer.utils.Utils;
import com.verizon.contenttransfer.utils.UtilsFromApacheLib.CTFileUtils;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.CompatibilityHints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IOSCalendarSender fetches only local calendars from sending device, generates .ics file for each calendar and stores in /Download/vztransfer/calendar folder.
 * Also generates /Download/vztransfer/calendar_list.txt and sends to receiving device.
 * @author  palanpr
 * @version 1.0
 * @since   2016-04-20
 */
public class CalendarSender {

    private static final String TAG = CalendarSender.class.getName();
    private static final String calendarLogHeader = "VZCONTENTTRANSFERCALENLOGAND";

    private static final String[] EVENT_COLS = new String[]{
            Events._ID, Events.ORIGINAL_ID, Events.UID_2445, Events.TITLE, Events.DESCRIPTION,
            Events.ORGANIZER, Events.EVENT_LOCATION, Events.STATUS, Events.ALL_DAY, Events.RDATE,
            Events.RRULE, Events.DTSTART, Events.EVENT_TIMEZONE, Events.DURATION, Events.DTEND,
            Events.EVENT_END_TIMEZONE, Events.ACCESS_LEVEL, Events.AVAILABILITY, Events.EXDATE,
            Events.EXRULE, Events.CUSTOM_APP_PACKAGE, Events.CUSTOM_APP_URI, Events.HAS_ALARM
    };

    /**
     * This method is used to generate /Download/vztransfer/calendar_list.txt in sending device
     */
    public static void generateCalendarListFile() {
        LogUtil.d(TAG, "generateCalendarListFile : Begin Calendar file list generation...calendar_list.txt ");

        MediaFileListGenerator fileListGenerator = new MediaFileListGenerator();
        fileListGenerator.getCalendarFileList( CTGlobal.getInstance().getContentTransferContext() );

        LogUtil.d(TAG, "generateCalendarListFile : End Calendar file list generation...calendar_list.txt ");
    }

    /**
     * This method is used to send /Download/vztransfer/calendar_list.txt to receiving device
     */
    public static void sendCalendarFileList(Socket iosClientSocket) {
        LogUtil.d(TAG, "sendCalendarFileList : Begin");

        File calendarFile = null;
        InputStream calendarStream = null;
        OutputStream out = null;
        PrintWriter writer = null;

        try {
            calendarFile = new File(VZTransferConstants.VZTRANSFER_DIR + VZTransferConstants.CALENDAR_FILE);
            calendarStream = new FileInputStream(calendarFile);
            out = iosClientSocket.getOutputStream();

            long calendarFileSize = calendarFile.length();
            writer = new PrintWriter(out, true);
            out.flush();

            String filesize = Long.toString(calendarFileSize);
            LogUtil.d(TAG, "File size : " + filesize);

            int numDigits = filesize.length();
            LogUtil.d(TAG, "Numb of digists : " + numDigits);

            if (numDigits < 10) {
                for (int k = 0; k < (10 - numDigits); k++) {
                    filesize = "0" + filesize;
                }
            }

            LogUtil.d(TAG, "File size : " + filesize);
            String calendarHeader = calendarLogHeader + filesize;
            out.write(calendarHeader.getBytes());
            out.flush();

            LogUtil.d(TAG, "Calendar Log Header sent.");
            byte[] buffer = new byte[4096];
            int len = calendarStream.read(buffer);

            while (len != -1) {
                out.write(buffer, 0, len);
                LogUtil.d(TAG, "Calendar file List transfer in progress... size " + len);
                len = calendarStream.read(buffer);
            }
            out.flush();
            LogUtil.d(TAG, "Calendar File list transfer complete");

        } catch (FileNotFoundException e) {
            LogUtil.d(TAG, "Calendar file not found" + e.getMessage());
        } catch (IOException e) {
            LogUtil.d(TAG, "Failed to send Calendar file" + e.getMessage());
        } finally{
            if(null != writer){
                writer.close();
            }
            if(null != calendarStream){
                try {
                    calendarStream.close();
                } catch (IOException e) {
                    LogUtil.e(TAG,"Exception in close calendarStream stream");
                    e.printStackTrace();
                }
            }
        }

        LogUtil.d(TAG, "sendCalendarFileList : End");
    }

    /**
     * This method fetches only local calendars from sending device, generates .ics file for each calendar
     * using ical4j.jar and stores in /Download/vztransfer/calendar folder.
     * It returns, calendar display Name, full path of corresponding .ics file.
     * @return Map<String,String>
     */
    public static Map<String, String> exportCalendars() {
        ContentResolver contentResolver = P2PSetupActivity.activity.getContentResolver();
        long startTime = System.currentTimeMillis();

        LogUtil.d(TAG, "exportCalendars : Begin");

        Map<String, String> calMap = new HashMap<String, String>();
        FileOutputStream fout = null;
        CalendarOutputter outputter = null;

        try {
            File tempDirPath = null;
            List<AndroidCalendar> calendar = AndroidCalendar.loadAll(contentResolver); //loads all calendars in sending device.

            if (null != calendar && !calendar.isEmpty()) {
                tempDirPath = createCalendarTempDirectory(); // create Download/vztransfer/calendar folder.
                MediaFileListGenerator.TOT_CALENDAR = 0;

                for (AndroidCalendar selectedCal : calendar) {
                    // break calendar collection if calendar is not selected.
                    if(!Utils.shouldCollect(VZTransferConstants.CALENDAR_STR)){
                        break;
                    }

                    if (!"LOCAL".equalsIgnoreCase(selectedCal.mAccountType)) {  //only local calendars
                        continue;
                    }



                    String fileName = VZTransferConstants.tempCalendarStoragePath + File.separator + selectedCal.mDisplayName + ".ics"; //to store .ics file in /Download/vztransfer/calendar folder.
                    LogUtil.d(TAG, "Save id " + selectedCal.mIdStr + " to file " + fileName);

                    // query events
                    //String selection = "( ("+ "title" + " LIKE '"+name+"') AND ( deleted != 1 ) )";
                    String selection = Events.CALENDAR_ID + "=? AND ( deleted != 1 )";
                    //String selection = Events.CALENDAR_ID + "=?";
                    String[] selectionArgs = new String[]{selectedCal.mIdStr};
                    final String sortBy = Events.CALENDAR_ID + " ASC";
/*                    Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/events"),
                            projection, selection, selectionArgs, null);*/

                    Cursor cur = contentResolver.query(Events.CONTENT_URI, EVENT_COLS, selection, selectionArgs, sortBy);

                    boolean relaxed = true;
                    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, relaxed);

                    Calendar cal = new Calendar();
                    String prodId = "-//" + selectedCal.mDisplayName + "//EN";
                    cal.getProperties().add(new ProdId(prodId));
                    cal.getProperties().add(Version.VERSION_2_0);
                    cal.getProperties().add(Method.PUBLISH);
                    cal.getProperties().add(CalScale.GREGORIAN);
                    cal.getProperties().add(new XProperty("X-WR-CALNAME", selectedCal.mDisplayName));

                    if (selectedCal.mTimezone != null) {
                        // We don't write any events with floating times, but export this
                        // anyway so the default timezone for new events is correct when
                        // the file is imported into a system that supports it.
                        cal.getProperties().add(new XProperty("X-WR-TIMEZONE", selectedCal.mTimezone));
                    }

                    DtStamp timestamp = new DtStamp(); // Same timestamp for all events
                    int i = 0;
                    // Collect up events and add them after any timezones
                    List<VEvent> events = new ArrayList<>();

                    try {
                        while (cur.moveToNext()) {
                            VEvent e = SaveCalendar.convertFromDb(contentResolver, cur, cal, timestamp);
                            if (e != null) {
                                events.add(e);
                            }
                            i++;
                            if(!Utils.shouldCollect(VZTransferConstants.CALENDAR_STR)){
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cur.close();
                    }

                    if (events.isEmpty()) {
                        LogUtil.d(TAG, "Calendar : " + selectedCal.mDisplayName + " has no events");
                        continue;
                    }

                    for (VEvent v : events) {
                        cal.getComponents().add(v);
                    }

                    //Saving an iCalendar file
                    fout = new FileOutputStream(fileName);
                    outputter = new CalendarOutputter(); //ical4j helps to generate .ics file.
                    outputter.setValidating(false);
                    outputter.output(cal, fout);
                    LogUtil.d(TAG, "File created : " + fileName.toString());

                    MediaFileListGenerator.TOT_CALENDAR++;
                    calMap.put(selectedCal.mDisplayName, Long.toString(new File(fileName).length()));
                }

            } else {
                LogUtil.d(TAG, "There are no calendars");
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "Exception in exportCalendars ");
            e.printStackTrace();

        } finally {
            if (null != outputter) {
                outputter = null;
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, "Exception in close fout stream ");
                    e.printStackTrace();
                }
            }
            LogUtil.d(TAG, "exportCalendars : End");
            long endTime = System.currentTimeMillis();

            LogUtil.d(TAG, "ExportCalendars  time spent to build ICS File :" + (endTime - startTime));

            return calMap;
        }

    }

    /**
     * creates folder ../Download/vztransfer/calendar to store .ics files
     * @return File
     */
    private static File createCalendarTempDirectory() {
        LogUtil.d(TAG, "createCalendarTempDirectory Begin");
        File file = null;

        try {

            String fileName = VZTransferConstants.tempCalendarStoragePath;
            file = new File(fileName);

            if (!file.exists()) {
                if (file.mkdir()) {
                    LogUtil.d(TAG, "Directory is created : " + fileName.toString());
                } else {
                    LogUtil.d(TAG, "Failed to create directory : " + fileName.toString());
                }
            } else {
                LogUtil.d(TAG, "Directory exists : " + fileName.toString());
                CTFileUtils.cleanDirectory(file);
            }

        } catch (IOException e) {
            LogUtil.e(TAG, "Exception createCalendarTempDirectory");
            e.printStackTrace();

        } finally {
            LogUtil.d(TAG, "createCalendarTempDirectory End");
            return file;
        }
    }
}

