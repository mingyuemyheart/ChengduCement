package com.wazh.shawn.cement

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.wazh.shawn.cement.common.CONST
import kotlinx.android.synthetic.main.activity_x5.*
import kotlinx.android.synthetic.main.layout_title.*

class X5Activity : BaseActivity(), OnClickListener {

    private var mExitTime: Long = 0 //记录点击完返回按钮后的long型时间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_x5)
        initWidget()
        initWebView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle.text = title
        }
    }

    /**
     * 初始化webview
     */
    private fun initWebView() {
        val url = intent.getStringExtra(CONST.WEB_URL)
        if (TextUtils.isEmpty(url)) {
            return
        }
        showDialog()
        val webSettings = webView.settings
        //支持javascript
        webSettings.javaScriptEnabled = true
        // 设置可以支持缩放
        webSettings.setSupportZoom(true)
        // 设置出现缩放工具
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        //扩大比例的缩放
        webSettings.useWideViewPort = true
        //自适应屏幕
        webSettings.loadWithOverviewMode = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webView.loadUrl(url)
        webView.addJavascriptInterface(JSInterface(), "Android")

        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }
        })

        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, itemUrl: String?): Boolean {
                webView.loadUrl(itemUrl)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cancelDialog()
            }
        })
    }

    private class JSInterface {
        @JavascriptInterface
        fun jsCallAndroid() {
        }

        @JavascriptInterface
        fun jsCallAndroidArgs(args: String?) {
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) {
            webView!!.reload()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                webView.canGoBack() -> {
                    webView.goBack()
                }
                System.currentTimeMillis() - mExitTime > 2000 -> {
                    Toast.makeText(this, "再按一次退出" + getString(R.string.app_name), Toast.LENGTH_SHORT).show()
                    mExitTime = System.currentTimeMillis()
                }
                else -> {
                    finish()
                }
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

}
