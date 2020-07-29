package com.wazh.shawn.cement

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.wazh.shawn.cement.common.CONST
import com.wazh.shawn.cement.common.MyApplication
import com.wazh.shawn.cement.util.CommonUtil
import com.wazh.shawn.cement.util.OkHttpUtil
import com.wazh.shawn.cement.util.sofia.Sofia
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 登录
 */
class LoginActivity : BaseActivity(), OnClickListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)
		Sofia.with(this)
				.invasionStatusBar() //设置顶部状态栏缩进
				.statusBarBackground(Color.TRANSPARENT) //设置状态栏颜色
				.invasionNavigationBar()
				.navigationBarBackground(Color.TRANSPARENT)
		initWidget()
	}

	/**
	 * 初始化控件
	 */
	private fun initWidget() {
		tvForgetUser.setOnClickListener(this)
		tvForgetPwd.setOnClickListener(this)
		tvLogin.setOnClickListener(this)
		tvRegister.setOnClickListener(this)

		val params = ivIcon.layoutParams
		params.width = CommonUtil.widthPixels(this)/3
		params.height = CommonUtil.widthPixels(this)/3
		ivIcon.layoutParams = params
	}

	/**
	 * 登录
	 */
	private fun okHttpLogin() {
		if (TextUtils.isEmpty(etUserName!!.text.toString())) {
			Toast.makeText(this, "请输入您的账号", Toast.LENGTH_SHORT).show()
			return
		}
		if (TextUtils.isEmpty(etPwd.text.toString())) {
			Toast.makeText(this, "请输入您的密码", Toast.LENGTH_SHORT).show()
			return
		}
		showDialog()
		val url = "${CONST.BASE_URL}/reLogin/loginToken"
		val builder = FormBody.Builder()
		builder.add("account", etUserName!!.text.toString())
		builder.add("password", CommonUtil.encrypt(etPwd.text.toString(), "SHA-256"))
		builder.add("clientId", CONST.CLIENDID)
		val body: RequestBody = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}

				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
//					if (!response.isSuccessful) {
//						return
//					}
					val result = response.body!!.string()
					runOnUiThread {
						cancelDialog()
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								if (!obj.isNull("code")) {
									val code = obj.getString("code")
									if (TextUtils.equals(code, "200")) {
										MyApplication.USERNAME = etUserName!!.text.toString()
										MyApplication.PASSWORD = etPwd.text.toString()
										if (!obj.isNull("data")) {
											MyApplication.TOKEN = obj.getString("data")
										}
										MyApplication.saveUserInfo(this@LoginActivity)
										val intent = Intent(this@LoginActivity, MainActivity::class.java)
										intent.putExtra(CONST.ACTIVITY_NAME, "测试")
										intent.putExtra(CONST.WEB_URL, "http://192.168.1.205:1234/Sandcastle_lightWeight2.0/demo/examples/default/if_ioc_map.html")
										startActivity(intent)
										finish()
									} else {
										if (!obj.isNull("message")) {
											Toast.makeText(this@LoginActivity, obj.getString("message"), Toast.LENGTH_SHORT).show()
										}
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

	override fun onClick(v: View) {
		when (v.id) {
//			R.id.tvForgetUser -> startActivityForResult(Intent(this, ForgetUsernameActivity::class.java), 1002)
//			R.id.tvForgetPwd -> startActivity(Intent(this, ForgetPwdActivity::class.java))
			R.id.tvLogin -> okHttpLogin()
//			R.id.tvRegister -> startActivityForResult(Intent(this, RegisterActivity::class.java), 1001)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			when (requestCode) {
				1001 -> {
					if (data != null) {
						if (data.extras != null) {
							val bundle = data.extras
							val userName = bundle.getString("userName")
							val pwd = bundle.getString("pwd")
							etUserName!!.setText(userName)
							etPwd.setText(pwd)
							okHttpLogin()
						}
					}
				}
				1002 -> {
					if (data != null) {
						if (data.extras != null) {
							val bundle = data.extras
							val userName = bundle.getString("userName")
							etUserName!!.setText(userName)
						}
					}
				}
			}
		}
	}

}
