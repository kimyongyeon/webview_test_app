package com.example.mywebviewtest

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity2: AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        webView = findViewById(R.id.myWebview2)

        webView.apply {
            webViewClient = WebViewClient() // new WebViewClient() 클릭시 새창 안뜨게

            // 팝업이나 파일 업로드 등 설정해주기 위해 webView.webChromeClient를 설정
            // 웹뷰에서 크롬이 실행가능 && 새창띄우기는 안됨
            // webChromeClient = WebChromeClient()

            // 웹뷰에서 팝업창 호출하기 위해
            webChromeClient = object: WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    val newWebView = WebView(this@MainActivity2).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val dialog = Dialog(this@MainActivity2).apply {
                        setContentView(newWebView)
                        window?.attributes?.width = ViewGroup.LayoutParams.MATCH_PARENT
                        window?.attributes?.height = ViewGroup.LayoutParams.MATCH_PARENT
                        show()
                    }

                    newWebView.webChromeClient = object: WebChromeClient() {
                        override fun onCloseWindow(window: WebView?) {
//                            super.onCloseWindow(window)
                            dialog.dismiss()
                        }
                    }
                    (resultMsg?.obj as WebView.WebViewTransport).webView = newWebView
                    resultMsg.sendToTarget()
                    return true
//                    return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }
            }

            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true) // 새창띄우기 허용여부
            settings.javaScriptCanOpenWindowsAutomatically = true // 자바스크립트 새창뛰이기(멀티뷰) 허용여부
            settings.loadWithOverviewMode = true // 메타태그 허용여부
            settings.useWideViewPort = true // 화면 사이즈 맞추기 허용여부
            settings.setSupportZoom(true) // 화면 줌 허용여부
            settings.builtInZoomControls = true // 화면 확대 축소 허용여부
            settings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용여부
            settings.domStorageEnabled = true // 로컬저장소 허용여부
            settings.displayZoomControls = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true // api 26
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                settings.mediaPlaybackRequiresUserGesture = true
            }

            settings.allowContentAccess = true
            settings.setGeolocationEnabled(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.allowUniversalAccessFromFileURLs = true
            }
            settings.allowFileAccess = true

            fitsSystemWindows = true
        }


        webView.loadUrl("https://app.tworld.co.kr/main/home");
    }


}