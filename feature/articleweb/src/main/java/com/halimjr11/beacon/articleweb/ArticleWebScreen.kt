package com.halimjr11.beacon.articleweb

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.halimjr11.beacon.ui.theme.BeaconIcons

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled") // News sites need JavaScript; file/content access stay off.
@Composable
fun ArticleWebScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // Explicit types: both are nullable (null = unusable url). Without them, analyzers that cannot
    // resolve Compose's generic `remember` infer a non-null type and flag the null checks below.
    val target: String? = remember(url) { WebUrlPolicy.resolve(url) }
    val external: String? = remember(url) { WebUrlPolicy.external(url) }
    var progress by remember { mutableFloatStateOf(0f) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    fun openExternal() {
        external?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
    }

    BackHandler(enabled = canGoBack && !failed) { webView?.goBack() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(BeaconIcons.Back, contentDescription = stringResource(R.string.web_back))
                    }
                },
                actions = {
                    if (external != null) {
                        IconButton(onClick = ::openExternal) {
                            Icon(BeaconIcons.OpenInBrowser, contentDescription = stringResource(R.string.web_open_in_browser))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (target == null || failed) {
                // Invalid url: no WebView is created at all (AC 4.3).
                ErrorPane(
                    message = stringResource(if (target == null) R.string.web_invalid_url else R.string.web_load_error),
                    // Leaving the error pane recreates the WebView (factory), which loads the url again.
                    onRetry = if (target != null) ({ failed = false; loading = true }) else null,
                    onOpenExternal = if (external != null) ::openExternal else null,
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { it.destroy(); if (webView === it) webView = null },
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView, newProgress: Int) {
                                    progress = newProgress / 100f
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                                    !WebUrlPolicy.isAllowedScheme(request.url.toString()) // true = blocked

                                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                    loading = true
                                    canGoBack = view.canGoBack()
                                }

                                override fun onPageFinished(view: WebView, url: String?) {
                                    loading = false
                                    canGoBack = view.canGoBack()
                                }

                                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                    if (request.isForMainFrame) failed = true
                                }
                            }
                            webView = this
                            loadUrl(target)
                        }
                    },
                )
                if (loading) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ErrorPane(message: String, onRetry: (() -> Unit)?, onOpenExternal: (() -> Unit)?) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, textAlign = TextAlign.Center)
        if (onRetry != null) Button(onClick = onRetry) { Text(stringResource(R.string.web_retry)) }
        if (onOpenExternal != null) {
            OutlinedButton(onClick = onOpenExternal) { Text(stringResource(R.string.web_open_in_browser)) }
        }
    }
}
