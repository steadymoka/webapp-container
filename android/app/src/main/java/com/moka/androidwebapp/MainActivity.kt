package com.moka.androidwebapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.moka.androidwebapp.ui.theme.LightishRed
import com.moka.androidwebapp.ui.theme.MyTheme
import com.moka.androidwebapp.ui.theme.Purple500
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import moka.land.imagehelper.picker.builder.ImagePicker
import kotlin.coroutines.resume

const val HOST_URL = "http://192.168.31.24:3000"
const val HOME = "${HOST_URL}/app/android"
const val TAG = "MainActivity"

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {

    companion object {
        const val KEY_LINK = "MainActivity.LINK"
    }

    private lateinit var webView: WebView
    private lateinit var bridge: AndroidBridge

    private val _isSplash = MutableLiveData<Boolean?>(null)
    private val isSplash: LiveData<Boolean?> = _isSplash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val fcmToken = getFcmToken()
            var link = intent.getStringExtra(KEY_LINK)
            if (link.isNullOrEmpty()) {
                link = HOME
            }
            setContent {
                ProvideWindowInsets {
                    MyTheme(darkTheme = false) {
                        WebPageScreen(if (null != fcmToken) "${link}?fcmToken=${fcmToken}" else link)
                        if (link == HOME) {
                            SplashView()
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event)
        }

        // splash 이면 앱 종료
        if (isSplash.value == true) {
            destroyWebView()
            finish()
            return true
        }

        // 웹뷰 뒤로가기
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }

        // 앱 종료
        destroyWebView()
        finish()
        return true
    }

    private fun destroyWebView() {
        lifecycleScope.launch {
            (window.decorView as ViewGroup).removeAllViews()
            webView.clearCache(true)
            webView.clearHistory()
            webView.destroy()
        }
    }

    private suspend fun getFcmToken(): String? {
        return ""
        return suspendCancellableCoroutine {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    it.resume(null)
                    return@OnCompleteListener
                }
                it.resume(task.result)
            })
        }
    }

    // -

    @Composable
    fun SplashView() {
        val isSplash by isSplash.observeAsState()

        Surface(
            Modifier
                .fillMaxSize()
                .background(if (isSplash != false) Color.White else Color.Transparent)
                .alpha(if (isSplash != false) 1f else 0f)
        ) {
            AnimatedVisibility(
                enter = fadeIn(0.1f, tween(500)),
                exit = fadeOut(),
                visible = (isSplash != false)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 96.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 24.dp)
                            .background(Purple500, RoundedCornerShape(6.dp))
                            .clickable {
                                bridge.requestRoutePush("/auth/login")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "로그인",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(56.dp)
                            .padding(horizontal = 24.dp)
                            .border(1.dp, Purple500, RoundedCornerShape(6.dp))
                            .clickable {
                                bridge.requestRoutePush("/auth/register")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "회원가입",
                            fontWeight = FontWeight.Bold,
                            color = Purple500,
                        )
                    }
                }
            }
        }
    }

    /**
     * Setup webView
     */

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    @Composable
    fun WebPageScreen(urlToRender: String) {
        AndroidView(
            factory = {
                WebView(it).apply {
                    webView = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = MyWebViewClient()
                    webChromeClient = MyWebChromeClient(this@MainActivity)

                    // settings
                    this.clearCache(true)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.cacheMode = LOAD_NO_CACHE
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                    val cookie = cookieManager.getCookie(HOST_URL)
                    val isLoggedIn = cookie?.contains("happtoken=") ?: false

                    // connect bridge
                    bridge = AndroidBridge(this)
                    addJavascriptInterface(bridge, "android")

                    // debug mode
                    if (BuildConfig.DEBUG) {
                        setWebContentsDebuggingEnabled(true)
                    }
                    loadUrl(urlToRender)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            update = {
                it.loadUrl(urlToRender)
            })
    }

    class MyWebViewClient : WebViewClient()

    class MyWebChromeClient(var context: Context) : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            ImagePicker
                .with(context)
                .setConfig { camera = true }
                .setOnCancel {
                    filePathCallback?.onReceiveValue(null)
                }
                .showSingle { uri ->
                    filePathCallback?.onReceiveValue(arrayOf(uri))
                }
            return true
        }
    }

    /**
     * APP & WEB Bridge
     */

    data class Message(
        var handler: String,
        var args: ArrayList<Any>,
        var cid: Int? = null
    )

    enum class WebToApp {
        onSplashVisible,
        onLogout,
        onChangeCookie,
        clearHistory,
        openNewWebview,
        ;
    }

    interface AppToWeb {
        fun requestRoutePush(link: String)
    }

    inner class AndroidBridge(var webView: WebView) : AppToWeb {

        @JavascriptInterface
        fun postMessage(message: String) {
            val message = Gson().fromJson(message, Message::class.java)

            Log.wtf("TAG.AndroidBridge", "message: ${message}")
            when (WebToApp.valueOf(message.handler)) {
                WebToApp.onSplashVisible -> {
                    val isVisible = message.args.getOrNull(0) as? Boolean ?: false
                    _isSplash.postValue(isVisible)
                }
                WebToApp.onChangeCookie -> {
                    CookieManager.getInstance().flush()
                }
                WebToApp.onLogout -> {
                    lifecycleScope.launch {
                        CookieManager.getInstance().flush()
                        startActivity(Intent(this@MainActivity, MainActivity::class.java))
                        finish()

                        destroyWebView()
                    }
                }
                WebToApp.clearHistory -> {
                    val link = message.args.getOrNull(0) as? String ?: ""
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    intent.putExtra(KEY_LINK, "${HOST_URL}${link}")
                    startActivity(intent)
                    finish()

                    destroyWebView()
                }
                WebToApp.openNewWebview -> {
                    lifecycleScope.launch {
                        val link = message.args.getOrNull(0) as? String ?: ""
                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                        intent.putExtra(KEY_LINK, "${HOST_URL}${link}")
                        startActivity(intent)

                        destroyWebView()
                    }
                }
            }

            // return invoke
            if (null != message.cid) {
                lifecycleScope.launch {
                    webView.loadUrl("javascript:__webkitCallback(${message.cid});");
                }
            }
        }

        override fun requestRoutePush(link: String) {
            lifecycleScope.launch {
                webView.loadUrl("javascript:__vueRoutePush('${link}');");
            }
        }

    }
}
