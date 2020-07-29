package com.wazh.shawn.cement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import com.wazh.shawn.cement.common.CONST
import com.wazh.shawn.cement.common.MyApplication
import com.wazh.shawn.cement.util.CommonUtil
import com.wazh.shawn.cement.util.OkHttpUtil
import com.wazh.shawn.cement.util.sofia.Sofia
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 欢迎界面
 */
class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        Sofia.with(this)
                .invasionStatusBar() //设置顶部状态栏缩进
                .statusBarBackground(Color.TRANSPARENT) //设置状态栏颜色
                .invasionNavigationBar()
                .navigationBarBackground(Color.TRANSPARENT)

        //点击Home键后再点击APP图标，APP重启而不是回到原来界面
        if (!isTaskRoot) {
            finish()
            return
        }
        //点击Home键后再点击APP图标，APP重启而不是回到原来界面
        Handler().postDelayed({
            if (!TextUtils.isEmpty(MyApplication.USERNAME) && !TextUtils.isEmpty(MyApplication.PASSWORD)) {
                okHttpLogin()
            } else {
                startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
                finish()
            }
        }, 2000)
    }

    /**
     * 登录
     */
    private fun okHttpLogin() {
        val url = "${CONST.BASE_URL}/reLogin/loginToken"
        val builder = FormBody.Builder()
        builder.add("account", MyApplication.USERNAME)
        builder.add("password", CommonUtil.encrypt(MyApplication.PASSWORD, "SHA-256"))
        builder.add("clientId", CONST.CLIENDID)
        val body: RequestBody = builder.build()
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("e", e.message);
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("code")) {
                                    val code = obj.getString("code")
                                    if (TextUtils.equals(code, "200")) {
                                        if (!obj.isNull("data")) {
                                            MyApplication.TOKEN = obj.getString("data")
                                        }
                                        MyApplication.saveUserInfo(this@WelcomeActivity)
                                        val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                                        intent.putExtra(CONST.ACTIVITY_NAME, "测试")
                                        intent.putExtra(CONST.WEB_URL, "http://192.168.1.205:1234/Sandcastle_lightWeight2.0/demo/examples/default/if_ioc_map.html")
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
                                        finish()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }

    override fun onKeyDown(KeyCode: Int, event: KeyEvent?): Boolean {
        return if (KeyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else super.onKeyDown(KeyCode, event)
    }
	
}
