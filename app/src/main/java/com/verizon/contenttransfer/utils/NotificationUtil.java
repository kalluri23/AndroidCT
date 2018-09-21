package com.verizon.contenttransfer.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.verizon.contenttransfer.R;
/**
 * Created by rahiahm on 5/26/2016.
 */
public class NotificationUtil extends Activity {

    private static NotificationManager notificationManager;
    private static NotificationCompat.Builder noti;

    public static void createNotification(int id, String text) {
        notificationManager = (NotificationManager) CTGlobal.getInstance().getContentTransferContext().getSystemService(NOTIFICATION_SERVICE);
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            noti = new Notification.Builder(ctx)
                    .setOngoing(true)
                    .setContentTitle("Content Transfer")
                    .setContentText(text)
                    .setProgress(100, 0, false)
                    .setSmallIcon(R.drawable.content_transfer);

            Notification notification = noti.build();
            notificationManager.notify(id, notification);

        }
*/
        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(CTGlobal.getInstance().getContentTransferContext(), 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

       noti = new NotificationCompat.Builder(CTGlobal.getInstance().getContentTransferContext())
                .setSmallIcon(R.mipmap.ct_noti_icon)
                .setContentTitle("Content Transfer")
                .setContentText(text)
                .setOngoing(true)
                .setProgress(100, 0, false)
                .setContentIntent(pendingIntent);

        notificationManager.notify(id, noti.build());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void updateProgress(int id, int num, String msg){

        noti.setProgress(100, num, false)
                .setContentText(msg );
        notificationManager.notify(id, noti.build());

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void complete(int id, String msg){

        noti.setContentText(msg)
                .setOngoing(false)
                .setProgress(0, 0, false)
                .setAutoCancel(true);

        notificationManager.notify(id, noti.build());
    }

}
