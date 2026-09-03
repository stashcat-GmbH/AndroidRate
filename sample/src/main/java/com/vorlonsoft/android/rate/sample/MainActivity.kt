package com.vorlonsoft.android.rate.sample

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory
import com.vorlonsoft.android.rate.AppRate
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

class MainActivity : ComponentActivity() {
    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private val reviewManager by lazy {
        ReviewManagerFactory.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme(
                colorScheme =
                    if (isSystemInDarkTheme()) darkColorScheme()
                    else lightColorScheme(),
            ) {
                AppRateMainScreen(onInAppReview = { onReviewRequestFailed ->
                    launchInAppReview(onReviewRequestFailed = onReviewRequestFailed)
                })
            }
        }
    }

    private fun launchInAppReview(onReviewRequestFailed: () -> Unit) {
        reviewManager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                reviewManager.launchReviewFlow(this, reviewInfo).addOnCompleteListener {
                    AppRate.with(this).apply {
                        recordDialogShown()
                        remindLater()
                    }

                    if (it.exception != null) {
                        Log.e(
                            TAG, "Could not request review flow", it.exception
                        )
                    } else {
                        Log.i(
                            TAG, "Successfully launched in-app review"
                        )
                    }
                }
            }

            .addOnFailureListener { error ->
                Log.e(TAG, "Could not request review flow", error)

                if (!isFinishing && !isDestroyed) {
                    onReviewRequestFailed.invoke()
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRateMainScreen(modifier: Modifier = Modifier, onInAppReview: (() -> Unit) -> Unit) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar = { message: String, duration: SnackbarDuration ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = message, duration = duration)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.medium,
                    modifier = modifier.padding(horizontal = 16.dp),
                )
            }
        },
    ) { contentPadding ->
        val context = LocalContext.current

        var showFallbackDialog by rememberSaveable { mutableStateOf(false) }
        val delayedMessage = stringResource(R.string.snackbar_message_delay)

        val openFallbackDialog: () -> Unit = {
            if (!showFallbackDialog) {
                Log.i(MainActivity.TAG, "Showing fallback dialog")

                AppRate.with(context).recordDialogShown()
                showFallbackDialog = true
            }
        }

        LaunchedEffect(context) {
            if (!showFallbackDialog &&
                AppRate.areAppRatingConditionsMet(context)
            ) {
                onInAppReview {
                    openFallbackDialog.invoke()
                }
            }
        }

        if (showFallbackDialog) {
            AppRateDialog(
                onRate = {
                    val appId = "com.vorlonsoft.android.rate.sample"
                    val uri = "https://play.google.com/store/apps/details?id=$appId".toUri()

                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, uri)
                                .setPackage("com.android.vending")
                        )
                    } catch (_: ActivityNotFoundException) {
                        // no play store, open web view
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: ActivityNotFoundException) {
                            showSnackbar(
                                "Kein Play Store oder Browser verfügbar.",
                                SnackbarDuration.Short,
                            )
                        }
                    }

                    AppRate.with(context).setAgreedOrDeclinedDialog(true)
                    showFallbackDialog = false
                },
                onNever = {
                    AppRate.with(context).setAgreedOrDeclinedDialog(true)
                    showFallbackDialog = false
                },
                onLater = {
                    AppRate.with(context).remindLater()
                    showFallbackDialog = false
                },
            )
        }

        Column(modifier = Modifier.padding(contentPadding)) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.hello_world)
            )

            Row(modifier = Modifier.weight(1f)) { }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    content = {
                        Text(stringResource(R.string.fallback_dialog_button))
                    },
                    onClick = {
                        openFallbackDialog.invoke()
                    },
                )

                TextButton(
                    content = {
                        Text(stringResource(R.string.delay_button))
                    },
                    onClick = {
                        AppRate.with(context).setDelay(7.days)
                        showSnackbar.invoke(delayedMessage, SnackbarDuration.Short)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppRateMainScreenPreview() {
    AppRateMainScreen(onInAppReview = {})
}

@Composable
fun AppRateDialog(
    onRate: () -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.new_rate_dialog_title)) },
        text = { Text(stringResource(R.string.new_rate_dialog_message)) },
        confirmButton = {
            Row {
                TextButton(onClick = onNever) {
                    Text(stringResource(R.string.new_rate_dialog_never))
                }

                TextButton(onClick = onLater) {
                    Text(stringResource(R.string.new_rate_dialog_later))
                }

                Spacer(Modifier.weight(1f))

                TextButton(onClick = onRate) {
                    Text(stringResource(R.string.new_rate_dialog_ok))
                }
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    )
}

@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun AppRateDialogPreview() {
    AppRateDialog({ }, { }, { })
}