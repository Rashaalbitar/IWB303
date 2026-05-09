package com.example.iwb303;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * كلاس لإدارة المؤثرات الصوتية في التطبيق
 * تم استخدام SoundPool لتشغيل الأصوات القصيرة عند النقر
 */
public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private int clickSoundId;
    private boolean loaded = false;
    private int lastStreamId = 0;

    private SoundManager(Context context) {
        // إعداد خصائص الصوت
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // بناء SoundPool
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // فحص اكتمال تحميل الصوت
        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
            if (status == 0) loaded = true;
        });

        // تحميل ملف الصوت من مجلد raw
        clickSoundId = soundPool.load(context.getApplicationContext(), R.raw.click, 1);
    }

    // Singleton لضمان وجود نسخة واحدة فقط من مدير الأصوات وتجنب استهلاك الذاكرة
    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    // تشغيل صوت النقرة
    public void playClick() {
        if (loaded && soundPool != null) {
            // إيقاف الصوت السابق قبل تشغيل الجديد لمنع التكرار المزعج
            if (lastStreamId != 0) {
                soundPool.stop(lastStreamId);
            }
            lastStreamId = soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    // تحرير الموارد عند إغلاق التطبيق لمنع تسريب الذاكرة
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            instance = null;
        }
    }
}
