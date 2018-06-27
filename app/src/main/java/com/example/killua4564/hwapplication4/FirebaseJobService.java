package com.example.killua4564.hwapplication4;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;

public class FirebaseJobService extends JobService {

    private String id;
    private String classname;
    private AsyncTask asyncTask;
    private static ArrayList<Item> items;
    private static final int NOTIFICATION_ID = 1138;
    private static final int PENDING_INTENT_ID = 3417;
    private static final String NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        this.id = jobParameters.getExtras().getString("id");
        this.classname = jobParameters.getExtras().getString("classname");
        this.asyncTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                for (Item item : items) {
                    if (item.getId().equals(id)) {
                        if (item.getKey()) {
                            NotificationManager notificationManager = (NotificationManager) FirebaseJobService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(NOTIFICATION_ID,
                                    new NotificationCompat.Builder(FirebaseJobService.this, NOTIFICATION_CHANNEL_ID)
                                            .setColor(ContextCompat.getColor(FirebaseJobService.this, R.color.colorPrimary))
                                            .setSmallIcon(R.drawable.ic_launcher_background)
                                            .setContentTitle(FirebaseJobService.this.getString(R.string.notification_title))
                                            .setContentText(classname)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setDefaults(Notification.DEFAULT_VIBRATE)
                                            .setContentIntent(PendingIntent.getActivity(FirebaseJobService.this, PENDING_INTENT_ID, new Intent(FirebaseJobService.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                                            .setAutoCancel(true)
                                            .build()
                            );
                        }
                        break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object object) {
                jobFinished(jobParameters, false);

            }
        };

        this.asyncTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (this.asyncTask != null) this.asyncTask.cancel(true);
        return true;
    }

    public static void updateItem(ArrayList<Item> itemArray) {
        items = itemArray;
    }
}