package com.example.mywebviewtest

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class UserInfoDTO (val userId: String, val password: String)
class AndroidBridge() {

    private var callback: BridgeListener? = null

    fun setListener(listener: BridgeListener) {
        callback = listener
    }

    @JavascriptInterface
    fun sendMessage() {
        println("sendMessage called")
    }
    @JavascriptInterface
    fun setParamData(msg: UserInfoDTO) {
        println("msg:${msg}")
    }

    @JavascriptInterface
    fun makeToast(msg: String) {
        callback?.showToast(msg)
    }

    interface BridgeListener {
        fun showToast(msg: String)
    }
}



class MainActivity : AppCompatActivity(), AndroidBridge.BridgeListener {

    private lateinit var webView: WebView
    private lateinit var mProgressBar: ProgressBar
    private val bridge = AndroidBridge()

    private lateinit var editSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnLogin: Button
    private lateinit var btnJoin: Button
    private lateinit var btnMytData: Button
    private lateinit var btnMytFare: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.myWebview)
        mProgressBar = findViewById(R.id.progress1)

        editSearch = findViewById(R.id.edtSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnLogin = findViewById(R.id.btnLogin)
        btnJoin = findViewById(R.id.btnJoin)
        btnMytData = findViewById(R.id.btnMytData)
        btnMytFare = findViewById(R.id.btnMytFare)

        btnLogin.setOnClickListener {
            Toast.makeText(this, "btnLogin", Toast.LENGTH_SHORT).show()
            // todo: TWD 로그인 팝업
        }

        btnJoin.setOnClickListener {
            Toast.makeText(this, "btnJoin", Toast.LENGTH_SHORT).show()
            // todo: TWD 회원가입 팝업
        }

        btnMytData.setOnClickListener {
            Toast.makeText(this, "btnMytData", Toast.LENGTH_SHORT).show()
            // todo: TWD 나의데이터 화면이동
            val nextIntent = Intent(this, MainActivity2::class.java)
            startActivity(nextIntent)
        }

        btnMytFare.setOnClickListener {
            Toast.makeText(this, "btnMytFare", Toast.LENGTH_SHORT).show()
            // todo: TWD 나의요금 화면이동
            val nextIntent = Intent(this, MainActivity3::class.java)
            startActivity(nextIntent)
        }

        editSearch.setOnClickListener {
//            Toast.makeText(this, "editSearch", Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            Toast.makeText(this, "btnSearch", Toast.LENGTH_SHORT).show()

            val edtSearchData = editSearch.text
            webView.loadUrl(edtSearchData.toString())
        }


        webView.apply {
            webViewClient = WebViewClientClass() // new WebViewClient() 클릭시 새창 안뜨게

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
                    val newWebView = WebView(this@MainActivity).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val dialog = Dialog(this@MainActivity).apply {
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

        webView.addJavascriptInterface(bridge, "HybridApp")
        bridge.setListener(this)

        val url = "http://192.168.0.32:3000"
        webView.loadUrl(url)

    }



    inner class WebViewClientClass: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url!!)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mProgressBar.visibility = ProgressBar.VISIBLE
            webView.visibility = View.INVISIBLE
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            mProgressBar.visibility = ProgressBar.GONE
            webView.visibility = View.VISIBLE
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
//            super.onReceivedSslError(view, handler, error)
            var builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@MainActivity)
            var message = "SSL Certificatae error."
            when (error?.primaryError) {
                SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> message = "The certificate has expired."
                SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid"
            }
            message += "Do you want to continue anyway?"
            builder.setTitle("SSL Certificate Error")
            builder.setMessage(message)
            builder.setPositiveButton("continue", DialogInterface.OnClickListener { _, _ -> handler?.proceed()})
            builder.setNegativeButton("cancel", DialogInterface.OnClickListener{ dialog, which -> handler?.cancel()})
            val dialog: android.app.AlertDialog? = builder.create()
            dialog?.show()

        }
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}



