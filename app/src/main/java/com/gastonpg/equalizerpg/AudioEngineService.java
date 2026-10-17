package com.gastonpg.equalizerpg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.IBinder;

/**
 * First real audio-engine integration test.
 * Uses Android's output-mix Equalizer (session 0) when the device permits it.
 * This validates system audio access; a custom parametric DSP path is a later stage.
 */
public class AudioEngineService extends Service {
    public static final String ACTION_START = "com.gastonpg.equalizerpg.AUDIO_START";
    public static final String ACTION_STOP = "com.gastonpg.equalizerpg.AUDIO_STOP";
    public static final String ACTION_BAND = "com.gastonpg.equalizerpg.AUDIO_BAND";
    public static final String ACTION_PREAMP = "com.gastonpg.equalizerpg.AUDIO_PREAMP";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_LEVEL = "level";
    private static final String CHANNEL_ID = "equalizer_pg_audio";
    private Equalizer equalizer;
    private short[] bandLevels;
    private int preampMb;

    @Override
    public void onCreate() {
        super.onCreate();
        bandLevels = new short[]{-200, 100, 100, -100, 0, 0, 100, 100, 100, 0};
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            releaseEqualizer();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(41, buildNotification("Activando ecualizador…"));
        try {
            if (equalizer == null) {
                // Session 0 requests an output-mix effect. Some devices may reject it.
                equalizer = new Equalizer(0, 0);
                equalizer.setEnabled(true);
            }
            if (ACTION_BAND.equals(action) && intent != null) {
                int index = intent.getIntExtra(EXTRA_INDEX, -1);
                int level = intent.getIntExtra(EXTRA_LEVEL, 0);
                if (index >= 0 && index < 10) {
                    bandLevels[index] = (short) Math.max(-1200, Math.min(1200, level));
                }
            } else if (ACTION_PREAMP.equals(action) && intent != null) {
                preampMb = intent.getIntExtra(EXTRA_LEVEL, 0);
            }
            applyBands();
            updateNotification("Activo · ecualizador del sistema");
        } catch (Throwable error) {
            updateNotification("Este dispositivo no permite ecualización global");
            android.util.Log.e("EqualizerPG", "No se pudo iniciar el efecto de salida", error);
            releaseEqualizer();
            stopSelf();
        }
        return START_STICKY;
    }

    private void applyBands() {
        if (equalizer == null) return;
        short count = equalizer.getNumberOfBands();
        if (count <= 0) return;
        int[] centers = {31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000};
        short[] rangeDb = equalizer.getBandLevelRange();
        for (short b = 0; b < count; b++) {
            int[] range = equalizer.getBandFreqRange(b);
            int nativeCenterHz = (range[0] + range[1]) / 2000;
            int nearestIndex = 0;
            double best = Double.MAX_VALUE;
            for (int i = 0; i < centers.length; i++) {
                double distance = Math.abs(Math.log(Math.max(1, nativeCenterHz) / (double) centers[i]));
                if (distance < best) {
                    best = distance;
                    nearestIndex = i;
                }
            }
            int level = Math.max(rangeDb[0], Math.min(rangeDb[1], bandLevels[nearestIndex]));
            equalizer.setBandLevel(b, (short) level);
        }
        // Android's Equalizer effect has no standard preamp control; retained for DSP integration.
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Equalizer PG", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Estado del procesamiento de audio");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String status) {
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return builder.setContentTitle("Equalizer PG")
                .setContentText(status)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String status) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(41, buildNotification(status));
    }

    private void releaseEqualizer() {
        if (equalizer != null) {
            try { equalizer.setEnabled(false); } catch (Throwable ignored) {}
            try { equalizer.release(); } catch (Throwable ignored) {}
            equalizer = null;
        }
    }

    @Override public void onDestroy() {
        releaseEqualizer();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
