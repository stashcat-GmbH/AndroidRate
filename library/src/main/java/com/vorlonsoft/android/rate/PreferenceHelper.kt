/*
 * Copyright 2017 - 2018 Vorlonsoft LLC
 *
 * Licensed under The MIT License (MIT)
 */
package com.vorlonsoft.android.rate

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.vorlonsoft.android.rate.AppInformation.getLongVersionCode
import com.vorlonsoft.android.rate.Constants.Date.YEAR_IN_DAYS
import com.vorlonsoft.android.rate.Constants.Utils.EMPTY_STRING
import java.util.*
import java.util.regex.Pattern
import kotlin.time.Duration

/**
 *
 * PreferenceHelper Class - preference helper class of the AndroidRate library.
 *
 * @since    0.1.3
 * @version  2.0.0
 * @author   Dennis Wagner
 * @author   Alexander Savin
 * @author   Shintaro Katafuchi
 */
class PreferenceHelper @JvmOverloads constructor(context: Context, prefsProvider: PreferencesProvider = defaultPreferencesProvider) {

    private val prefs = prefsProvider.getSharedPreferences(context, PREF_FILE_NAME, Context.MODE_PRIVATE)

    interface PreferencesProvider {
        fun getSharedPreferences(context: Context, name: String, mode: Int): SharedPreferences
    }

    companion object {
        val defaultPreferencesProvider =
            object : PreferencesProvider {
                override fun getSharedPreferences(
                    context: Context,
                    name: String,
                    mode: Int
                ): SharedPreferences = context.getSharedPreferences(name, mode)
            }

        private const val PREF_FILE_NAME = "androidrate_pref_file"
        internal const val PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES =
            "androidrate_365_day_period_dialog_launch_times"

        /** The key prefix for each custom event, so that there is no clash with existing keys (PREF_KEY_INSTALL_DATE etc.)  */
        internal const val PREF_KEY_CUSTOM_EVENT_PREFIX = "androidrate_custom_event_prefix_"
        internal const val PREF_KEY_DIALOG_FIRST_LAUNCH_TIME = "androidrate_dialog_first_launch_time"
        internal const val PREF_KEY_INSTALL_DATE = "androidrate_install_date"
        internal const val PREF_KEY_AGREED_OR_DECLINED = "androidrate_is_agree_show_dialog"
        internal const val PREF_KEY_LAUNCH_TIMES = "androidrate_launch_times"
        internal const val PREF_KEY_LAST_TIME_SHOWN = "androidrate_remind_interval"
        internal const val PREF_KEY_REMIND_LAUNCHES_NUMBER = "androidrate_remind_launches_number"
        internal const val PREF_KEY_DELAY_DATE = "androidrate_delay_date"
        private const val PREF_KEY_VERSION_CODE = "androidrate_version_code"
        private const val PREF_KEY_VERSION_NAME = "androidrate_version_name"
        private const val NUMERIC_MASK = "(0|[1-9]\\d*)"
        internal const val DEFAULT_DIALOG_LAUNCH_TIMES = ":0y0-0:"
        private const val CURRENT_DAY_LAUNCHES_MASK = "-$NUMERIC_MASK:"

        internal const val UNSET_DELAY_DATE = -1L
    }

    @SuppressLint("ApplySharedPref")
    inline fun SharedPreferences.edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        val editor = edit()
        action(editor)
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    private fun setCurrentDayDialogLaunchTimes(
        dialogLaunchTimes: String?,
        currentYear: Byte,
        currentDay: Short,
        currentDayCount: Short
    ) {
        var putDialogLaunchTimes: String = if (dialogLaunchTimes != null) {
            Pattern.compile(":${currentDay}y$currentYear$CURRENT_DAY_LAUNCHES_MASK")
                .matcher(dialogLaunchTimes)
                .replaceAll(":" + currentDay + "y" + currentYear + "-" + currentDayCount + ":")
        } else {
            ":${currentDay}y${currentYear}-${currentDayCount}:"
        }

        // since 3rd year deletes data for the current day that older than 2 years
        if (currentYear > 1) {
            for (b in 0 until currentYear - 1) {
                putDialogLaunchTimes =
                    Pattern.compile(":${currentDay}y$b$CURRENT_DAY_LAUNCHES_MASK")
                        .matcher(putDialogLaunchTimes)
                        .replaceAll(":")
            }
        }

        prefs.edit {
            putString(PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES, putDialogLaunchTimes)
        }
    }

    /**
     *
     * Clears data in shared preferences.
     *
     * @param context context
     */
    fun clearSharedPreferences() = prefs.edit{ clear() }

    fun isFirstLaunch(): Boolean =
        prefs.getLong(PREF_KEY_INSTALL_DATE, 0L) == 0L

    fun setFirstLaunchSharedPreferences(context: Context) {
        prefs.edit {
            putString(
                PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES,
                DEFAULT_DIALOG_LAUNCH_TIMES
            )
            putLong(PREF_KEY_DIALOG_FIRST_LAUNCH_TIME, 0L)
            putLong(PREF_KEY_INSTALL_DATE, Date().time)
            putInt(PREF_KEY_LAUNCH_TIMES, 1)
            putLong(PREF_KEY_LAST_TIME_SHOWN, 0L)
            putInt(PREF_KEY_REMIND_LAUNCHES_NUMBER, 0)
            putLong(PREF_KEY_VERSION_CODE, getLongVersionCode(context))
            putString(PREF_KEY_VERSION_NAME, AppInformation.getVersionName(context))
        }

        if (!getAgreedOrDeclined()) { //if (get() == false) set(false); - NOT error!
            setAgreedOrDeclined(false)
        }
    }

    fun increment365DayPeriodDialogLaunchTimes() {
        var currentDay =
            ((Date().time - getDialogFirstLaunchTime()) / Time.DAY).toShort()
        val currentYear: Byte = (currentDay / YEAR_IN_DAYS).toByte()
        val currentDialogLaunchTimes = prefs
            .getString(PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES, DEFAULT_DIALOG_LAUNCH_TIMES)
        if (currentYear > 0) {
            currentDay = (currentDay % YEAR_IN_DAYS).toShort()
        }

        if (currentDialogLaunchTimes != null &&
            Pattern.matches(
                "(.*):${currentDay}y$currentYear$CURRENT_DAY_LAUNCHES_MASK",
                currentDialogLaunchTimes
            )
        ) {
            val length = currentDialogLaunchTimes.length.toShort()
            var currentDayCount: String = EMPTY_STRING + currentDialogLaunchTimes[length - 2]
            for (s in (length - 3).toShort() downTo 1) {
                currentDayCount = if (Character.isDigit(currentDialogLaunchTimes[s])) {
                    currentDialogLaunchTimes[s].toString() + currentDayCount
                } else {
                    break
                }
            }
            setCurrentDayDialogLaunchTimes(
                currentDialogLaunchTimes, currentYear,
                currentDay, (currentDayCount.toShort() + 1).toShort()
            )
        } else {
            setCurrentDayDialogLaunchTimes(
                currentDialogLaunchTimes, currentYear,
                currentDay, 1.toShort()
            )
        }
    }

    fun get365DayPeriodDialogLaunchTimes(): Short {
        var currentDay =
            ((Date().time - getDialogFirstLaunchTime()) / Time.DAY).toShort()
        val currentYear: Byte = (currentDay / YEAR_IN_DAYS).toByte()
        var dialogLaunchTimes = prefs
            .getString(PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES, DEFAULT_DIALOG_LAUNCH_TIMES)
            ?.let {
                Pattern.compile(":${NUMERIC_MASK}y${currentYear}-")
                    .matcher(it)
                    .replaceAll(":")
            } ?: DEFAULT_DIALOG_LAUNCH_TIMES

        if (currentYear > 0) {
            currentDay = (currentDay % YEAR_IN_DAYS).toShort()
            for (s in currentDay until YEAR_IN_DAYS) {
                dialogLaunchTimes = Pattern.compile(":${s}y${(currentYear - 1)}-")
                    .matcher(dialogLaunchTimes)
                    .replaceAll(":")
            }
        }
        dialogLaunchTimes =
            Pattern.compile(":${NUMERIC_MASK}y$NUMERIC_MASK$CURRENT_DAY_LAUNCHES_MASK")
                .matcher(dialogLaunchTimes)
                .replaceAll(":")

        if (dialogLaunchTimes.length > 2) {
            dialogLaunchTimes = dialogLaunchTimes.substring(1, dialogLaunchTimes.length - 1)
        }

        var dialogLaunchTimesCount: Short = 0
        val dialogLaunchTimesSplit = Pattern.compile(":").split(dialogLaunchTimes)
        for (aDialogLaunchTimesSplit in dialogLaunchTimesSplit) {
            dialogLaunchTimesCount =
                (dialogLaunchTimesCount + aDialogLaunchTimesSplit.toShort()).toShort()
        }

        return dialogLaunchTimesCount
    }

    fun setCustomEventCount(eventName: String, eventCount: Short) =
        prefs.edit { putInt(PREF_KEY_CUSTOM_EVENT_PREFIX + eventName, eventCount.toInt()) }

    fun getCustomEventCount(eventName: String): Short =
        prefs.getInt(PREF_KEY_CUSTOM_EVENT_PREFIX + eventName, 0).toShort()

    fun setDialogFirstLaunchTime() =
        prefs.edit { putLong(PREF_KEY_DIALOG_FIRST_LAUNCH_TIME, Date().time) }

    fun getDialogFirstLaunchTime(): Long =
        prefs.getLong(PREF_KEY_DIALOG_FIRST_LAUNCH_TIME, 0L)

    fun getInstallDate(): Long =
        prefs.getLong(PREF_KEY_INSTALL_DATE, 0L)

    /**
     *
     * Sets the Rate Dialog agreed or declined flag.
     *
     * If true, the user has either agreed or declined to rating the app.
     * Meaning the rating dialog shouldn't be shown again.
     *
     * @param agreedOrDeclined the Rate Dialog agree flag
     */
    fun setAgreedOrDeclined(agreedOrDeclined: Boolean) =
        prefs.edit { putBoolean(PREF_KEY_AGREED_OR_DECLINED, agreedOrDeclined) }

    fun getAgreedOrDeclined(): Boolean =
        prefs.getBoolean(PREF_KEY_AGREED_OR_DECLINED, false)

    /**
     *
     * Sets number of times the app has been launched.
     *
     * @param launchTimes number of launch times to set
     */
    fun setLaunchTimes(launchTimes: Short) =
        prefs.edit { putInt(PREF_KEY_LAUNCH_TIMES, launchTimes.toInt()) }

    fun getLaunchTimes(): Short =
        prefs.getInt(PREF_KEY_LAUNCH_TIMES, 0).toShort()

    fun setLastTimeShown() =
        prefs.edit{ putLong(PREF_KEY_LAST_TIME_SHOWN, Date().time) }

    fun getLastTimeShown(): Long =
        prefs.getLong(PREF_KEY_LAST_TIME_SHOWN, 0L)

    /**
     *
     * Sets to [.PREF_KEY_REMIND_LAUNCHES_NUMBER] the current number of app launches.
     *
     * The Library calls this method when the neutral button is clicked.
     *
     */
    fun setRemindLaunchesNumber() =
        prefs.edit {
            putInt(
                PREF_KEY_REMIND_LAUNCHES_NUMBER,
                getLaunchTimes().toInt()
            )
        }

    fun getRemindLaunchesNumber(): Short =
        prefs.getInt(PREF_KEY_REMIND_LAUNCHES_NUMBER, 0).toShort()

    /**
     *
     * Clears shared preferences that were set up by clicking the Remind Button.
     *
     */
    fun clearRemindButtonClick() =
        prefs.edit {
            putLong(PREF_KEY_LAST_TIME_SHOWN, 0L)
            putInt(PREF_KEY_REMIND_LAUNCHES_NUMBER, 0)
        }

    fun setVersionCode(context: Context) =
        prefs.edit { putLong(PREF_KEY_VERSION_CODE, getLongVersionCode(context)) }

    fun getVersionCode(): Long =
        prefs.getLong(PREF_KEY_VERSION_CODE, 0L)

    fun setVersionName(context: Context) =
        prefs.edit {
            putString(
                PREF_KEY_VERSION_NAME,
                AppInformation.getVersionName(context)
            )
        }

    fun getVersionName(): String? =
        prefs.getString(PREF_KEY_VERSION_NAME, EMPTY_STRING)

    fun dialogShown() {
        if (getDialogFirstLaunchTime() == 0L) {
            setDialogFirstLaunchTime()
        }
        increment365DayPeriodDialogLaunchTimes()
    }

    fun setReminderToShowAgain() {
        setLastTimeShown()
        setRemindLaunchesNumber()
    }

    fun getDelay(): Long =
        prefs.getLong(PREF_KEY_DELAY_DATE, UNSET_DELAY_DATE)

    fun setDelay(delay: Duration) =
        prefs.edit {
            putLong(PREF_KEY_DELAY_DATE, Date().time + delay.inWholeMilliseconds)
        }

    fun setDelayUntil(date: Date) =
        prefs.edit { putLong(PREF_KEY_DELAY_DATE, date.time) }

    fun addDelay(delay: Duration) {
        var currentDelay: Long =
            prefs.getLong(PREF_KEY_DELAY_DATE, UNSET_DELAY_DATE)
        if (currentDelay == UNSET_DELAY_DATE) {
            currentDelay = Date().time
        }
        prefs.edit {
            putLong(PREF_KEY_DELAY_DATE, currentDelay + delay.inWholeMilliseconds)
        }
    }
}