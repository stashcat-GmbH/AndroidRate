/*
 * Copyright 2017 - 2018 Vorlonsoft LLC
 *
 * Licensed under The MIT License (MIT)
 */
package com.vorlonsoft.android.rate.sample

import android.app.Application
import android.util.Log
import com.vorlonsoft.android.rate.*
import kotlin.time.Duration.Companion.days

class SampleApplication : Application() {
    companion object {
        private val TAG = SampleApplication::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        /* uncomment to test other locales - start */
        //if ((Build.VERSION.SDK_INT >= 17)&&(Build.VERSION.SDK_INT < 25)) {
        //    String mLang = "fr";    // change to your test language
        //    String mCountry = "FR"; // change to your test country
        //    Configuration mConfig;
        //    Locale mLocale;
        //
        //    if (mCountry.hashCode() == "".hashCode()) {
        //        mLocale = new Locale(mLang);
        //    } else {
        //        mLocale = new Locale(mLang, mCountry);
        //    }
        //    Locale.setDefault(mLocale);
        //
        //    mConfig = getBaseContext().getResources().getConfiguration();
        //    mConfig.setLocale(mLocale);
        //    mConfig.setLayoutDirection(mLocale);
        //
        //    Resources resources = getBaseContext().getResources();
        //    resources.updateConfiguration(mConfig, resources.getDisplayMetrics());
        //}
        /* uncomment to test other locales - end */

        /* comment if you don't want to test AppCompatDialogManager instead DefaultDialogManager */
        val appCompatDialogManagerFactory: DialogManager.Factory = AppCompatDialogManager.Factory()
        AppRate.with(applicationContext)
            // default is GOOGLEPLAY (Google Play), other options are AMAZON (Amazon Appstore), BAZAAR (Cafe Bazaar),
            //      CHINESESTORES (19 chinese app stores), MI (Mi Appstore (Xiaomi Market)), SAMSUNG (Samsung Galaxy Apps),
            //      SLIDEME (SlideME Marketplace), TENCENT (Tencent App Store), YANDEX (Yandex.Store),
            //      setStoreType(BLACKBERRY, long) (BlackBerry World, long - your application ID),
            //      setStoreType(APPLE, long) (Apple App Store, long - your application ID),
            //      setStoreType(String...) (Any other store/stores, String... - an URI or array of URIs to your app) and
            //      setStoreType(Intent...) (Any custom intent/intents, Intent... - an intent or array of intents) */
            .setStoreType(StoreType.GOOGLEPLAY)
            // default is 10 days, 10 days mean dialog is shown 10 days after installation (first app launch), 0 means install millisecond
            .setTimeToWaitAfterInstall(3.days)
            // default is 10, 3 means app is launched 3 or more times
            .setLaunchTimes(10.toByte())
            // default is 1 day, 1 means app is launched 1 or more time units after neutral button clicked
            .setRemindTimeToWait(2.days)
            // default is 0, 1 means app is launched 1 or more times after neutral button clicked
            .setRemindLaunchesNumber(1.toByte())
            // default is 1, 1 means each launch, 2 means every 2nd launch, 3 means every 3rd launch, etc
            .setSelectedAppLaunches(4.toByte())
            // default is true, true means to show the Neutral button ("Remind me later").
            .setShowLaterButton(true)
            // default is unlimited, 3 means 3 or less occurrences of the display of the Rate Dialog within a 365-day period
            .set365DayPeriodMaxNumberDialogLaunchTimes(3.toShort())
            // default is false, true means to re-enable the Rate Dialog if a new versio[n of app with different version code is installed
            .setVersionCodeCheck(true)
            // default is false, true means to re-enable the Rate Dialog if a new version of app with different version name is installed
            .setVersionNameCheck(true)
            // default is false, true is for development only, true ensures that the Rate Dialog will be shown each time the app is launched
            .setDebug(true)
            // default false.
            .setCancelable(false)
            .setOnClickButtonListener(object : OnClickButtonListener {
                override fun onClickButton(which: Byte) {
                    Log.d(TAG, "RateButton: $which")
                }
            })
            /* uncomment to test AppCompatDialogManager instead DefaultDialogManager */
            //.setDialogManagerFactory(appCompatDialogManagerFactory)
            /* comment to use library strings instead app strings - start */
            .setTitle(R.string.new_rate_dialog_title)
            /* comment to use library strings instead app strings - end */ /* uncomment to use app string instead library string */ //.setMessage(R.string.new_rate_dialog_message)
            .setTextLater(R.string.new_rate_dialog_later)
            /* comment to use library strings instead app strings - start */
            .setTextNever(R.string.new_rate_dialog_never)
            /* comment to use library strings instead app strings - end */
            .setTextRateNow(R.string.new_rate_dialog_ok)
            // Monitors the app launch times
            .monitor()
    }
}