package com.urbandroid.sleep.captcha;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.urbandroid.sleep.captcha.annotation.CaptchaDifficulty;
import com.urbandroid.sleep.captcha.annotation.CaptchaEvent;
import com.urbandroid.sleep.captcha.annotation.CaptchaMode;
import com.urbandroid.sleep.captcha.annotation.SleepOperation;
import com.urbandroid.sleep.captcha.finder.BaseCaptchaFinder;
import com.urbandroid.sleep.captcha.finder.CaptchaFinder;
import com.urbandroid.sleep.captcha.intent.IntentExtraSetter;
import com.urbandroid.sleep.captcha.launcher.BaseCaptchaLauncher;
import com.urbandroid.sleep.captcha.launcher.CaptchaLauncher;

import static com.urbandroid.sleep.captcha.CaptchaConstant.CAPTCHA_ACTION_CONFIG;
import static com.urbandroid.sleep.captcha.CaptchaConstant.CAPTCHA_CONFIG_DIFFICULTY;
import static com.urbandroid.sleep.captcha.CaptchaConstant.PREVIEW;

public class BaseCaptchaSupport implements CaptchaSupport {

    private final Context context;
    private final Intent intent;
    private final CaptchaFinder finder;
    private final CaptchaLauncher launcher;

    protected BaseCaptchaSupport(final @NonNull Context context, final @NonNull Intent intent) {
        this.context = context;
        this.intent = intent;
        this.finder = new BaseCaptchaFinder(context);
        this.launcher = new BaseCaptchaLauncher(context);
    }

    @Override
    public boolean isPreviewMode(){
        return intent.getBooleanExtra(PREVIEW, false);
    }

    @Override
    public boolean isConfigurationMode(){
        return CAPTCHA_ACTION_CONFIG.equals(intent.getAction());
    }

    @Override
    public boolean isOperationalMode() {
        return !isPreviewMode() && !isConfigurationMode();
    }

    @Override
    @CaptchaMode
    public int getMode() {
        if (isPreviewMode()) {
            return CaptchaMode.CAPTCHA_MODE_PREVIEW;
        }
        if (isConfigurationMode()) {
            return CaptchaMode.CAPTCHA_MODE_CONFIGURATION;
        }
        return CaptchaMode.CAPTCHA_MODE_OPERATIONAL;
    }

    @Override
    @SuppressWarnings("ResourceType")
    @CaptchaDifficulty
    public int getDifficulty(){
        return intent.getIntExtra(CAPTCHA_CONFIG_DIFFICULTY, CaptchaDifficulty.VERY_SIMPLE);
    }

    protected boolean hasOperation(){
        return !intent.hasExtra(SleepOperation.OPERATION_NONE);
    }

    @Override
    public void alive() {
        send(CaptchaEvent.CAPTCHA_BACK_INTENT_ALIVE, null);
    }

    @Override
    public void solved(){
        solved(null);
    }

    @Override
    public void solved(final @Nullable IntentExtraSetter extraSetter){
        send(CaptchaEvent.CAPTCHA_BACK_INTENT_SOLVED, extraSetter);
    }

    @Override
    public void unsolved() {
        send(CaptchaEvent.CAPTCHA_BACK_INTENT_UNSOLVED, null);
    }

    void send(final @CaptchaEvent String event, final @Nullable IntentExtraSetter extraSetter){
        if (!isOperationalMode()) {
            return;
        }

        final Intent callbackIntent = intent.getParcelableExtra(event);
        Log.i(TAG, "calling " + event + ": " + callbackIntent);
        if (callbackIntent == null) {
            return;
        }
        if (extraSetter != null) {
            extraSetter.setExtras(callbackIntent);
        }

        switch (event) {
            case CaptchaEvent.CAPTCHA_BACK_INTENT_ALIVE:
                context.sendBroadcast(callbackIntent);
                break;
            case CaptchaEvent.CAPTCHA_BACK_INTENT_UNSOLVED:
                if (!hasOperation()) {
                    context.startActivity(callbackIntent);
                }
                break;
            case CaptchaEvent.CAPTCHA_BACK_INTENT_SOLVED:
                if (hasOperation()) {
                    context.sendBroadcast(callbackIntent);
                } else {
                    context.startActivity(callbackIntent);
                }
                break;
        }
    }

    @Override
    public CaptchaFinder getFinder() {
        return finder;
    }
    @Override
    public CaptchaLauncher getLauncher() {
        return launcher;
    }


}
