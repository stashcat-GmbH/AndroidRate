/*
 * Copyright 2017 - 2018 Vorlonsoft LLC
 *
 * Licensed under The MIT License (MIT)
 */
package com.vorlonsoft.android.rate

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
internal object PreferenceHelper {

    private const val PREF_FILE_NAME = "androidrate_pref_file"
    private const val PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES =
        "androidrate_365_day_period_dialog_launch_times"

    /** The key prefix for each custom event, so that there is no clash with existing keys (PREF_KEY_INSTALL_DATE etc.)  */
    private const val PREF_KEY_CUSTOM_EVENT_PREFIX = "androidrate_custom_event_prefix_"
    private const val PREF_KEY_DIALOG_FIRST_LAUNCH_TIME = "androidrate_dialog_first_launch_time"
    private const val PREF_KEY_INSTALL_DATE = "androidrate_install_date"
    private const val PREF_KEY_AGREED_OR_DECLINED = "androidrate_is_agree_show_dialog"
    private const val PREF_KEY_LAUNCH_TIMES = "androidrate_launch_times"
    private const val PREF_KEY_LAST_TIME_SHOWN = "androidrate_remind_interval"
    private const val PREF_KEY_REMIND_LAUNCHES_NUMBER = "androidrate_remind_launches_number"
    private const val PREF_KEY_DELAY_DATE = "androidrate_delay_date"
    private const val PREF_KEY_VERSION_CODE = "androidrate_version_code"
    private const val PREF_KEY_VERSION_NAME = "androidrate_version_name"
    private const val NUMERIC_MASK = "(0|[1-9]\\d*)"
    private const val DEFAULT_DIALOG_LAUNCH_TIMES = ":0y0-0:"
    private const val CURRENT_DAY_LAUNCHES_MASK = "-$NUMERIC_MASK:"

    private const val UNSET_DELAY_DATE = -1L

    private fun Context.getPreferences() =
        getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    private fun Context.editPrefs(
        commit: Boolean = false,
        editAction: SharedPreferences.Editor.() -> Unit
    ) = getPreferences().edit(commit) { editAction() }

    private fun setCurrentDayDialogLaunchTimes(
        context: Context,
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
        context.editPrefs {
            putString(PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES, putDialogLaunchTimes)
        }
    }

    /**
     *
     * Clears data in shared preferences.
     *
     * @param context context
     */
    @JvmStatic
    fun clearSharedPreferences(context: Context) = context.editPrefs { clear() }

    @JvmStatic
    fun isFirstLaunch(context: Context): Boolean =
        context.getPreferences().getLong(PREF_KEY_INSTALL_DATE, 0L) == 0L

    @JvmStatic
    fun setFirstLaunchSharedPreferences(context: Context) {
        context.editPrefs {
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

        if (!getAgreedOrDeclined(context)) { //if (get() == false) set(false); - NOT error!
            setAgreedOrDeclined(context, false)
        }
    }

    @JvmStatic
    fun increment365DayPeriodDialogLaunchTimes(context: Context) {
        var currentDay =
            ((Date().time - getDialogFirstLaunchTime(context)) / Time.DAY).toShort()
        val currentYear: Byte = (currentDay / YEAR_IN_DAYS).toByte()
        val currentDialogLaunchTimes = context.getPreferences()
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
                context, currentDialogLaunchTimes, currentYear,
                currentDay, (currentDayCount.toShort() + 1).toShort()
            )
        } else {
            setCurrentDayDialogLaunchTimes(
                context, currentDialogLaunchTimes, currentYear,
                currentDay, 1.toShort()
            )
        }
    }

    @JvmStatic
    fun get365DayPeriodDialogLaunchTimes(context: Context): Short {
        var currentDay =
            ((Date().time - getDialogFirstLaunchTime(context)) / Time.DAY).toShort()
        val currentYear: Byte = (currentDay / YEAR_IN_DAYS).toByte()
        var dialogLaunchTimes = context.getPreferences()
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

    @JvmStatic
    fun setCustomEventCount(context: Context, eventName: String, eventCount: Short) =
        context.editPrefs { putInt(PREF_KEY_CUSTOM_EVENT_PREFIX + eventName, eventCount.toInt()) }

    @JvmStatic
    fun getCustomEventCount(context: Context, eventName: String): Short =
        context.getPreferences().getInt(PREF_KEY_CUSTOM_EVENT_PREFIX + eventName, 0).toShort()

    @JvmStatic
    fun setDialogFirstLaunchTime(context: Context) =
        context.editPrefs { putLong(PREF_KEY_DIALOG_FIRST_LAUNCH_TIME, Date().time) }

    @JvmStatic
    fun getDialogFirstLaunchTime(context: Context): Long =
        context.getPreferences().getLong(PREF_KEY_DIALOG_FIRST_LAUNCH_TIME, 0L)

    @JvmStatic
    fun getInstallDate(context: Context): Long =
        context.getPreferences().getLong(PREF_KEY_INSTALL_DATE, 0L)

    /**
     *
     * Sets the Rate Dialog agreed or declined flag.
     *
     * If true, the user has either agreed or declined to rating the app.
     * Meaning the rating dialog shouldn't be shown again.
     *
     * @param context context
     * @param agreedOrDeclined the Rate Dialog agree flag
     */
    @JvmStatic
    fun setAgreedOrDeclined(context: Context, agreedOrDeclined: Boolean) =
        context.editPrefs { putBoolean(PREF_KEY_AGREED_OR_DECLINED, agreedOrDeclined) }

    @JvmStatic
    fun getAgreedOrDeclined(context: Context): Boolean =
        context.getPreferences().getBoolean(PREF_KEY_AGREED_OR_DECLINED, false)

    /**
     *
     * Sets number of times the app has been launched.
     *
     * @param context context
     * @param launchTimes number of launch times to set
     */
    @JvmStatic
    fun setLaunchTimes(context: Context, launchTimes: Short) =
        context.editPrefs { putInt(PREF_KEY_LAUNCH_TIMES, launchTimes.toInt()) }

    @JvmStatic
    fun getLaunchTimes(context: Context): Short =
        context.getPreferences().getInt(PREF_KEY_LAUNCH_TIMES, 0).toShort()

    @JvmStatic
    fun setLastTimeShown(context: Context) =
        context.editPrefs { putLong(PREF_KEY_LAST_TIME_SHOWN, Date().time) }

    @JvmStatic
    fun getLastTimeShown(context: Context): Long =
        context.getPreferences().getLong(PREF_KEY_LAST_TIME_SHOWN, 0L)

    /**
     *
     * Sets to [.PREF_KEY_REMIND_LAUNCHES_NUMBER] the current number of app launches.
     *
     * The Library calls this method when the neutral button is clicked.
     *
     * @param context context
     */
    @JvmStatic
    fun setRemindLaunchesNumber(context: Context) =
        context.editPrefs {
            putInt(
                PREF_KEY_REMIND_LAUNCHES_NUMBER,
                getLaunchTimes(context).toInt()
            )
        }

    @JvmStatic
    fun getRemindLaunchesNumber(context: Context): Short =
        context.getPreferences().getInt(PREF_KEY_REMIND_LAUNCHES_NUMBER, 0).toShort()

    /**
     *
     * Clears shared preferences that were set up by clicking the Remind Button.
     *
     * @param context context
     */
    @JvmStatic
    fun clearRemindButtonClick(context: Context) =
        context.editPrefs {
            putLong(PREF_KEY_LAST_TIME_SHOWN, 0L)
            putInt(PREF_KEY_REMIND_LAUNCHES_NUMBER, 0)
        }

    @JvmStatic
    fun setVersionCode(context: Context) =
        context.editPrefs { putLong(PREF_KEY_VERSION_CODE, getLongVersionCode(context)) }

    @JvmStatic
    fun getVersionCode(context: Context): Long =
        context.getPreferences().getLong(PREF_KEY_VERSION_CODE, 0L)

    @JvmStatic
    fun setVersionName(context: Context) =
        context.editPrefs {
            putString(
                PREF_KEY_VERSION_NAME,
                AppInformation.getVersionName(context)
            )
        }

    @JvmStatic
    fun getVersionName(context: Context): String? =
        context.getPreferences().getString(PREF_KEY_VERSION_NAME, EMPTY_STRING)

    @JvmStatic
    fun dialogShown(context: Context) {
        if (getDialogFirstLaunchTime(context) == 0L) {
            setDialogFirstLaunchTime(context)
        }
        increment365DayPeriodDialogLaunchTimes(context)
    }

    @JvmStatic
    fun setReminderToShowAgain(context: Context) {
        setLastTimeShown(context)
        setRemindLaunchesNumber(context)
    }

    @JvmStatic
    fun getDelay(context: Context): Long =
        context.getPreferences().getLong(PREF_KEY_DELAY_DATE, UNSET_DELAY_DATE)

    @JvmStatic
    fun setDelay(context: Context, delay: Duration) =
        context.editPrefs {
            putLong(PREF_KEY_DELAY_DATE, Date().time + delay.inWholeMilliseconds)
        }

    @JvmStatic
    fun setDelayUntil(context: Context, date: Date) =
        context.editPrefs { putLong(PREF_KEY_DELAY_DATE, date.time) }

    @JvmStatic
    fun addDelay(context: Context, delay: Duration) {
        var currentDelay: Long =
            context.getPreferences().getLong(PREF_KEY_DELAY_DATE, UNSET_DELAY_DATE)
        if (currentDelay == UNSET_DELAY_DATE) {
            currentDelay = Date().time
        }
        context.editPrefs {
            putLong(PREF_KEY_DELAY_DATE, currentDelay + delay.inWholeMilliseconds)
        }
    }
}