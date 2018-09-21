/**
 * Copyright (C) 2013  Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2010-2011  Lukas Aichbauer
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.verizon.contenttransfer.utils.CalendarUtil;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.verizon.contenttransfer.utils.CalendarUtil.CalendarContractWrapper.Calendars;
import com.verizon.contenttransfer.utils.CalendarUtil.CalendarContractWrapper.Events;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class AndroidCalendar {
    private static final String[] CAL_COLS = new String[]{
            Calendars._ID, Calendars.DELETED, Calendars.NAME, Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.ACCOUNT_NAME, Calendars.ACCOUNT_TYPE, Calendars.OWNER_ACCOUNT,
            Calendars.VISIBLE, Calendars.CALENDAR_TIME_ZONE};
    public long mId;
    public String mIdStr;
    public String mName;
    public String mDisplayName;
    public String mAccountName;
    public String mAccountType;
    public String mOwner;
    public boolean mIsActive;
    public String mTimezone;
    public int mNumEntries;

    // Load all available calendars.
    // If an empty list is returned the caller probably needs to enable calendar
    // read permissions in App Ops/XPrivacy etc.
    public static List<AndroidCalendar> loadAll(ContentResolver resolver) {

        if (missing(resolver, Calendars.CONTENT_URI) || missing(resolver, Events.CONTENT_URI))
            return new ArrayList<>();
        String selection = "( deleted != 1 )";
        Cursor cur = resolver.query(Calendars.CONTENT_URI, CAL_COLS, selection, null, null);
       //Cursor cur = resolver.query(Calendars.CONTENT_URI, CAL_COLS, null, null, null);
        List<AndroidCalendar> calendars = new ArrayList<>(cur.getCount());

        while (cur.moveToNext()) {
            long deletedCal = getLong(cur, Calendars.DELETED);
            if (deletedCal != 0)
                continue;
            AndroidCalendar calendar = new AndroidCalendar();
            calendar.mId = getLong(cur, Calendars._ID);
            calendar.mIdStr = getString(cur, Calendars._ID);
            calendar.mName = getString(cur, Calendars.NAME);
            calendar.mDisplayName = getString(cur, Calendars.CALENDAR_DISPLAY_NAME);
            calendar.mAccountName = getString(cur, Calendars.ACCOUNT_NAME);
            calendar.mAccountType = getString(cur, Calendars.ACCOUNT_TYPE);
            calendar.mOwner = getString(cur, Calendars.OWNER_ACCOUNT);
            calendar.mIsActive = getLong(cur, Calendars.VISIBLE) == 1;
            calendar.mTimezone = getString(cur, Calendars.CALENDAR_TIME_ZONE);

            final String[] cols = new String[]{Events._ID};
            //final String where = Events.CALENDAR_ID + "=?";
            final String where = Events.CALENDAR_ID + "=? AND ( deleted != 1 )";
            final String[] args = new String[]{calendar.mIdStr};
            Cursor eventsCur = resolver.query(Events.CONTENT_URI, cols, where, args, null);
            calendar.mNumEntries = eventsCur.getCount();
            eventsCur.close();
            calendars.add(calendar);
        }
        cur.close();

        return calendars;
    }

    private static long getLong(Cursor src, String columnName) {
        return src.getLong(src.getColumnIndex(columnName));
    }

    private static String getString(Cursor src, String columnName) {
        return src.getString(src.getColumnIndex(columnName));
    }

    private static boolean missing(ContentResolver resolver, Uri uri) {
        // Determine if a provider is missing
        ContentProviderClient provider = resolver.acquireContentProviderClient(uri);
        if (provider != null)
            provider.release();
        return provider == null;
    }

    @Override
    public String toString() {
        return mDisplayName + " (" + mIdStr + ")";
    }


}
