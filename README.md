[![AndroidRate Logo](https://raw.githubusercontent.com/Vorlonsoft/AndroidRate/master/logo/152px.png)](#)

This Project is a fork and an extension to this existing
[AndroidRate](https://github.com/Vorlonsoft/AndroidRate) Project, which in turn was originally based
on this [Project](https://github.com/hotchemi/Android-Rate).

[![AndroidRate animated screenshots](https://raw.githubusercontent.com/Vorlonsoft/AndroidRate/master/screenshots/screenshots_360x640.gif)](#)

## Contents

* [Usage](#usage)
    * [Configuration](#configuration)
    * [OnClickButtonListener interface](#onclickbuttonlistener-interface)
    * [Custom event requirements](#optional-custom-event-requirements)
    * [Clear show dialog flag](#clear-show-dialog-flag)
    * [Forced Rate Dialog](#forced-display-of-the-rate-dialog)
    * [Forced dismiss of the Dialog](#forced-dismiss-of-the-rate-dialog)
    * [Custom view](#set-custom-view)
    * [Custom theme](#specific-theme)
    * [Custom dialog labels](#custom-dialog-labels)
    * [Appstores](#appstores)
    * [Сustom intents](#custom-intents)
    * [Check for Google Play](#check-for-google-play)
* [Sample](#sample)
* [Javadoc Documentation](#javadoc-documentation)
* [Supported Languages](#supported-languages)
* [Already in Use](#already-in-use)
* [Contribute](#contribute)
* [License](#license)

## Usage

### Configuration

AndroidRate library provides methods to configure it's behavior. Select the type of configuration
that best describes your needs. The configuration is best setup in your App's Application class.
The rating dialog itself should be shown at your own discretion,
[the Android Documentation](https://developer.android.com/guide/playcore/in-app-review#when-to-request)
has some advice about when to show app rating dialogs.

#### Nano configuration

Uses library's defaults.

```kotlin
// in activity
AppRate.quickStart(activity)
```

#### Micro configuration

Configures basic library behavior only.

```kotlin
// in application
AppRate.with(applicationContext)
    // default is 10 days, 10 days mean dialog is shown 10 days after first app launch
    .setTimeToWaitAfterInstall(3.days)
    // default is 10, 10 means app is launched 10 or more times
    .setLaunchTimes(10.toByte())
    // default is 1 day, 1 day means app is launched 1 day or more after neutral button clicked
    .setRemindTimeToWait(2.days)
    // default is 0, 1 means app is launched 1 or more times after neutral button clicked
    .setRemindLaunchesNumber(1.toByte())
    .monitor()
```

```kotlin
// in activity
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        // check if play store / network is available
        AppRate.showRateDialogIfMeetsConditions(activity)
    }
}
```
Also see [Check for Google Play](#check-for-google-play)

#### Standard configuration

The choice of most corporate developers.

```kotlin
// in application
AppRate.with(applicationContext)
    // default is GOOGLEPLAY (Google Play) for other options have a look at the Appstores section
    .setStoreType(StoreType.GOOGLEPLAY)
    // use this when you want to use the google in-app review API
    // if set, a google play store app review dialog is shown directly in the app
    .useGoogleInAppReview()
    // default is 10 days, 10 days mean dialog is shown 10 days after first app launch
    .setTimeToWaitAfterInstall(3.days)
    // default is 10, 10 means app is launched 10 or more times
    .setLaunchTimes(10.toByte())
    // default is 1 day, 1 day means app is launched 1 day or more after neutral button clicked
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
    .setDebug(false)
    .setOnClickButtonListener(object : OnClickButtonListener {
        override fun onClickButton(which: Byte) {
            Log.d(TAG, "RateButton: $which")
        }
    })
    .monitor()
```

Default options of the Rate Dialog are as below:

1. Google Play launches when you press the positive button. Change via `AppRate#setStoreType(int)`
   , `AppRate#setStoreType(int, long)`, `AppRate#setStoreType(vararg uris: String)`
   or `AppRate#setStoreType(vararg intents: Intent)`.
   1. Call this when you want to use the
[google in-app review API](https://developer.android.com/guide/playcore/in-app-review): `AppRate#useGoogleInAppReview()`
2. App is launched 10 or more days later than installation. Change
   via `AppRate#setTimeToWaitAfterInstall(kotlin.time.Duration)`.
3. App is launched 10 or more times. Change via `AppRate#setLaunchTimes(byte)`.
4. App is launched 1 or more days after neutral button clicked. Change
   via `AppRate#setRemindTimeToWait(kotlin.time.Duration)`.
5. App is launched 0 or more times after neutral button clicked. Change
   via `AppRate#setRemindLaunchesNumber(byte)`.
6. Each launch (the condition is satisfied if appLaunches % `param` == 0). Change
   via `AppRate#setSelectedAppLaunches(byte)`.
7. App shows the Neutral button ("Remind me later"). Change via `setShowLaterButton(boolean)`.
8. Unlimited occurrences of the display of the Rate Dialog within a 365-day period. Change
   via `AppRate#set365DayPeriodMaxNumberDialogLaunchTimes(short)`.
9. Don't re-enable the Rate Dialog if a new version of app with different version code is installed.
   Change via `AppRate#setVersionCodeCheck(boolean)`.
10. Don't re-enable the Rate Dialog if a new version of app with different version name is
    installed. Change via `AppRate#setVersionNameCheck(boolean)`.
11. Setting `AppRate#setDebug(boolean)` to `true` ensures that the Rate Dialog will be shown each
    time the app is launched. **This feature is for development only!**.
12. There is no default callback when the button of Rate Dialog is pressed. Change
    via `AppRate.with(this).setOnClickButtonListener(OnClickButtonListener)`.

### OnClickButtonListener interface

You can implement OnClickButtonListener Interface and use
`AppRate.with(this).setOnClickButtonListener(OnClickButtonListener)` to specify the callback when
the button of Rate Dialog is pressed.
`DialogInterface.BUTTON_POSITIVE`, `DialogInterface.BUTTON_NEUTRAL` or
`DialogInterface.BUTTON_NEGATIVE` will be passed in the argument of
`OnClickButtonListener#onClickButton`.

```kotlin
AppRate.with(applicationContext)
    .setOnClickButtonListener(object : OnClickButtonListener {
        override fun onClickButton(which: Byte) {
            Log.d(TAG, "RateButton: $which")
        }
    })
```

### Optional custom event requirements

You can add additional optional requirements for showing dialog. Each requirement can be
added/referenced as a unique string. You can set a minimum count for each such event (for e.g. "
action_performed" 3 times,
"button_clicked" 5 times, etc.)

```kotlin
AppRate.with(context).setMinimumEventCount(String,short);
AppRate.with(context).incrementEventCount(String);
AppRate.with(context).setEventCountValue(String,short);
```

### Clear show dialog flag

When you want to show the dialog again, call
`AppRate#clearAgreeShowDialog()`.

```kotlin
AppRate.with(context).clearAgreeShowDialog();
```

### Forced display of the Rate Dialog

Use this method directly if you want to force display of the Rate Dialog. Call it when some button
presses on. Method also useful for testing purposes. Call `AppRate#showRateDialog(Activity)`.

```kotlin
AppRate.with(context).showRateDialog(activity);
```

### Forced dismiss of the Rate Dialog

Use this method directly if you want to remove the Rate Dialog from the screen.
Call `AppRate#dismissRateDialog()`.

```kotlin
AppRate.with(context).dismissRateDialog();
```

### Set custom view

Call `AppRate#setView(View)`.

```kotlin
val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
val view: View = inflater.inflate(R.layout.custom_dialog, findViewById<View>(R.id.layout_root) as ViewGroup)
AppRate.with(this).setView(view)
```

### Specific theme

You can use a specific theme to inflate the dialog.

```kotlin
AppRate.with(context).setThemeResId(int);
```

### Custom dialog labels

If you want to use your own dialog labels, override string xml resources on your application.

```xml
<resources>
    <string name="rate_dialog_title">Rate this app</string>
    <string name="rate_dialog_message">If you enjoy playing this app, would you mind taking a moment
        to rate it? It won\'t take more than a minute. Thanks for your support!
    </string>
    <string name="rate_dialog_ok">Rate It Now</string>
    <string name="rate_dialog_cancel">Remind Me Later</string>
    <string name="rate_dialog_no">No, Thanks</string>
</resources>
```

### Appstores

You can use different app stores.

#### Google Play, Amazon Appstore, Cafe Bazaar, Mi Appstore (Xiaomi Market), Samsung Galaxy Apps, SlideME Marketplace, Tencent App Store, Yandex.Store

```kotlin
AppRate.with(context).setStoreType(StoreType.GOOGLEPLAY) // Google Play
AppRate.with(context).setStoreType(StoreType.AMAZON)     // Amazon Appstore
AppRate.with(context).setStoreType(StoreType.BAZAAR)     // Cafe Bazaar
AppRate.with(context).setStoreType(StoreType.MI)         // Mi Appstore (Xiaomi Market)
AppRate.with(context).setStoreType(StoreType.SAMSUNG)    // Samsung Galaxy Apps
AppRate.with(context).setStoreType(StoreType.SLIDEME)    // SlideME Marketplace
AppRate.with(context).setStoreType(StoreType.TENCENT)    // Tencent App Store
AppRate.with(context).setStoreType(StoreType.YANDEX)     // Yandex.Store
```

#### Apple App Store

```kotlin
/* Apple App Store, long - your Apple App Store application ID
 * e. g. 284882215 for Facebook (https://itunes.apple.com/app/id284882215) */
AppRate.with(context).setStoreType(StoreType.APPLE,long)
```

#### BlackBerry World

```kotlin
/* BlackBerry World, long - your BlackBerry World application ID
 * e. g. 50777 for Facebook (https://appworld.blackberry.com/webstore/content/50777) */
AppRate.with(context).setStoreType(StoreType.BLACKBERRY,long)
```

#### Chinese app stores

The first Chinese app store found on the user device will be used, if first fails, second will be
used, etc. The Library doesn't check the availability of your application on the app store.

```kotlin
/* 19 chinese app stores: 腾讯应用宝, 360手机助手, 小米应用商店, 华为应用商店, 百度手机助手,
 * OPPO应用商店, 中兴应用商店, VIVO应用商店, 豌豆荚, PP手机助手, 安智应用商店, 91手机助手,
 * 应用汇, QQ手机管家, 机锋应用市场, GO市场, 宇龙Coolpad应用商店, 联想应用商店, cool市场 */
AppRate.with(context).setStoreType(StoreType.CHINESESTORES)
```

#### Other store

```kotlin
/* Any other store/stores,
 * String... - an RFC 2396-compliant URI or array of URIs to your app,
 * e. g. "https://otherstore.com/app?id=com.yourapp"
 * or "otherstore://apps/com.yourapp" */
AppRate.with(context).setStoreType(vararg uris: String)
```

### Custom intents

You can set custom action to the Positive button. For example, you want to open your custom
RateActivity when the Rate button clicked.

```kotlin
/* Any custom intent/intents, Intent... - an intent or array of intents,
 * first will be executed (startActivity(intents[0])), if first fails,
 * second will be executed (startActivity(intents[1])), etc. */
AppRate.with(context).setStoreType(vararg intent: Intent)
```

### Check for Google Play

The following code checks that Google Play is available on the user's device. We recommend to use it
if current app store type from library options is StoreType.GOOGLEPLAY.

```kotlin
if (AppRate.with(context).getStoreType() == StoreType.GOOGLEPLAY &&
    GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) != ConnectionResult.SERVICE_MISSING
) {
    // launch dialog
}
```

## Sample

Tryout AndroidRate by checking out the
[sample](https://github.com/stashcat-GmbH/AndroidRate/tree/master/sample) module.

## Supported Languages

AndroidRate currently supports the following languages:

* Albanian
* Arabic
* Azerbaijani
* Basque
* Benqali
* Bulgarian
* Catalan
* Chinese (zh-CN, zh-TW)
* Croatian
* Czech
* Danish
* Dutch
* English
* Finnish
* French
* German
* Greek
* Hebrew
* Hindi
* Hungarian
* Indonesian
* Italy
* Japanese
* Korean
* Malay
* Norwegian
* Persian
* Polish
* Portuguese
* Romanian
* Russian
* Serbian
* Slovak
* Slovenian
* Spanish
* Swedish
* Thai
* Turkish
* Ukrainian
* Vietnamese

## License

    The MIT License (MIT)

    Copyright (c) 2017 - 2018 Vorlonsoft LLC
    Copyright (c) 2023 stashcat GmbH

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
