[![AndroidRate Logo](https://raw.githubusercontent.com/Vorlonsoft/AndroidRate/master/logo/152px.png)](#)

This Project is a fork and an extension to this existing
[AndroidRate](https://github.com/Vorlonsoft/AndroidRate) Project, which in turn was originally based
on this [Project](https://github.com/hotchemi/Android-Rate).

## Contents

<!-- TOC -->
  * [Usage](#usage)
    * [Configuration](#configuration)
      * [Minimal setup](#minimal-setup)
      * [Basic setup](#basic-setup)
      * [Standard configuration](#standard-configuration)
      * [Optional custom event requirements](#optional-custom-event-requirements)
      * [Clear show dialog flag](#clear-show-dialog-flag)
    * [Showing an app rating Dialog](#showing-an-app-rating-dialog)
      * [When and how to show a dialog](#when-and-how-to-show-a-dialog)
      * [Processing result of dialog](#processing-result-of-dialog)
  * [Sample](#sample)
  * [License](#license)
<!-- TOC -->

## Usage

### Configuration
**NOTE**: This library does not show any dialogs anymore, you have to handle that yourself.
<br/>

The AndroidRate library allows you to track statistics about your users' basic app usage. This includes
things like the number of app launches and minimum time interval since the installation date.

Users of this library can create a configuration for when an app rating dialog should be shown.
This is preferably done in the app's Application class. After that they can query the
library about whether the configured conditions are met.

Rating dialogs should be shown at your own discretion,
[the Android Documentation](https://developer.android.com/guide/playcore/in-app-review#when-to-request)
has some advice about when to show app rating dialogs.

#### Minimal setup

Uses library's defaults.

```kotlin
AppRate.with(context).monitor()
```

#### Basic setup

Set up a basic app rating configuration.

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

#### Standard configuration

The choice of most corporate developers.

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
    // default is 1, 1 means each launch, 2 means every 2nd launch, 3 means every 3rd launch, etc
    .setSelectedAppLaunches(4.toByte())
    // default is unlimited, 3 means 3 or fewer occurrences of the display of the Rate Dialog within a 365-day period
    .set365DayPeriodMaxNumberDialogLaunchTimes(3.toShort())
    // default is false, true means to re-enable the Rate Dialog if a new version of app with different version code is installed
    .setVersionCodeCheck(true)
    // default is false, true means to re-enable the Rate Dialog if a new version of app with different version name is installed
    .setVersionNameCheck(true)
    // default is false, true is for development only, true ensures that the Rate Dialog will be shown each time the app is launched
    .setDebug(false)
    .monitor()
```

Default options of the Rate Dialog are as below:

1. App is launched 10 or more days later than installation. Change
   via `AppRate#setTimeToWaitAfterInstall(kotlin.time.Duration)`.
2. App is launched 10 or more times. Change via `AppRate#setLaunchTimes(byte)`.
3. App is launched 1 or more days after neutral button clicked. Change
   via `AppRate#setRemindTimeToWait(kotlin.time.Duration)`.
4. App is launched 0 or more times after neutral button clicked. Change
   via `AppRate#setRemindLaunchesNumber(byte)`.
5. Each launch (the condition is satisfied if appLaunches % `param` == 0). Change
   via `AppRate#setSelectedAppLaunches(byte)`.
6. Unlimited occurrences of the display of the Rate Dialog within a 365-day period. Change
   via `AppRate#set365DayPeriodMaxNumberDialogLaunchTimes(short)`.
7. Don't re-enable the Rate Dialog if a new version of app with different version code is installed.
   Change via `AppRate#setVersionCodeCheck(boolean)`.
8. Don't re-enable the Rate Dialog if a new version of app with different version name is
   installed. Change via `AppRate#setVersionNameCheck(boolean)`.
9. Setting `AppRate#setDebug(boolean)` to `true` ensures that the Rate Dialog will be shown each
   time the app is launched. **This feature is for development only!**.

#### Optional custom event requirements

You can add additional optional requirements for showing dialogs. Each requirement can be
added/referenced as a unique string. You can set a minimum count for each such event (for e.g. "
action_performed" 3 times,
"button_clicked" 5 times, etc.)

```kotlin
AppRate.with(context).setMinimumEventCount(String,short)
AppRate.with(context).incrementEventCount(String)
AppRate.with(context).setEventCountValue(String,short)
```

#### Clear show dialog flag

When you want to show a dialog again, call `AppRate#clearAgreeShowDialog()`.

```kotlin
AppRate.with(context).clearAgreeShowDialog()
```

### Showing an app rating Dialog

#### When and how to show a dialog

The basis for when to show an app rating dialog is formed by the 
`AppRate#areAppRatingConditionsMet()` function. When it returns true, all the conditions set up
in the [configuration step](#configuration) are met and an app rating dialog should be shown. 
You can also use the 
[google in-app review API](https://developer.android.com/guide/playcore/in-app-review)
in that case, or combine both like we did in the 
[sample](https://github.com/stashcat-GmbH/AndroidRate/tree/master/sample).

```kotlin
if (AppRate.areAppRatingConditionsMet(context)) {
    // show dialog UI
    AppRateDialog()
    
    // tell the library the dialog was shown
    LaunchedEffect(Unit) {
        AppRate.with(context).recordDialogShown()    
    }
}
```

#### Processing result of dialog
This is an example of what to do in reaction to button presses of a rating dialog. The example
has three buttons: Never, Later, Rate. This example is taken form the
[sample](https://github.com/stashcat-GmbH/AndroidRate/tree/master/sample).

```kotlin
AppRateDialog(
    onRate = {
        // lead user to an external store or use custom app rating
        AppRate.with(context).setAgreedOrDeclinedDialog(true)
    },
    onNever = {
        AppRate.with(context).setAgreedOrDeclinedDialog(true)
    },
    onLater = {
        AppRate.with(context).remindLater()
    },
)
```

`AppRate.with(context).setAgreedOrDeclinedDialog(true)` basically tells the library that no more
dialogs should be shown, the rating has already been handled.

`AppRate.with(context).remindLater()` the app rating dialog will be shown again, 
after `remindInterval`and `remindLaunchesNumber` are fulfilled.

## Sample

Tryout AndroidRate by checking out the
[sample](https://github.com/stashcat-GmbH/AndroidRate/tree/master/sample) module.

## License

    The MIT License (MIT)

    Copyright (c) 2017 - 2018 Vorlonsoft LLC
    Copyright (c) 2023 - 2026 stashcat GmbH

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
