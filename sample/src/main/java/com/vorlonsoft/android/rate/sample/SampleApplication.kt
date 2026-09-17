/*
 * Copyright 2017 - 2018 Vorlonsoft LLC
 *
 * Licensed under The MIT License (MIT)
 */
package com.vorlonsoft.android.rate.sample

import android.app.Application
import com.vorlonsoft.android.rate.*
import kotlin.time.Duration.Companion.days

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        AppRate.with(applicationContext)
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
            // default is unlimited, 3 means 3 or less occurrences of the display of the Rate Dialog within a 365-day period
            .set365DayPeriodMaxNumberDialogLaunchTimes(3.toShort())
            // default is false, true means to re-enable the Rate Dialog if a new versio[n of app with different version code is installed
            .setVersionCodeCheck(true)
            // default is false, true means to re-enable the Rate Dialog if a new version of app with different version name is installed
            .setVersionNameCheck(true)
            // default is false, true is for development only, true ensures that the Rate Dialog will be shown each time the app is launched
            .setDebug(true)
            // Monitors the app launch times
            .monitor()
    }
}