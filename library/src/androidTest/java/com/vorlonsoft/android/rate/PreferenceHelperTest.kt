package com.vorlonsoft.android.rate

import androidx.test.platform.app.InstrumentationRegistry
import com.vorlonsoft.android.rate.test_util.PreferencesTestEnv
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class PreferenceHelperTest {

    companion object {
        private const val CUSTOM_TEST_EVENT = "CUSTOM_TEST_EVENT"
    }

    private val prefsEnv = PreferencesTestEnv()

    @Test
    fun testDefault() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertTrue(underTest.isFirstLaunch())
            assertEquals(0.toShort(), underTest.get365DayPeriodDialogLaunchTimes())
            assertEquals(false, underTest.getAgreedOrDeclined())
            assertEquals(0, underTest.getInstallDate())
            assertEquals(0.toShort(), underTest.getRemindLaunchesNumber())
            assertEquals(0, underTest.getLastTimeShown())
            assertEquals(0, underTest.getDialogFirstLaunchTime())
            assertEquals(PreferenceHelper.UNSET_DELAY_DATE, underTest.getDelay())
            assertEquals(0, underTest.getCustomEventCount("").toInt())
        }
    }

    @Test
    fun testFirstLaunch() {
        val context = InstrumentationRegistry.getInstrumentation().context
        PreferenceHelper(context, prefsEnv.testSharedPreferencesProvider).also { underTest ->
            underTest.setFirstLaunchSharedPreferences(context)
            assertEquals(
                PreferenceHelper.DEFAULT_DIALOG_LAUNCH_TIMES,
                prefsEnv.testSharedPreferences.getString(
                    PreferenceHelper.PREF_KEY_365_DAY_PERIOD_DIALOG_LAUNCH_TIMES,
                    null
                )
            )
            assertEquals(
                1,
                prefsEnv.testSharedPreferences.getInt(PreferenceHelper.PREF_KEY_LAUNCH_TIMES, -1)
            )
            assertTrue(
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_INSTALL_DATE,
                    -1
                ) > 0
            )
        }
    }

    @Test
    fun testCustomEvent() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(0, underTest.getCustomEventCount(CUSTOM_TEST_EVENT).toInt())
            underTest.setCustomEventCount(CUSTOM_TEST_EVENT, 2)
            assertEquals(2, underTest.getCustomEventCount(CUSTOM_TEST_EVENT).toInt())
            assertEquals(
                2,
                prefsEnv.testSharedPreferences.getInt(
                    PreferenceHelper.PREF_KEY_CUSTOM_EVENT_PREFIX + CUSTOM_TEST_EVENT,
                    -1
                )
            )
        }
    }

    @Test
    fun testDialogFirstLaunchTime() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(0, underTest.getDialogFirstLaunchTime())
            underTest.setDialogFirstLaunchTime()
            assertTrue(underTest.getDialogFirstLaunchTime() > 0)
            assertTrue(
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_DIALOG_FIRST_LAUNCH_TIME,
                    -1
                ) > 0
            )
        }
    }

    @Test
    fun testInstallDate() {
        val context = InstrumentationRegistry.getInstrumentation().context
        PreferenceHelper(context, prefsEnv.testSharedPreferencesProvider).also { underTest ->
            assertEquals(0, underTest.getInstallDate())
            underTest.setFirstLaunchSharedPreferences(context)
            assertTrue(underTest.getInstallDate() > 0)
            assertTrue(
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_INSTALL_DATE,
                    -1
                ) > 0
            )
        }
    }

    @Test
    fun testAgreedOrDeclined() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertFalse(underTest.getAgreedOrDeclined())
            underTest.setAgreedOrDeclined(true)
            assertTrue(underTest.getAgreedOrDeclined())
            assertTrue(
                prefsEnv.testSharedPreferences.getBoolean(
                    PreferenceHelper.PREF_KEY_AGREED_OR_DECLINED,
                    false
                )
            )
        }
    }

    @Test
    fun testLaunchTimes() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(0, underTest.getLaunchTimes().toInt())
            underTest.setLaunchTimes(333)
            assertEquals(333, underTest.getLaunchTimes().toInt())
            assertEquals(
                333,
                prefsEnv.testSharedPreferences.getInt(PreferenceHelper.PREF_KEY_LAUNCH_TIMES, -1)
            )
        }
    }

    @Test
    fun testLastTimeShown() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(0, underTest.getLastTimeShown())
            underTest.setLastTimeShown()
            assertTrue(underTest.getLastTimeShown() > 0)
            assertTrue(
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_LAST_TIME_SHOWN,
                    -1
                ) > 0
            )
        }
    }

    @Test
    fun testRemindLaunchesNumber() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(0, underTest.getRemindLaunchesNumber().toInt())
            underTest.setLaunchTimes(10)
            underTest.setRemindLaunchesNumber()
            assertEquals(10, underTest.getRemindLaunchesNumber().toInt())
            assertEquals(
                10,
                prefsEnv.testSharedPreferences.getInt(
                    PreferenceHelper.PREF_KEY_REMIND_LAUNCHES_NUMBER,
                    -1
                )
            )
        }
    }

    @Test
    fun testClearRemindButtonClick() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            underTest.setLastTimeShown()
            underTest.setLaunchTimes(3)
            underTest.setRemindLaunchesNumber()

            underTest.clearRemindButtonClick()

            assertEquals(0, underTest.getLastTimeShown())
            assertEquals(
                0,
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_LAST_TIME_SHOWN,
                    -1
                )
            )
            assertEquals(0, underTest.getRemindLaunchesNumber().toInt())
            assertEquals(
                0,
                prefsEnv.testSharedPreferences.getInt(
                    PreferenceHelper.PREF_KEY_REMIND_LAUNCHES_NUMBER,
                    -1
                )
            )
        }
    }

    @Test
    fun testReminderToShowAgain() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->

            assertEquals(0, underTest.getLastTimeShown())
            assertEquals(0, underTest.getRemindLaunchesNumber().toInt())

            underTest.setLaunchTimes(1)
            underTest.setReminderToShowAgain()

            assertTrue(underTest.getLastTimeShown() > 0)
            assertTrue(
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_LAST_TIME_SHOWN,
                    -1
                ) > 0
            )

            assertEquals(1, underTest.getRemindLaunchesNumber().toInt())
            assertEquals(
                1,
                prefsEnv.testSharedPreferences.getInt(
                    PreferenceHelper.PREF_KEY_REMIND_LAUNCHES_NUMBER,
                    -1
                )
            )
        }
    }

    @Test
    fun testDelay() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(PreferenceHelper.UNSET_DELAY_DATE, underTest.getDelay())

            val testValue = 5.days
            val testTimeStamp = Date().time + testValue.inWholeMilliseconds
            underTest.setDelay(5.days)
            assertTrue(
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_DELAY_DATE,
                    -1
                ) >= testTimeStamp
            )
        }
    }

    @Test
    fun testDelayUntil() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(PreferenceHelper.UNSET_DELAY_DATE, underTest.getDelay())

            val testTimeStamp = Date(System.currentTimeMillis() + 5.days.inWholeMilliseconds)
            underTest.setDelayUntil(testTimeStamp)
            assertEquals(
                testTimeStamp.time,
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_DELAY_DATE,
                    -1
                )
            )
        }
    }

    @Test
    fun testAddDelay() {
        PreferenceHelper(
            InstrumentationRegistry.getInstrumentation().context,
            prefsEnv.testSharedPreferencesProvider
        ).also { underTest ->
            assertEquals(PreferenceHelper.UNSET_DELAY_DATE, underTest.getDelay())

            val testTimeStamp = Date()
            underTest.setDelayUntil(testTimeStamp)
            val testDelay = 4000.hours
            underTest.addDelay(testDelay)

            assertEquals(
                testTimeStamp.time + testDelay.inWholeMilliseconds,
                prefsEnv.testSharedPreferences.getLong(
                    PreferenceHelper.PREF_KEY_DELAY_DATE,
                    -1
                )
            )
        }
    }
}