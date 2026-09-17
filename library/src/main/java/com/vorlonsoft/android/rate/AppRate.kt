/*
 * Copyright 2017 - 2018 Vorlonsoft LLC
 *
 * Licensed under The MIT License (MIT)
 */
package com.vorlonsoft.android.rate

import android.annotation.SuppressLint
import android.content.Context
import com.vorlonsoft.android.rate.AppInformation.getLongVersionCode
import com.vorlonsoft.android.rate.AppInformation.getVersionName
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 *
 * AppRate Class - main class of the AndroidRate library, an thread-safe and fast singleton
 * implementation.
 * Improved and converted to kotlin on 13.02.2023.
 *
 * @since   0.0.4
 * @version 2.0.0
 * @author  Dennis Wagner
 * @author  Alexander Savin
 * @author  Shintaro Katafuchi
 */
class AppRate private constructor(context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AppRate? = null

        @JvmStatic
        fun with(context: Context): AppRate = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AppRate(context).also { INSTANCE = it }
        }

        fun areAppRatingConditionsMet(context: Context): Boolean =
            with(context).let {
                it.isDebug || it.shouldShowRateDialog()
            }
    }

    private var customEventsCounts: MutableMap<String, Short> = mutableMapOf()

    /**
     * The context of the single, global Application object of the current process.
     * */
    private val context: Context = context.applicationContext

    private val prefsHelper = PreferenceHelper(context)

    /**
     *
     * Checks if the library is in Debug mode. **For development only!**
     *
     * @return true if the library is in Debug mode, false otherwise
     */
    var isDebug = false
        private set

    private var isVersionCodeCheck = false
    private var isVersionNameCheck = false
    private var installWaitDuration = 10.days
    private var appLaunchTimes = 10.toByte()
    private var remindInterval = 1.days
    private var remindLaunchesNumber = 0.toByte()
    private var selectedAppLaunches = 1.toByte()

    /** Short.MAX_VALUE means unlimited occurrences of the display of the dialog within a 365-day period  */
    private var dialogLaunchTimes = Short.MAX_VALUE

    /**
     *
     * Sets the max number of occurrences of the display of the Rate Dialog within a 365-day
     * period.
     *
     * @param dialogLaunchTimes the max number of the display of the Rate Dialog within a 365-day
     * period, default is `Short.MAX_VALUE`, `Short.MAX_VALUE`
     * means unlimited occurrences
     * @return the [AppRate] singleton object
     */
    fun set365DayPeriodMaxNumberDialogLaunchTimes(dialogLaunchTimes: Short): AppRate = apply {
        this.dialogLaunchTimes = dialogLaunchTimes
    }

    /**
     *
     * Sets the minimum number of launches until the Rate Dialog pops up for
     * the first time.
     *
     * @param appLaunchTimes number of launches, default is 10, 3 means app is launched 3 or
     * more times
     * @return the [AppRate] singleton object
     */
    fun setLaunchTimes(appLaunchTimes: Byte): AppRate = apply {
        this.appLaunchTimes = appLaunchTimes
    }

    /**
     *
     * Sets the minimal duration until the Rate Dialog pops up for the first time.
     *
     * Default is 10 days, 0 means install millisecond.
     *
     * @param waitDuration Duration to wait after install
     * @return the [AppRate] singleton object
     */
    fun setTimeToWaitAfterInstall(waitDuration: Duration): AppRate = apply {
        installWaitDuration = waitDuration
    }

    /**
     *
     * Sets the minimal duration until a rate dialog should be shown again.
     * Default is 1 [day][days].
     *
     * @param remindDuration Duration to wait until showing rate dialog again
     * @return the [AppRate] singleton object
     * @see .setLastTimeShown
     * @see kotlin.time.Duration
     */
    fun setRemindTimeToWait(remindDuration: Duration): AppRate = apply {
        remindInterval = remindDuration
    }

    /**
     *
     * Sets the minimal number of app's launches after neutral button clicked until the Rating
     * Dialog pops up next time.
     *
     * @param remindLaunchesNumber number of app launches, default is 0, 1 means app is launched 1
     * or more times after neutral button clicked
     * @return the [AppRate] singleton object
     */
    fun setRemindLaunchesNumber(remindLaunchesNumber: Byte): AppRate = apply {
        this.remindLaunchesNumber = remindLaunchesNumber
    }

    /**
     *
     * Clears shared preferences that were set up by clicking the Remind Button.
     *
     * @return the [AppRate] singleton object
     */
    fun clearRemindButtonClick(): AppRate = apply {
        prefsHelper.clearRemindButtonClick()
    }

    fun setMinimumEventCount(eventName: String, minimumCount: Short): AppRate = apply {
        customEventsCounts[eventName] = minimumCount
    }

    /**
     *
     * Selects App launches.
     *
     * Method sets divisor for division of app launches with a remainder. This condition is
     * satisfied if `appLaunches % divisorAppLaunches == 0`
     *
     * @param selectedAppLaunches default is 1, 1 means each launch, 2 means every 2nd launch,
     * 3 means every 3rd launch, etc
     * @return the [AppRate] singleton object
     * @since 1.2.0
     */
    fun setSelectedAppLaunches(selectedAppLaunches: Byte): AppRate = apply {
        this.selectedAppLaunches = selectedAppLaunches
    }

    fun clearSettingsParam(): AppRate = apply {
        prefsHelper.clearSharedPreferences()
    }

    /**
     *
     * Clears agree to show Rate Dialog flag.
     *
     * @return the [AppRate] singleton object
     */
    fun clearAgreeShowDialog(): AppRate = apply {
        setAgreedOrDeclinedDialog(false)
    }

    /**
     * @return true if the rating dialog has been agreed to or declined.
     */
    fun getAgreedOrDeclinedDialog(): Boolean = prefsHelper.getAgreedOrDeclined()

    /**
     *
     * Sets if the user has agreed or declined the dialog.
     * If false, the user hasn't done either.
     *
     * @param agreedOrDeclined agree to show Rate Dialog flag
     * @return the [AppRate] singleton object
     */
    fun setAgreedOrDeclinedDialog(agreedOrDeclined: Boolean): AppRate = apply {
        prefsHelper.setAgreedOrDeclined(agreedOrDeclined)
    }

    fun incrementEventCount(eventName: String): AppRate {
        return setEventCountValue(
            eventName,
            (prefsHelper.getCustomEventCount(eventName) + 1).toShort()
        )
    }

    fun setEventCountValue(eventName: String, countValue: Short): AppRate = apply {
        prefsHelper.setCustomEventCount(eventName, countValue)
    }

    /**
     *
     * Sets the check whether the version code of the app is changed.
     *
     * @param isVersionCodeCheck true means to re-enable the Rate Dialog if a new version of app
     * with different version code is installed, default is false
     * @return the [AppRate] singleton object
     */
    fun setVersionCodeCheck(isVersionCodeCheck: Boolean): AppRate = apply {
        this.isVersionCodeCheck = isVersionCodeCheck
    }

    /**
     *
     * Sets the check whether the version name of the app is changed.
     *
     * @param isVersionNameCheck true means to re-enable the Rate Dialog if a new version of app
     * with different version name is installed, default is false
     * @return the [AppRate] singleton object
     */
    fun setVersionNameCheck(isVersionNameCheck: Boolean): AppRate = apply {
        this.isVersionNameCheck = isVersionNameCheck
    }

    /**
     * Sets a delay, which will block rating dialogs completely, until the delay is over. The delay
     * is tracked from the moment this function is called (Date().time + [delay]).
     *
     * @param delay Duration by which to delay
     * @return the [AppRate] singleton object
     */
    fun setDelay(delay: Duration): AppRate = apply {
        prefsHelper.setDelay(delay)
    }

    /**
     * Adds a delay duration to the current (already existing) delay. If no delay was previously set,
     * the delay is calculated from the current time (Date().time + [delay]).
     *
     * @param delay Duration by which to delay
     * @return the [AppRate] singleton object
     */
    fun addDelay(delay: Duration): AppRate = apply {
        prefsHelper.addDelay(delay)
    }

    /**
     * Sets a delay date, rating dialogs will be blocked completely until the delay date is over.
     *
     * @param delayDate Until which to delay rating dialogs
     * @return the [AppRate] singleton object
     */
    fun setDelayUntil(delayDate: Date): AppRate = apply {
        prefsHelper.setDelayUntil(delayDate)
    }

    /**
     * Saves that the user wants to be reminded to rate the app. User will be remined according to
     * the [remindInterval] and [remindLaunchesNumber] properties.
     */
    fun remindLater(): AppRate = apply {
        prefsHelper.setReminderToShowAgain()
    }

    /**
     * Record the action of showing an app rating dialog. Will increment a count for future
     * checks regarding the max number of times the dialog is to be displayed per year.
     */
    fun recordDialogShown(): AppRate = apply {
        prefsHelper.dialogShown()
    }

    /**
     *
     * Monitors launches of the application.
     *
     * Call this method when the `onCreate()` of the app's launcher activity or Application class
     * is called.
     */
    fun monitor() {
        if (prefsHelper.isFirstLaunch()) {
            prefsHelper.setFirstLaunchSharedPreferences(context)
        } else {
            prefsHelper.setLaunchTimes(
                (prefsHelper.getLaunchTimes() + 1).toShort()
            )
            if (getLongVersionCode(context) != prefsHelper.getVersionCode()) {
                if (isVersionCodeCheck) {
                    setAgreedOrDeclinedDialog(false)
                }
                prefsHelper.setVersionCode(context)
            }
            if (getVersionName(context) != prefsHelper.getVersionName()) {
                if (isVersionNameCheck) {
                    setAgreedOrDeclinedDialog(false)
                }
                prefsHelper.setVersionName(context)
            }
        }
    }

    /**
     *
     * Determines whether conditions to show the Rate Dialog meets or not.
     *
     * @return true if the conditions to show the Rate Dialog meets, false otherwise
     */
    fun shouldShowRateDialog(): Boolean {
        return !getAgreedOrDeclinedDialog() &&
                isOverLaunchTimes() &&
                isSelectedAppLaunch() &&
                isOverInstallDate() &&
                isOverRemindDate() &&
                isOverRemindLaunchesNumber() &&
                isOverCustomEventsRequirements() &&
                isBelow365DayPeriodMaxNumberDialogLaunchTimes() &&
                isOverDelay()
    }

    private fun isOverDate(targetDate: Long, threshold: Duration): Boolean {
        return Date().time - targetDate >= threshold.inWholeMilliseconds
    }

    private fun isOverLaunchTimes(): Boolean =
        appLaunchTimes.toInt() == 0 || prefsHelper.getLaunchTimes() >= appLaunchTimes

    private fun isSelectedAppLaunch(): Boolean = selectedAppLaunches.toInt() == 1 ||
            selectedAppLaunches.toInt() != 0 && prefsHelper.getLaunchTimes() % selectedAppLaunches == 0

    private fun isOverInstallDate(): Boolean =
        installWaitDuration.inWholeMilliseconds == 0L || isOverDate(
            prefsHelper.getInstallDate(),
            installWaitDuration
        )

    private fun isOverRemindDate(): Boolean = remindInterval.inWholeMilliseconds == 0L ||
            prefsHelper.getLastTimeShown() == 0L ||
            isOverDate(prefsHelper.getLastTimeShown(), remindInterval)

    private fun isOverRemindLaunchesNumber(): Boolean = remindLaunchesNumber.toInt() == 0 ||
            prefsHelper.getRemindLaunchesNumber().toInt() == 0 ||
            prefsHelper.getLaunchTimes() - prefsHelper.getRemindLaunchesNumber() >= remindLaunchesNumber

    private fun isBelow365DayPeriodMaxNumberDialogLaunchTimes(): Boolean =
        dialogLaunchTimes == Short.MAX_VALUE ||
                prefsHelper.get365DayPeriodDialogLaunchTimes() < dialogLaunchTimes

    private fun isOverCustomEventsRequirements(): Boolean {
        if (customEventsCounts.isEmpty()) {
            return true
        }
        // checking if at least one custom event count is below the expected value
        val unmetCustomEvents = customEventsCounts.entries.firstOrNull {
            prefsHelper.getCustomEventCount(
                it.key
            ) < it.value
        }
        // if null, all custom events have counts bigger than or are equal to the expected values
        return unmetCustomEvents == null
    }

    private fun isOverDelay() = Date().time > prefsHelper.getDelay()

    /**
     *
     * Debug mode. **For development only!**
     *
     * Setting the library to Debug mode ensures that the Rate Dialog will be shown each time
     * the app is launched.
     *
     * @param isDebug default is false, true ensures that the Rate Dialog will be shown each time
     * the app is launched
     * @return the [AppRate] singleton object
     */
    fun setDebug(isDebug: Boolean): AppRate = apply {
        this.isDebug = isDebug
    }
}