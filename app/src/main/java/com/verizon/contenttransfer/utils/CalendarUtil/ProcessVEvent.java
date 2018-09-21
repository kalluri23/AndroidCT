
/**
 *  Copyright (C) 2013
 *  Copyright (C) 2010-2011
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *
 */

package com.verizon.contenttransfer.utils.CalendarUtil;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.MailTo;
import android.net.ParseException;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.verizon.contenttransfer.utils.LogUtil;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Trigger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@SuppressLint("NewApi")
public class ProcessVEvent {
    private static final String TAG = "Calendar_ProcessVEvent";

    private static final Duration ONE_DAY = createDuration("P1D");
    private static final Duration ZERO_SECONDS = createDuration("PT0S");

    private static final String[] EVENT_QUERY_COLUMNS = new String[]{CalendarContractWrapper.Events.CALENDAR_ID, CalendarContractWrapper.Events._ID};
    private static final int EVENT_QUERY_CALENDAR_ID_COL = 0;
    private static final int EVENT_QUERY_ID_COL = 1;

    private final Calendar mICalCalendar;
    private final boolean mIsInserter;
    private Context ctx = null;
    //private List<Integer> remindersTemp = new ArrayList<Integer>();
    private long calId = -1;


    public ProcessVEvent(Calendar iCalCalendar, boolean isInserter, Context ctx, long calId) {
        // super(activity, R.string.processing_entries, true);
        mICalCalendar = iCalCalendar;
        mIsInserter = isInserter;
        this.ctx = ctx;
        this.calId = calId;
        try {
            //LogUtil.d(TAG," :---Inside constructor ProcessVEvent - before calling run");
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Duration createDuration(String value) {
        Duration d = new Duration();
        d.setValue(value);
        return d;
    }

    private static long durationToMs(Dur d) {
        long ms = 0;
        ms += d.getSeconds() * DateUtils.SECOND_IN_MILLIS;
        ms += d.getMinutes() * DateUtils.MINUTE_IN_MILLIS;
        ms += d.getHours() * DateUtils.HOUR_IN_MILLIS;
        ms += d.getDays() * DateUtils.DAY_IN_MILLIS;
        ms += d.getWeeks() * DateUtils.WEEK_IN_MILLIS;
        return ms;
    }

    void run() throws Exception {
        try {

            final Options options = new Options();
            //LogUtil.d(TAG,ty = getActivity();
            //LogUtil.d(TAG," :-------------------1------options------" + options);
            //   final Options options = new Options(new MainActivity());
            final AndroidCalendar selectedCal = new AndroidCalendar(); //activity.getSelectedCalendar(); //TODO which calendar to insert to.

            selectedCal.mId = calId;
            //  selectedCal.mIdStr = "2";


            //LogUtil.d(TAG," :-------------------3------------");


            List<Integer> reminders = new ArrayList<>();

            ComponentList events = mICalCalendar.getComponents(VEvent.VEVENT);

            // setMax(events.size());
            ContentResolver resolver = this.ctx.getContentResolver();
            int numDel = 0;
            int numIns = 0;
            int numDups = 0;

            ContentValues cAlarm = new ContentValues();
            cAlarm.put(CalendarContractWrapper.Reminders.METHOD, CalendarContractWrapper.Reminders.METHOD_ALERT);

            //final Settings.DuplicateHandlingEnum dupes = options.getDuplicateHandling();

            //LogUtil.d(TAG,(mIsInserter ? "Insert" : "Delete") + " for id ");
            //LogUtil.d(TAG," :Duplication option is " + dupes.ordinal());

            for (Object ve : events) {

                //LogUtil.d(TAG," :Name Of Event-------" + ve.toString());

                VEvent e = (VEvent) ve;


                if (e.getRecurrenceId() != null) {
                    // FIXME: Support these edited instances
                    //LogUtil.d(TAG," :Ignoring edited instance of a recurring event");
                    continue;
                }

                long insertCalendarId = selectedCal.mId; // Calendar id to insert to

                ContentValues c = convertToDB(e, options, reminders, selectedCal.mId);

                Cursor cur = null;
                boolean mustDelete = false;

                // Determine if we need to delete a duplicate event in order to update it

                LogUtil.d(TAG,"ContentValues toString : " + c.toString());

                cur = query(resolver,  c);
                //LogUtil.d(TAG,"ProcessEvent Debug 1 - mustDelete :"+mustDelete+", is this exec...cur::"+cur.getCount());
                int test = 1;
                if(cur != null && cur.moveToFirst()){
                    do {
                        mustDelete = true;
                        LogUtil.d(TAG, "******** duplicate calendar event found *********** Total duplicate event :" + cur.getCount() + " - current count is :" + test++);
                        //MessageUtil.displayCursorProperties(cur, "calendar event test :");
                    }while (cur.moveToNext());
                }
/*                cur.moveToFirst();

                while (!mustDelete && cur != null && cur.moveToNext()) {

                    mustDelete = true;
                    LogUtil.d(TAG,"ProcessEvent Debug 4 : " + mustDelete);
                }*/


                //LogUtil.d(TAG," : 3..next line will not exec for unique." + mustDelete);
                if (mustDelete) {

                    if (true) {
                        //LogUtil.d(TAG," : Avoiding inserting a duplicate event");
                        numDups++;
                        cur.close();
                        continue;
                    }
                    cur.moveToPosition(-1); // Rewind for use below
                }



                if (cur != null) {
                    cur.close();
                }

                //LogUtil.d(TAG," : :  CalendarContractWrapper.Events.UID_2445---" + c.containsKey(CalendarContractWrapper.Events.UID_2445));

                if (CalendarContractWrapper.Events.UID_2445 != null && !c.containsKey(CalendarContractWrapper.Events.UID_2445)) {
                    // Create a UID for this event to use. We create it here so if
                    // exported multiple times it will always have the same id.
                    c.put(CalendarContractWrapper.Events.UID_2445, UUID.randomUUID().toString().replace("-", ""));
                }

                c.put(CalendarContractWrapper.Events.CALENDAR_ID, insertCalendarId);
              /*  if (options.getTestFileSupport()) {
                    processEventTests(e, c, reminders);
                    numIns++;
                    continue;
                }*/


                //LogUtil.d(TAG,"******* insert and log event ***************");
                Uri uri = insertAndLog(resolver, CalendarContractWrapper.Events.CONTENT_URI, c, "Event");
                if (uri == null)
                    continue;

                final long id = Long.parseLong(uri.getLastPathSegment());

                //LogUtil.d(TAG,"rem :getLastPathSegment ::::::::::::" + (id));

                for (int time : options.getReminders(reminders)) {
                    cAlarm.put(CalendarContractWrapper.Reminders.EVENT_ID, id);
                    cAlarm.put(CalendarContractWrapper.Reminders.MINUTES, time);

                    //LogUtil.d(TAG,"******* insert and log reminder ***************");
                    insertAndLog(resolver, CalendarContractWrapper.Reminders.CONTENT_URI, cAlarm, "Reminder");
                }
                numIns++;
            }

            selectedCal.mNumEntries += numIns;
            selectedCal.mNumEntries -= numDel;
            //   activity.updateNumEntries(selectedCal);

            //  Resources res = activity.getResources();
            int n = mIsInserter ? numIns : numDel;
            String msg = "processed number of entried---" + n;

           /* if (mIsInserter) {
                msg += "\n";
                if (options.getDuplicateHandling() == Settings.DuplicateHandlingEnum.DUP_DONT_CHECK)
                    msg += "didnot check for duplicates";
                else
                    msg += "found these many duplicated "+numDups;
            }*/

            // activity.showToast(msg);

            //LogUtil.d(TAG," : : end of run " + n);
        } catch (Exception e) {
            //LogUtil.d(TAG," : exception---" + e);
        }
    }

    // Munge a VEvent so Android won't reject it, then convert to ContentValues for inserting
    private ContentValues convertToDB(VEvent e, Options options,
                                      List<Integer> reminders, long calendarId) {
        reminders.clear();

        boolean allDay = false;
        boolean startIsDate = !(e.getStartDate().getDate() instanceof DateTime);
        boolean isRecurring = hasProperty(e, Property.RRULE) || hasProperty(e, Property.RDATE);

        if (startIsDate) {
            // If the start date is a DATE, the event is all-day, midnight to midnight (RFC 2445).
            // Add a duration of 1 day and remove the end date. If the event is non-recurring then
            // we will convert the duration to an end date below, which fixes all-day cases where
            // the end date is set to the same day at 23:59:59, rolls over because of a TZ, etc.
            e.getProperties().add(ONE_DAY);
            allDay = true;
            //  If an event is marked as all day it must be in the UTC timezone.
            e.getStartDate().setUtc(true);
            removeProperty(e, Property.DTEND);
        }

        if (!hasProperty(e, Property.DTEND) && !hasProperty(e, Property.DURATION)) {
            // No end date or duration given.
            // Since we added a duration above when the start date is a DATE:
            // - The start date is a DATETIME, the event lasts no time at all (RFC 2445).
            e.getProperties().add(ZERO_SECONDS);
            // Zero time events are always free (RFC 2445), so override/set TRANSP accordingly.
            removeProperty(e, Property.TRANSP);
            e.getProperties().add(Transp.TRANSPARENT);
        }

        if (isRecurring) {
            // Recurring event. Android insists on a duration.
            if (!hasProperty(e, Property.DURATION)) {
                // Calculate duration from start to end date
                Duration d = new Duration(e.getStartDate().getDate(), e.getEndDate().getDate());
                e.getProperties().add(d);
            }
            removeProperty(e, Property.DTEND);
        } else {
            // Non-recurring event. Android insists on an end date.
            if (!hasProperty(e, Property.DTEND)) {
                // Calculate end date from duration, set it and remove the duration.
                e.getProperties().add(e.getEndDate());
            }
            removeProperty(e, Property.DURATION);
        }

        // Now calculate the db values for the event
        ContentValues c = new ContentValues();

        c.put(CalendarContractWrapper.Events.CALENDAR_ID, calendarId);
        copyProperty(c, CalendarContractWrapper.Events.TITLE, e, Property.SUMMARY);
        copyProperty(c, CalendarContractWrapper.Events.DESCRIPTION, e, Property.DESCRIPTION);

        if (e.getOrganizer() != null) {
            URI uri = e.getOrganizer().getCalAddress();
            try {
                MailTo mailTo = MailTo.parse(uri.toString());
                c.put(CalendarContractWrapper.Events.ORGANIZER, mailTo.getTo());
                c.put(CalendarContractWrapper.Events.GUESTS_CAN_MODIFY, 1); // Ensure we can edit if not the organiser
            } catch (ParseException ignored) {
                //LogUtil.d(TAG," :Failed to parse Organiser URI " + uri.toString());
            }
        }

        copyProperty(c, CalendarContractWrapper.Events.EVENT_LOCATION, e, Property.LOCATION);

        if (hasProperty(e, Property.STATUS)) {
            String status = e.getProperty(Property.STATUS).getValue();
            switch (status) {
                case "TENTATIVE":
                    c.put(CalendarContractWrapper.Events.STATUS, CalendarContractWrapper.Events.STATUS_TENTATIVE);
                    break;
                case "CONFIRMED":
                    c.put(CalendarContractWrapper.Events.STATUS, CalendarContractWrapper.Events.STATUS_CONFIRMED);
                    break;
                case "CANCELLED":  // NOTE: In ical4j it is CANCELLED with two L
                    c.put(CalendarContractWrapper.Events.STATUS, CalendarContractWrapper.Events.STATUS_CANCELED);
                    break;
            }
        }

        copyProperty(c, CalendarContractWrapper.Events.DURATION, e, Property.DURATION);

        if (allDay)
            c.put(CalendarContractWrapper.Events.ALL_DAY, 1);

        copyDateProperty(c, CalendarContractWrapper.Events.DTSTART, CalendarContractWrapper.Events.EVENT_TIMEZONE, e.getStartDate());
        if (hasProperty(e, Property.DTEND))
            copyDateProperty(c, CalendarContractWrapper.Events.DTEND, CalendarContractWrapper.Events.EVENT_END_TIMEZONE, e.getEndDate());

        if (hasProperty(e, Property.CLASS)) {
            String access = e.getProperty(Property.CLASS).getValue();
            int accessLevel = CalendarContractWrapper.Events.ACCESS_DEFAULT;
            switch (access) {
                case "CONFIDENTIAL":
                    accessLevel = CalendarContractWrapper.Events.ACCESS_CONFIDENTIAL;
                    break;
                case "PRIVATE":
                    accessLevel = CalendarContractWrapper.Events.ACCESS_PRIVATE;
                    break;
                case "PUBLIC":
                    accessLevel = CalendarContractWrapper.Events.ACCESS_PUBLIC;
                    break;
            }

            c.put(CalendarContractWrapper.Events.ACCESS_LEVEL, accessLevel);
        }

        // Work out availability. This is confusing as FREEBUSY and TRANSP overlap.
        if (CalendarContractWrapper.Events.AVAILABILITY != null) {
            int availability = CalendarContractWrapper.Events.AVAILABILITY_BUSY;
            if (hasProperty(e, Property.TRANSP)) {
                if (e.getTransparency() == Transp.TRANSPARENT)
                    availability = CalendarContractWrapper.Events.AVAILABILITY_FREE;

            } else if (hasProperty(e, Property.FREEBUSY)) {
                FreeBusy fb = (FreeBusy) e.getProperty(Property.FREEBUSY);
                FbType fbType = (FbType) fb.getParameter(Parameter.FBTYPE);
                if (fbType != null && fbType == FbType.FREE)
                    availability = CalendarContractWrapper.Events.AVAILABILITY_FREE;
                else if (fbType != null && fbType == FbType.BUSY_TENTATIVE)
                    availability = CalendarContractWrapper.Events.AVAILABILITY_TENTATIVE;
            }
            c.put(CalendarContractWrapper.Events.AVAILABILITY, availability);
        }

        copyProperty(c, CalendarContractWrapper.Events.RRULE, e, Property.RRULE);
        copyProperty(c, CalendarContractWrapper.Events.RDATE, e, Property.RDATE);
        copyProperty(c, CalendarContractWrapper.Events.EXRULE, e, Property.EXRULE);
        copyProperty(c, CalendarContractWrapper.Events.EXDATE, e, Property.EXDATE);
        copyProperty(c, CalendarContractWrapper.Events.CUSTOM_APP_URI, e, Property.URL);
        copyProperty(c, CalendarContractWrapper.Events.UID_2445, e, Property.UID);
        if (c.containsKey(CalendarContractWrapper.Events.UID_2445) && TextUtils.isEmpty(c.getAsString(CalendarContractWrapper.Events.UID_2445))) {
            // Remove null/empty UIDs
            c.remove(CalendarContractWrapper.Events.UID_2445);
        }

        for (Object alarm : e.getAlarms()) {
            VAlarm a = (VAlarm) alarm;

            if (a.getAction() != Action.AUDIO && a.getAction() != Action.DISPLAY)
                continue; // Ignore email and procedure alarms

            Trigger t = a.getTrigger();
            final long startMs = e.getStartDate().getDate().getTime();
            long alarmStartMs = startMs;
            long alarmMs;

            // FIXME: - Support for repeating alarms
            //        - Check the calendars max number of alarms
            if (t.getDateTime() != null)
                alarmMs = t.getDateTime().getTime(); // Absolute
            else if (t.getDuration() != null && t.getDuration().isNegative()) {
                Related rel = (Related) t.getParameter(Parameter.RELATED);
                if (rel != null && rel == Related.END)
                    alarmStartMs = e.getEndDate().getDate().getTime();
                alarmMs = alarmStartMs - durationToMs(t.getDuration()); // Relative
            } else {
                continue;
            }

            int reminder = (int) ((startMs - alarmMs) / DateUtils.MINUTE_IN_MILLIS);
            if (reminder >= 0 && !reminders.contains(reminder))
                reminders.add(reminder);
        }

        //LogUtil.d(TAG,"rem  convertodb...  " + options);

        if (options.getReminders(reminders).size() > 0) {
            c.put(CalendarContractWrapper.Events.HAS_ALARM, 1);
        }

        // FIXME: Attendees, SELF_ATTENDEE_STATUS
        return c;
    }

    private boolean hasProperty(VEvent e, String name) {
        return e.getProperty(name) != null;
    }

    private void removeProperty(VEvent e, String name) {
        Property p = e.getProperty(name);
        if (p != null)
            e.getProperties().remove(p);
    }

    private void copyProperty(ContentValues c, String dbName, VEvent e, String evName) {
        if (dbName != null) {
            Property p = e.getProperty(evName);
            if (p != null)
                c.put(dbName, p.getValue());
        }
    }

    private void copyDateProperty(ContentValues c, String dbName, String dbTzName, DateProperty date) {
        if (dbName != null && date.getDate() != null) {
            c.put(dbName, date.getDate().getTime()); // ms since epoc in GMT
            if (dbTzName != null) {
                if (date.isUtc() || date.getTimeZone() == null)
                    c.put(dbTzName, "UTC");
                else
                    c.put(dbTzName, date.getTimeZone().getID());
            }
        }
    }

    private Uri insertAndLog(ContentResolver resolver, Uri uri, ContentValues c, String type) {


        //LogUtil.d(TAG,"Inserting " + type + " values: " + c);
        //LogUtil.d(TAG,"C here is-->" + (c.toString()));
        //LogUtil.d(TAG,"URI----------------" + uri);

        Uri result = null;

        try {
            result = resolver.insert(uri, c);
        } catch (Exception e) {
            //LogUtil.d(TAG,"exception here is :" + e);
            e.printStackTrace();
        }

        if (result == null) {
            //LogUtil.d(TAG," :failed to insert " + type + " values: " + c); // Not already logged, dump now
        } else {
            //LogUtil.d(TAG,"Insert " + type + " returned " + result.toString());
        }
        return result;
    }

    private Cursor queryEvents(ContentResolver resolver, StringBuilder b, List<String> argsList) {

        final String where = b.toString()+ "AND deleted != 1";
        //LogUtil.d(TAG,"event query ... string :"+where);
/*        for(int i=0;i<argsList.size();i++) {
            LogUtil.d(TAG, "event query ... arg list["+i+"] :" + argsList.get(i));
        }*/

        final String[] args = argsList.toArray(new String[argsList.size()]);

        return resolver.query(CalendarContractWrapper.Events.CONTENT_URI, EVENT_QUERY_COLUMNS, where, args, null);
    }

    private Cursor query(ContentResolver resolver, ContentValues c) {

        StringBuilder b = new StringBuilder();
        List<String> argsList = new ArrayList<>();
        if (CalendarContractWrapper.Events.UID_2445 != null && c.containsKey(CalendarContractWrapper.Events.UID_2445)) {
            // Use our UID to query, either globally or per-calendar unique
            //if (!options.getGlobalUids()) {
            b.append(CalendarContractWrapper.Events.CALENDAR_ID).append("=? AND ");
            argsList.add(c.getAsString(CalendarContractWrapper.Events.CALENDAR_ID));
            //}
            b.append(CalendarContractWrapper.Events.UID_2445).append("=?");
            argsList.add(c.getAsString(CalendarContractWrapper.Events.UID_2445));
            //LogUtil.d(TAG,"finding calendar events 3 : "+argsList.size());
            return queryEvents(resolver, b, argsList);
        }
        //LogUtil.d(TAG,"finding calendar events 4 : "+c.containsKey(CalendarContractWrapper.Events.CALENDAR_ID));
        // Without UIDs, the best we can do is check the start date and title within
        // the current calendar, even though this may return false duplicates.
        if (!c.containsKey(CalendarContractWrapper.Events.CALENDAR_ID) || !c.containsKey(CalendarContractWrapper.Events.DTSTART))
            return null;

        b.append(CalendarContractWrapper.Events.CALENDAR_ID).append("=? AND ");
        b.append(CalendarContractWrapper.Events.DTSTART).append("=? AND ");
        b.append(CalendarContractWrapper.Events.TITLE);

        argsList.add(c.getAsString(CalendarContractWrapper.Events.CALENDAR_ID));
        argsList.add(c.getAsString(CalendarContractWrapper.Events.DTSTART));
        //LogUtil.d(TAG,"finding calendar events 6 : "+CalendarContractWrapper.Events.TITLE);
        if (c.containsKey(CalendarContractWrapper.Events.TITLE)) {
            b.append("=?");
            argsList.add(c.getAsString(CalendarContractWrapper.Events.TITLE));
        } else {
            b.append(" is null");
        }
        //LogUtil.d(TAG,"finding calendar events 7 : "+b.toString());
        return queryEvents(resolver, b, argsList);
    }

    private void checkTestValue(VEvent e, ContentValues c, String keyValue, String testName) {
        String[] parts = keyValue.split("=");
        String key = parts[0];
        String expected = parts.length > 1 ? parts[1] : "";
        String got = c.getAsString(key);

        if (expected.equals("<non-null>") && got != null)
            got = "<non-null>"; // Sentinel for testing present and non-null
        if (got == null)
            got = "<null>"; // Sentinel for testing not present values

        if (!expected.equals(got)) {
            //LogUtil.d(TAG," :    " + keyValue + " -> FAILED");
            //LogUtil.d(TAG," :    values: " + c);
            String error = "Test " + testName + " FAILED, expected '" + keyValue + "', got '" + got + "'";
            throw new RuntimeException(error);
        }
        //LogUtil.d(TAG," :    " + keyValue + " -> PASSED");
    }

    private void processEventTests(VEvent e, ContentValues c, List<Integer> reminders) {

        Property testName = e.getProperty("X-TEST-NAME");
        if (testName == null)
            return; // Not a test case

        // This is a test event. Verify it using the embedded meta data.
        //LogUtil.d(TAG," :Processing test case " + testName.getValue() + "...");

        String reminderValues = "";
        String sep = "";
        for (Integer i : reminders) {
            reminderValues += sep + i;
            sep = ",";
        }
        c.put("reminders", reminderValues);

        for (Object o : e.getProperties()) {
            Property p = (Property) o;
            switch (p.getName()) {
                case "X-TEST-VALUE":
                    checkTestValue(e, c, p.getValue(), testName.getValue());
                    break;
                case "X-TEST-MIN-VERSION":
                    final int ver = Integer.parseInt(p.getValue());
                    if (android.os.Build.VERSION.SDK_INT < ver) {
                        //LogUtil.d(TAG," :    -> SKIPPED (MIN-VERSION < " + ver + ")");
                        return;
                    }
                    break;
            }
        }
    }

    private final class Options {//extends org.sufficientlysecure.ical.Settings {
        private final List<Integer> eventReminders = new ArrayList<Integer>();


        public List<Integer> getReminders(List<Integer> eventReminders) {
            //LogUtil.d(TAG,"rem : eventReminders :::::" + eventReminders); //exec
            if (eventReminders.size() > 0)
                return eventReminders;

            return eventReminders;
        }
    }
}


