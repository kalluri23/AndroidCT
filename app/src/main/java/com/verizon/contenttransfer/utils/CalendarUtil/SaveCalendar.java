package com.verizon.contenttransfer.utils.CalendarUtil;

import android.content.ContentResolver;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.verizon.contenttransfer.utils.LogUtil;
import com.verizon.contenttransfer.utils.MessageUtil;
import com.verizon.contenttransfer.utils.Utils;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

//import android.provider.CalendarContract;

/**
 * Created by palanpr on 4/16/2016.
 */
public class SaveCalendar {

    private static final String TAG = "Calendar_SaveCalendar";

    private final static PropertyFactoryImpl mPropertyFactory = PropertyFactoryImpl.getInstance();
    private final static Set<TimeZone> mInsertedTimeZones = new HashSet<>();
    private static final List<String> STATUS_ENUM = Arrays.asList("TENTATIVE", "CONFIRMED", "CANCELLED");
    private static final List<String> CLASS_ENUM = Arrays.asList(null, "CONFIDENTIAL", "PRIVATE", "PUBLIC");
    private static final List<String> AVAIL_ENUM = Arrays.asList(null, "FREE", "BUSY-TENTATIVE");
    private static final String[] REMINDER_COLS = new String[]{
            CalendarContractWrapper.Reminders.MINUTES, CalendarContractWrapper.Reminders.METHOD
    };
    private static TimeZoneRegistry mTzRegistry;

    public static VEvent convertFromDb(ContentResolver contentResolver, Cursor cur, Calendar cal, DtStamp timestamp) {

        //LogUtil.d(TAG, "cursor: " + DatabaseUtils.dumpCurrentRowToString(cur));
        if (hasStringValue(cur, CalendarContractWrapper.Events.ORIGINAL_ID)) {
            // FIXME: Support these edited instances
            LogUtil.d(TAG, "Ignoring edited instance of a recurring event");
            return null;
        }

        String deletedEvent =  getString(cur, CalendarContractWrapper.Events.DELETED);
        //LogUtil.d(TAG, "Calendar deletedEvent =" +deletedEvent);
        if (null != deletedEvent && deletedEvent.equals("1")){
            return null;
        }

        PropertyList l = new PropertyList();
        l.add(timestamp);

        if (copyProperty(l, Property.UID, cur, CalendarContractWrapper.Events.UID_2445) == null) {
            MessageUtil.displayCursorProperties(cur, "show cal event pro :");
            String event_id =  getString(cur, CalendarContractWrapper.Events._ID);
            String event_dtstart =  getString(cur, CalendarContractWrapper.Events.DTSTART);
            String event_end =  getString(cur, CalendarContractWrapper.Events.DTEND);
            String event_title =  getString(cur, CalendarContractWrapper.Events.TITLE);

            // Generate a UID. Not ideal, since its not reproducible
            //String uidPid = UUID.randomUUID().toString().replace("-", "");
            String uidPid = Utils.createUniqueUID(cur, event_id, event_dtstart, event_end, event_title);
            l.add(new Uid(uidPid));
        }

        String summary = copyProperty(l, Property.SUMMARY, cur, CalendarContractWrapper.Events.TITLE);
        String description = copyProperty(l, Property.DESCRIPTION, cur, CalendarContractWrapper.Events.DESCRIPTION);

        String organizer = getString(cur, CalendarContractWrapper.Events.ORGANIZER);
        if (!TextUtils.isEmpty(organizer)) {
            // The check for mailto: here handles early versions of this code which
            // incorrectly left it in the organizer column.
            if (!organizer.startsWith("mailto:"))
                organizer = "mailto:" + organizer;
            try {
                l.add(new Organizer(organizer));
            } catch (URISyntaxException ignored) {
                // Log.e(TAG, "Failed to create mailTo for organizer " + organizer);
            }
        }
        copyProperty(l, Property.LOCATION, cur, CalendarContractWrapper.Events.EVENT_LOCATION);
        copyEnumProperty(l, Property.STATUS, cur, CalendarContractWrapper.Events.STATUS, STATUS_ENUM);

        boolean allDay = TextUtils.equals(getString(cur, CalendarContractWrapper.Events.ALL_DAY), "1");
        boolean isTransparent;
        DtEnd dtEnd = null;

        if (allDay) {
            // All day event
            isTransparent = true;
            Date start = getDateTime(cur, CalendarContractWrapper.Events.DTSTART, null, null);
            l.add(new DtStart(start));
            dtEnd = new DtEnd(utcDateFromMs(start.getTime() + DateUtils.DAY_IN_MILLIS));
            l.add(dtEnd);
        } else {
            // Regular or zero-time event. Start date must be a date-time
            Date startDate = getDateTime(cur, CalendarContractWrapper.Events.DTSTART, CalendarContractWrapper.Events.EVENT_TIMEZONE, cal);
            l.add(new DtStart(startDate));

            // Use duration if we have one, otherwise end date
            if (hasStringValue(cur, CalendarContractWrapper.Events.DURATION)) {
                isTransparent = getString(cur, CalendarContractWrapper.Events.DURATION).equals("PT0S");
                if (!isTransparent) {
                    copyProperty(l, Property.DURATION, cur, CalendarContractWrapper.Events.DURATION);
                }
            } else {
                String endTz = CalendarContractWrapper.Events.EVENT_END_TIMEZONE;
                if (endTz == null) {
                    endTz = CalendarContractWrapper.Events.EVENT_TIMEZONE;
                }
                Date end = getDateTime(cur, CalendarContractWrapper.Events.DTEND, endTz, cal);
                dtEnd = new DtEnd(end);
                isTransparent = startDate.getTime() == end.getTime();
                if (!isTransparent) {
                    l.add(dtEnd);
                }
            }
        }

        copyEnumProperty(l, Property.CLASS, cur, CalendarContractWrapper.Events.ACCESS_LEVEL, CLASS_ENUM);

        int availability = getInt(cur, CalendarContractWrapper.Events.AVAILABILITY);
        if (availability > CalendarContractWrapper.Events.AVAILABILITY_TENTATIVE)
            availability = -1;     // Unknown/Invalid

        if (isTransparent) {
            // This event is ordinarily transparent. If availability shows that its
            // not free, then mark it opaque.
            if (availability >= 0 && availability != CalendarContractWrapper.Events.AVAILABILITY_FREE)
                l.add(Transp.OPAQUE);

        } else if (availability > CalendarContractWrapper.Events.AVAILABILITY_BUSY) {
            // This event is ordinarily busy but differs, so output a FREEBUSY
            // period covering the time of the event
            FreeBusy fb = new FreeBusy();
            fb.getParameters().add(new FbType(AVAIL_ENUM.get(availability)));
            DateTime start = new DateTime(((DtStart) l.getProperty(Property.DTSTART)).getDate());

            if (dtEnd != null)
                fb.getPeriods().add(new Period(start, new DateTime(dtEnd.getDate())));
            else {
                Duration d = (Duration) l.getProperty(Property.DURATION);
                fb.getPeriods().add(new Period(start, d.getDuration()));
            }
            l.add(fb);
        }

        copyProperty(l, Property.RRULE, cur, CalendarContractWrapper.Events.RRULE);
        copyProperty(l, Property.RDATE, cur, CalendarContractWrapper.Events.RDATE);
        copyProperty(l, Property.EXRULE, cur, CalendarContractWrapper.Events.EXRULE);
        copyProperty(l, Property.EXDATE, cur, CalendarContractWrapper.Events.EXDATE);
        if (TextUtils.isEmpty(getString(cur, CalendarContractWrapper.Events.CUSTOM_APP_PACKAGE))) {
            // Only copy URL if there is no app i.e. we probably imported it.
            copyProperty(l, Property.URL, cur, CalendarContractWrapper.Events.CUSTOM_APP_URI);
        }

        VEvent e = new VEvent(l);
        if (getInt(cur, CalendarContractWrapper.Events.HAS_ALARM) == 1) {
            // Add alarms

            String s = summary == null ? (description == null ? "" : description) : summary;
            Description desc = new Description(s);

            //System.out.println("save calendar----------1-------------");
            long eventId = getLong(cur, CalendarContractWrapper.Events._ID);
            Cursor alarmCur = CalendarContractWrapper.Reminders.query(contentResolver, eventId, REMINDER_COLS);
            //System.out.println("save calendar----------2-------------");
            while (alarmCur.moveToNext()) {
                //System.out.println("save calendar----------3-------------");
                int mins = getInt(alarmCur, CalendarContractWrapper.Reminders.MINUTES);
                if (mins == -1)
                    mins = 60;     // FIXME: Get the real default

                // FIXME: We should support other types if possible
                int method = getInt(alarmCur, CalendarContractWrapper.Reminders.METHOD);
                //System.out.println("save calendar----------4-------------");
                if (method == CalendarContractWrapper.Reminders.METHOD_DEFAULT || method == CalendarContractWrapper.Reminders.METHOD_ALERT) {
                    //System.out.println("save calendar----------5-------------");
                    VAlarm alarm = new VAlarm(new Dur(0, 0, -mins, 0));
                    //System.out.println("save calendar----------6-------------");
                    alarm.getProperties().add(net.fortuna.ical4j.model.property.Action.DISPLAY);
                    //System.out.println("save calendar----------7-------------");
                    alarm.getProperties().add(desc);
                    //System.out.println("save calendar----------8-------------");
                    e.getAlarms().add(alarm);
                    //System.out.println("save calendar----------9-----99--------");
                }
            }
            alarmCur.close();
        }
        return e;
    }

    private static int getColumnIndex(Cursor cur, String dbName) {
        return dbName == null ? -1 : cur.getColumnIndex(dbName);
    }

    private static String getString(Cursor cur, String dbName) {
        int i = getColumnIndex(cur, dbName);
        return i == -1 ? null : cur.getString(i);
    }

    private static long getLong(Cursor cur, String dbName) {
        int i = getColumnIndex(cur, dbName);
        return i == -1 ? -1 : cur.getLong(i);
    }

    private static int getInt(Cursor cur, String dbName) {
        int i = getColumnIndex(cur, dbName);
        return i == -1 ? -1 : cur.getInt(i);
    }

    private static boolean hasStringValue(Cursor cur, String dbName) {
        int i = getColumnIndex(cur, dbName);
        return i != -1 && !TextUtils.isEmpty(cur.getString(i));
    }

    private static Date utcDateFromMs(long ms) {
        // This date will be UTC provided the default false value of the iCal4j property
        // "net.fortuna.ical4j.timezone.date.floating" has not been changed.
        return new Date(ms);
    }

    private static boolean isUtcTimeZone(final String tz) {
        if (TextUtils.isEmpty(tz))
            return true;
        final String utz = tz.toUpperCase(Locale.US);
        return utz.equals("UTC") || utz.equals("UTC-0") || utz.equals("UTC+0") || utz.endsWith("/UTC");
    }

    private static Date getDateTime(Cursor cur, String dbName, String dbTzName, Calendar cal) {
        int i = getColumnIndex(cur, dbName);
        if (i == -1 || cur.isNull(i))
            return null;

        if (cal == null) {
            return utcDateFromMs(cur.getLong(i));     // Ignore timezone for date-only dates
        }

        String tz = getString(cur, dbTzName);
        final boolean isUtc = isUtcTimeZone(tz);

        DateTime dt = new DateTime(isUtc);
        if (dt.isUtc() != isUtc)
            throw new RuntimeException("UTC mismatch after construction");
        dt.setTime(cur.getLong(i));
        if (dt.isUtc() != isUtc)
            throw new RuntimeException("UTC mismatch after setTime");

        if (!isUtc) {
            if (mTzRegistry == null)
                mTzRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
            TimeZone t = mTzRegistry.getTimeZone(tz);
            dt.setTimeZone(t);
            if (!mInsertedTimeZones.contains(t)) {
                cal.getComponents().add(t.getVTimeZone());
                mInsertedTimeZones.add(t);
            }
        }
        return dt;
    }

    private static String copyProperty(PropertyList l, String evName, Cursor cur, String dbName) {
        // None of the exceptions caught below should be able to be thrown AFAICS.
        try {
            String value = getString(cur, dbName);
            if (value != null) {
                Property p = mPropertyFactory.createProperty(evName);
                p.setValue(value);
                l.add(p);
                return value;
            }
        } catch (IOException | URISyntaxException | ParseException ignored) {
        }
        return null;
    }

    private static void copyEnumProperty(PropertyList l, String evName, Cursor cur, String dbName,
                                         List<String> vals) {
        // None of the exceptions caught below should be able to be thrown AFAICS.
        try {
            int i = getColumnIndex(cur, dbName);
            if (i != -1 && !cur.isNull(i)) {
                int value = (int) cur.getLong(i);
                if (value >= 0 && value < vals.size() && vals.get(value) != null) {
                    Property p = mPropertyFactory.createProperty(evName);
                    p.setValue(vals.get(value));
                    l.add(p);
                }
            }
        } catch (IOException | URISyntaxException | ParseException ignored) {
        }
    }

}
