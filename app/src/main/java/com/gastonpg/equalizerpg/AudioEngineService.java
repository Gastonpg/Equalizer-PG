package com.gastonpg.equalizerpg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.audiofx.DynamicsProcessing;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Global output-mix test using Android 9's DynamicsProcessing effect.
 * This is deliberately independent of the legacy Equalizer effect.
 */
public class AudioEngineService extends Service {
    public static final String ACTION_START = "com.gastonpg.equalizerpg.AUDIO_START";
    public static final String ACTION_STOP = "com.gastonpg.equalizerpg.AUDIO_STOP";
    public static final String ACTION_BAND = "com.gastonpg.equalizerpg.AUDIO_BAND";
    public static final String ACTION_PREAMP = "com.gastonpg.equalizerpg.AUDIO_PREAMP";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_LEVEL = "level";
    private static final String CHANNEL_ID = "equalizer_pg_audio";
    private static final int NOTIFICATION_ID = 41;
    private static final float[] FREQS = {31f, 63f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f};
    private final float[] gains = new float[10];
    private DynamicsProcessing processor;
    private int preampMb;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            releaseProcessor();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification("Iniciando motor DynamicsProcessing…"));
        try {
            if (processor == null) createProcessor();
            if (ACTION_BAND.equals(action) && intent != null) {
                int index = intent.getIntExtra(EXTRA_INDEX, -1);
                int millibels = intent.getIntExtra(EXTRA_LEVEL, 0);
                if (index >= 0 && index < gains.length) gains[index] = Math.max(-12f, Math.min(12f, millibels / 100f));
            } else if (ACTION_PREAMP.equals(action) && intent != null) {
                preampMb = intent.getIntExtra(EXTRA_LEVEL, 0);
            }
            applySettings();
            updateNotification("Motor DynamicsProcessing activo · salida global solicitada");
            Log.i("EqualizerPG", "DynamicsProcessing enabled on session 0");
        } catch (Throwable error) {
            String detail = error.getClass().getSimpleName() + (error.getMessage() == null ? "" : ": " + error.getMessage());
            updateNotification("No compatible: " + detail);
            Log.e("EqualizerPG", "No se pudo crear/aplicar DynamicsProcessing en salida global", error);
            releaseProcessor();
            stopSelf();
        }
        return START_STICKY;
    }

    private void createProcessor() {
        DynamicsProcessing.Config.Builder builder = new DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                2, false, 0, false, 0, true, FREQS.length, false);
        DynamicsProcessing.Eq eq = new DynamicsProcessing.Eq(true, true, FREQS.length);
        for (int i = 0; i < FREQS.length; i++) {
            eq.setBand(i, new DynamicsProcessing.EqBand(true, FREQS[i], gains[i]));
        }
        builder.setPostEqAllChannelsTo(eq);
        processor = new DynamicsProcessing(0, 0, builder.build());
        processor.setEnabled(true);
    }

    private void applySettings() {
        if (processor == null) return;
        DynamicsProcessing.Eq eq = new DynamicsProcessing.Eq(true, true, FREQS.length);
        for (int i = 0; i < FREQS.length; i++) {
            eq.setBand(i, new DynamicsProcessing.EqBand(true, FREQS[i], gains[i]));
        }
        // Android 9 exposes live stage setters on the effect itself; Config does
        // not expose getChannelCount() and DynamicsProcessing has no setConfig().
        processor.setPostEqAllChannelsTo(eq);
        processor.setInputGainAllChannelsTo(Math.max(-12f, Math.min(0f, preampMb / 100f)));
        processor.setEnabled(true);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Equalizer PG", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Estado del motor de audio");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String status) {
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return builder.setContentTitle("Equalizer PG").setContentText(status)
                .setSmallIcon(R.drawable.ic_notification_pg).setOngoing(true).build();
    }

    private void updateNotification(String status) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIFICATION_ID, buildNotification(status));
    }

    private void releaseProcessor() {
        if (processor != null) {
            try { processor.setEnabled(false); } catch (Throwable ignored) {}
            try { processor.release(); } catch (Throwable ignored) {}
            processor = null;
        }
    }

    @Override public void onDestroy() {
        releaseProcessor();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}