package com.example.kyungjunmin.tmon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import io.realm.Realm;

/**
 * Created by KyungJunMin on 2017. 8. 1..
 */

public class NotificationPlayer {
    private final static int NOTIFICATION_PLAYER_ID = 0x123;
    private AudioService mService;
    private NotificationManager mNotificationManager;
    private NotificationManagerBuilder mNotificationManagerBuilder;
    private boolean isForeground;



    public NotificationPlayer(AudioService service) {
        Log.i("NotificationPlayer", "NotificationPlayer()");
        mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        updateNotificationPlayer();
    }



    public void updateNotificationPlayer() {
        Log.i("NotificationPlayer", "updateNotificationPlayer()");

        cancel();
        mNotificationManagerBuilder = new NotificationManagerBuilder();
        mNotificationManagerBuilder.execute();
    }

    public void removeNotificationPlayer() {
        Log.i("NotificationPlayer", "removeNotificationPlayer()");
        cancel();
        mService.stopForeground(true);
        isForeground = false;
    }

    private void cancel() {
        Log.i("NotificationPlayer", "cancel()");

        if (mNotificationManagerBuilder != null) {
            mNotificationManagerBuilder.cancel(true);
            mNotificationManagerBuilder = null;
        }
    }

    private class NotificationManagerBuilder extends AsyncTask<Void, Void, Notification> {
        private RemoteViews mRemoteViews;
        private NotificationCompat.Builder mNotificationBuilder;
        private PendingIntent mMainPendingIntent;

        @Override
        protected void onPreExecute() {
            Log.i("NotificationPlayer", "onPreExecute()");

            super.onPreExecute();
            Intent mainActivity = new Intent(mService, MainActivity.class);
            mMainPendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, 0);
            mRemoteViews = createRemoteView(R.layout.player_notification);
            Intent actionClose = new Intent(CommandActions.CLOSE);
            PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);


            mNotificationBuilder = new NotificationCompat.Builder(mService);
            mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(false)
                    .setContentIntent(mMainPendingIntent)
                    .setAutoCancel(true)
                    .setContent(mRemoteViews)
                    .setDeleteIntent(close);


            Notification notification = mNotificationBuilder.build();
            notification.priority = Notification.PRIORITY_MAX;
            notification.contentIntent = mMainPendingIntent;

            if (!isForeground) {
                isForeground = true;
                // 서비스를 Foreground 상태로 만든다
                mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
            }
        }

        @Override
        protected Notification doInBackground(Void... params) {
            Log.i("NotificationPlayer", "doInBackground()");

            mNotificationBuilder.setContent(mRemoteViews);
            mNotificationBuilder.setContentIntent(mMainPendingIntent);
            mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = mNotificationBuilder.build();
            updateRemoteView(mRemoteViews, notification);
            return notification;
        }

        @Override
        protected void onPostExecute(Notification notification) {


            super.onPostExecute(notification);

            if (!mService.isPlaying()) {
                isForeground = false;
                mService.stopForeground(false);
            }


            try {
                mNotificationManager.notify(NOTIFICATION_PLAYER_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("NotificationPlayer", "onPostExecute()");
        }

        private RemoteViews createRemoteView(int layoutId) {
            Log.i("NotificationPlayer", "createRemoteView()");

            RemoteViews remoteView = new RemoteViews(mService.getPackageName(), layoutId);
            Intent actionTogglePlay = new Intent(CommandActions.TOGGLE_PLAY);
            Intent actionForward = new Intent(CommandActions.FORWARD);
            Intent actionRewind = new Intent(CommandActions.REWIND);

            PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
            PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
            PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, 0);

            remoteView.setOnClickPendingIntent(R.id.notification_play, togglePlay);
            remoteView.setOnClickPendingIntent(R.id.notification_forward, forward);
            remoteView.setOnClickPendingIntent(R.id.notification_rewind, rewind);
            return remoteView;
        }

        private void updateRemoteView(final RemoteViews remoteViews, final Notification notification) {
            Log.i("NotificationPlayer", "updateRemoteView()");



            if (mService.isPlaying()) {
                remoteViews.setImageViewResource(R.id.notification_play, R.mipmap.ic_pause);
                mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
                isForeground = true;
            } else {
                remoteViews.setImageViewResource(R.id.notification_play, R.mipmap.ic_play);
            }

            Realm realm;
            realm = Realm.getDefaultInstance();
            AudioItem musicItem = realm.where(AudioItem.class).equalTo("mId", mService.getmCurrentId()).findFirst();

            Log.i("NotificationPlayer", "updateRemoteView() 의 아이템의 타이틀 "+ musicItem.getmTitle());


            if (musicItem == null)
                return;
            String title = musicItem.getmTitle();
            String artist = musicItem.getmArtist();
            remoteViews.setTextViewText(R.id.txt_title, title);
            remoteViews.setTextViewText(R.id.artist_name, artist);
            final Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), musicItem.getmAlbumId());
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("NotificationPlayer", "updateRemoteView() run()");
                    Picasso.with(mService)
                            .load(albumArtUri)
                            .error(R.mipmap.ic_empty_albumart)
                            .into(remoteViews, R.id.notification_albumart, NOTIFICATION_PLAYER_ID, notification);
                }
            });
            realm.close();
        }
    }
}
