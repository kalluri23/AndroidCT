package com.verizon.contenttransfer.utils.CalendarUtil;

/**
 * Created by palanpr on 4/11/2016.
 */
public class RestoreCalendar {

    private static final String TAG = "RestoreCalendar";
/*    public RestoreCalendar(Context ctx) {

        processCalendars(ctx);


    }*/


    /*private void processCalendars(Context ctx) {



        File trfrIcsFiles[] = null;

        try {

            //get files
            //get only local calendars from receiving device
            //check calendar exist in receiving device
            //if cal doesnot exist
            //create a calendar...get it's id
            //else
            //insert the events into exisitng calendar

            List < String > trfrCalendarNames = new ArrayList < String > ();
            Map < String, Long > rDeviceCalendarNames = new HashMap < String, Long > ();
            List < String > createCalendarNames = new ArrayList < String > ();
            //create calendars map for using on receiving device
            HashMap < Long, String > finalmap = new HashMap < Long, String > ();

            //get files from storage
            File calendarFile = new File(VZTransferConstants.tempCalendarStoragePath);
            if (null != calendarFile && calendarFile.isDirectory()) {
                trfrIcsFiles = calendarFile.listFiles();
                if (null != trfrIcsFiles) {
                    for (int i = 0; i < trfrIcsFiles.length; i++) {
                        String fileNameWithOutExt = FilenameUtils.removeExtension(trfrIcsFiles[i].getName());
                        trfrCalendarNames.add(fileNameWithOutExt);
                    }
                }
            }

            //get only local calendars from receiving device
            List < AndroidCalendar > rDeviceCalendars = AndroidCalendar.loadAll(ctx.getContentResolver());
            if (null != rDeviceCalendars) {
                for (AndroidCalendar rcal: rDeviceCalendars) {
                    if ("LOCAL".equalsIgnoreCase(rcal.mAccountType)) {
                        rDeviceCalendarNames.put(rcal.mDisplayName, rcal.mId);
                    }
                }
            }

            //these calendars needs to be created on receiving device
            for (String name: trfrCalendarNames) {
                if (rDeviceCalendarNames.containsKey(name)) {
                    finalmap.put(rDeviceCalendarNames.get(name), name);
                } else {
                    createCalendarNames.add(name);
                }
            }



            //create the calendars on receiing device
            if (!createCalendarNames.isEmpty()) {
                for (String calname: createCalendarNames) {
                    finalmap = CalendarController.addCalendar(ctx, calname, 0, ctx.getContentResolver(), finalmap);
                }
            }



            if (null != calendarFile && calendarFile.isDirectory()) {
                trfrIcsFiles = calendarFile.listFiles();
                if (null != trfrIcsFiles) {
                    for (int i = 0; i < trfrIcsFiles.length; i++) {
                        String calendarFilePath = trfrIcsFiles[i].getPath();

                        URL url = new URL("file://" + calendarFilePath);
                        URLConnection c = url.openConnection();
                        InputStream in = c == null ? null : c.getInputStream();

                        Calendar mCalendar = in == null ? null : new CalendarBuilder().build( in );

                        long calId = -1;

                        for (Map.Entry < Long, String > entry: finalmap.entrySet()) {
                            long key = entry.getKey();
                            String value = entry.getValue();
                            String icsfileNameWithOutExt = FilenameUtils.removeExtension(trfrIcsFiles[i].getName());

                            if (value.equalsIgnoreCase(icsfileNameWithOutExt)) {
                                calId = key;
                            }
                        }


                        ProcessVEvent process = new ProcessVEvent(mCalendar, true, ctx, calId);

                    }
                }
            }


            //delete calendarTemp

            File removeDir = new File(VZTransferConstants.tempCalendarStoragePath);
            FileUtils.deleteDirectory(removeDir);

        } catch (java.net.MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }*/








}