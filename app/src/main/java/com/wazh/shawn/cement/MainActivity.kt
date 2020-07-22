package com.wazh.shawn.cement

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebSettings.LayoutAlgorithm
import android.widget.Toast
import com.wazh.shawn.cement.common.CONST
import com.wazh.shawn.cement.common.MyApplication
import com.wazh.shawn.cement.util.CommonUtil
import com.wazh.shawn.cement.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer.*
import kotlinx.android.synthetic.main.layout_middle.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : BaseActivity(), OnClickListener {

    private var mExitTime: Long = 0 //记录点击完返回按钮后的long型时间

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyApplication.addDestoryActivity(this@MainActivity, "MainActivity")
        initWidget()
        initWebView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        ivBack.setImageResource(R.drawable.icon_menu)
        llBack.setOnClickListener(this)
        llMonitor.setOnClickListener(this)
        llWarning.setOnClickListener(this)
        llSetting.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle.text = title
        }

        val params: ViewGroup.LayoutParams = clDrawer.layoutParams
        params.width = CommonUtil.widthPixels(this)-CommonUtil.dip2px(this, 50.0f).toInt()
        clDrawer.layoutParams = params

        okHttpUserinfo()
        okHttpWarning()
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
        webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.loadUrl(url)
        webView.addJavascriptInterface(JSInterface(), "Android")

        webView.webChromeClient = object : WebChromeClient() {
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, itemUrl: String): Boolean {
                webView.loadUrl(itemUrl)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                cancelDialog()
            }
        }
    }

    /**
     * js调用android
     */
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

    /**
     * 控制抽屉
     */
    private fun setDrawer() {
        if (drawerlayout.isDrawerOpen(clDrawer)) {
            drawerlayout.closeDrawer(clDrawer)
        } else {
            drawerlayout.openDrawer(clDrawer)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                webView.canGoBack() -> {
                    webView.goBack()
                }
                drawerlayout.isDrawerOpen(clDrawer) -> drawerlayout.closeDrawer(clDrawer)
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
            R.id.llBack -> {
                setDrawer()
            }
            R.id.llMonitor -> {
                ivMonitor.setImageResource(R.drawable.icon_monitor_press)
                ivWarning.setImageResource(R.drawable.icon_warning)
                ivSetting.setImageResource(R.drawable.icon_setting)
                tvMonitor.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvWarning.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tvSetting.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                setDrawer()
            }
            R.id.llWarning -> {
                ivMonitor.setImageResource(R.drawable.icon_monitor)
                ivWarning.setImageResource(R.drawable.icon_warning_press)
                ivSetting.setImageResource(R.drawable.icon_setting)
                tvMonitor.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tvWarning.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvSetting.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                setDrawer()
            }
            R.id.llSetting -> {
                ivMonitor.setImageResource(R.drawable.icon_monitor)
                ivWarning.setImageResource(R.drawable.icon_warning)
                ivSetting.setImageResource(R.drawable.icon_setting_press)
                tvMonitor.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tvWarning.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tvSetting.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                val intent = Intent(this, SettingActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "系统设置")
                startActivity(intent)
            }
        }
    }

    /**
     * 获取用户信息
     */
    private fun okHttpUserinfo() {
        Thread(Runnable {
            val url = "${CONST.BASE_URL}/guns-cloud-system/entUser/getCurrentUser"
            OkHttpUtil.enqueue(Request.Builder().url(url).addHeader("Authorization", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("code")) {
                                val code = obj.getString("code")
                                if (TextUtils.equals(code, "200")) {
                                    if (!obj.isNull("data")) {
                                        val dataObj = obj.getJSONObject("data")
                                        if (!dataObj.isNull("account")) {
                                            tvUserName.text = dataObj.getString("account")
                                        }
                                        if (!dataObj.isNull("name")) {
                                            tvName.text = dataObj.getString("name")
                                        }
                                        if (!dataObj.isNull("sex")) {
                                            val sex = dataObj.getString("sex")
                                            if (TextUtils.equals(sex, "M")) {
                                                tvSex.text = "男"
                                            } else {
                                                tvSex.text = "女"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

    /**
     * 获取告警数
     */
    private fun okHttpWarning() {
        Thread(Runnable {
            val url = "${CONST.BASE_URL}/guns-cloud-cdsncioc/cdsnciockzjk/getIocAlarmQuantity"
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, "")
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("Authorization", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("code")) {
                                val code = obj.getString("code")
                                if (TextUtils.equals(code, "200")) {
                                    if (!obj.isNull("data")) {
                                        tvWarningCount.text = obj.getString("data")
                                        tvWarningCount.setBackgroundResource(R.drawable.bg_circle_red)
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

}
