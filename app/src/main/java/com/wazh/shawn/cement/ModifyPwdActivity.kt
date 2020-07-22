package com.wazh.shawn.cement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.wazh.shawn.cement.common.CONST
import com.wazh.shawn.cement.common.MyApplication
import com.wazh.shawn.cement.util.OkHttpUtil
import kotlinx.android.synthetic.main.activity_modify_pwd.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

/**
 * 修改密码
 */
class ModifyPwdActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_pwd)
        initWidget()
    }

    private fun initWidget() {
        tvTitle.text = "修改密码"
        llBack.setOnClickListener(this)
        tvLogin.setOnClickListener(this)
    }

    /**
     * 修改密码
     */
    private fun okHttpPwd() {
        if (TextUtils.isEmpty(etPwd.text.toString())) {
            Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(etNewPwd.text.toString())) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(etPwdConfirm.text.toString())) {
            Toast.makeText(this, "请再次输入新密码", Toast.LENGTH_SHORT).show()
            return
        }
        if (!TextUtils.equals(etNewPwd.text.toString(), etPwdConfirm.text.toString())) {
            Toast.makeText(this, "再次输入的新密码不一致", Toast.LENGTH_SHORT).show()
            return
        }
        showDialog()
        Thread(Runnable {
            val url = "${CONST.BASE_URL}/guns-cloud-system/entUser/updatePassword"
            val param  = JSONObject()
            param.put("oldPassword", etPwd.text.toString())
            param.put("newPassword", etNewPwd.text.toString())
            param.put("repeatPassword", etNewPwd.text.toString())
            val json : String = param.toString()
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("Authorization", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
//                    if (!response.isSuccessful) {
//                        return
//                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("code")) {
                                val code = obj.getString("code")
                                if (TextUtils.equals(code, "200")) {
                                    Toast.makeText(this@ModifyPwdActivity, "修改密码成功", Toast.LENGTH_SHORT).show()
                                    MyApplication.clearUserInfo(this@ModifyPwdActivity)
                                    MyApplication.destoryActivity()
                                    startActivity(Intent(this@ModifyPwdActivity, LoginActivity::class.java))
                                    finish()
                                } else {
                                    if (!obj.isNull("message")) {
                                        Toast.makeText(this@ModifyPwdActivity, obj.getString("message"), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
            R.id.tvLogin -> okHttpPwd()
        }
    }

}