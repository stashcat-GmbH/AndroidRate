package com.vorlonsoft.android.rate.test_util

import android.content.Context
import android.content.SharedPreferences
import com.vorlonsoft.android.rate.PreferenceHelper
import org.junit.Assert.assertEquals
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer

class PreferencesTestEnv {

    val testSharedPreferencesMap: MutableMap<String, Any?> = mutableMapOf()
    var lastPreferencesFile: String? = null
    val testSharedPreferences: SharedPreferences = mock<SharedPreferences>().apply {
        // sharedPreferences mock
        whenever(all).thenAnswer {
            return@thenAnswer testSharedPreferencesMap
        }
        whenever(getString(any(), anyOrNull())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap[it.arguments[0]] as? String
                ?: it.arguments[1] as String?
        }
        whenever(getStringSet(any(), anyOrNull())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap[it.arguments[0]] as? Set<String>
                ?: it.arguments[1]
        }
        whenever(getInt(any(), anyOrNull())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap[it.arguments[0]] as? Int
                ?: it.arguments[1]
        }
        whenever(getLong(any(), anyOrNull())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap[it.arguments[0]] as? Long
                ?: it.arguments[1]
        }
        whenever(getFloat(any(), anyOrNull())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap[it.arguments[0]] as? Float
                ?: it.arguments[1]
        }
        whenever(getBoolean(any(), anyOrNull())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap[it.arguments[0]] as? Boolean
                ?: it.arguments[1]
        }
        whenever(contains(any())).thenAnswer {
            return@thenAnswer testSharedPreferencesMap.containsKey(it.arguments[0])
        }
    }
    val testSharedPreferencesEditor: SharedPreferences.Editor = mock<SharedPreferences.Editor>().apply {
        // mock editor
        val mockAnswer: Answer<SharedPreferences.Editor> = Answer {
            testSharedPreferencesMap[it.arguments[0] as String] = it.arguments[1]
            return@Answer this
        }
        whenever(putBoolean(any(), any())).thenAnswer(mockAnswer)
        whenever(putFloat(any(), any())).thenAnswer(mockAnswer)
        whenever(putInt(any(), any())).thenAnswer(mockAnswer)
        whenever(putStringSet(any(), any())).thenAnswer(mockAnswer)
        whenever(putLong(any(), any())).thenAnswer(mockAnswer)
        whenever(putString(any(), any())).thenAnswer(mockAnswer)

        whenever(clear()).thenAnswer {
            testSharedPreferencesMap.clear()
            return@thenAnswer this
        }
        whenever(remove(any())).thenAnswer {
            testSharedPreferencesMap.remove(it.arguments[0] as String)
            return@thenAnswer this
        }
    }

    val testSharedPreferencesProvider: PreferenceHelper.PreferencesProvider = object :
        PreferenceHelper.PreferencesProvider {
        override fun getSharedPreferences(
            context: Context,
            name: String,
            mode: Int
        ): SharedPreferences {
            whenever(testSharedPreferences.edit()).thenReturn(testSharedPreferencesEditor)
            lastPreferencesFile = name
            return testSharedPreferences
        }
    }
}