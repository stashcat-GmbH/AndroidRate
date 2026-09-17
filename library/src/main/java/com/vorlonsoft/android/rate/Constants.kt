/*
 * Copyright 2018 Vorlonsoft LLC
 *
 * Licensed under The MIT License (MIT)
 */

package com.vorlonsoft.android.rate

/**
 * Constants Object - the constants object of the AndroidRate library.
 *
 * Contains constants.
 *
 * @since       1.1.8
 * @version     2.0.0
 * @author      Alexander Savin
 */
internal object Constants {
    /**
     * Constants.Date Object - the date constants object of the AndroidRate library.
     *
     * Contains date constants.
     *
     * @since       1.1.8
     * @version     2.0.0
     * @author      Alexander Savin
     */
    internal object Date {
        /** The time unit representing one year in days. */
        internal const val YEAR_IN_DAYS: Short = 365.toShort()
    }

    /**
     * Constants.Utils Object - the utils constants object of the AndroidRate library.
     *
     * Contains utils constants.
     *
     * @since       1.1.8
     * @version     2.0.0
     * @author      Alexander Savin
     */
    internal object Utils {
        /** The empty String. */
        internal const val EMPTY_STRING: String = ""
        /** The tag of all log messages of AndroidRate library. */
        internal const val TAG: String = "ANDROIDRATE"
    }
}