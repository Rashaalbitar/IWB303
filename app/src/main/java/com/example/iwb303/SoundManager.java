package com.example.iwb303;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Class to manage sound effects in the application.
 * Requirements addressed:
 * 1. Use SoundPool or MediaPlayer.
 * 2. Place sound file in 'raw' folder.
 * 3. Prevent sound repetition or memory leaks.
 */
public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private int clickSoundId;
    private boolean loaded = false;
    private int lastStreamId = 0;

    private SoundManager(Context context) {
        // التحقق من إعدادات الصوت
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // 1. استخدام SoundPool (مفضل للمؤثرات القصيرة)
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
            if (status == 0) {
                loaded = true;
            }
        });

        // 2. تحميل ملف الصوت من مجلد raw
        // تم وضع الملف في res/raw/click.wav
        clickSoundId = soundPool.load(context.getApplicationContext(), R.raw.click, 1);
    }

    // 3. منع تسريب الذاكرة باستخدام Singleton وسياق التطبيق (ApplicationContext)
    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    public void playClick() {
        if (loaded) {
            // منع تكرار الصوت (إيقاف الصوت السابق إذا كان قيد التشغيل)
            if (lastStreamId != 0) {
                soundPool.stop(lastStreamId);
            }
            // تشغيل الصوت مرة واحدة (loop = 0)
            lastStreamId = soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    //  منع تسريب الذاكرة
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            instance = null;
        }
    }
}
