package com.wazh.shawn.cement

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.wazh.shawn.cement.common.CONST
import com.wazh.shawn.cement.common.MyApplication
import com.wazh.shawn.cement.util.CommonUtil
import com.wazh.shawn.cement.util.DataCleanManager
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_cache.view.*
import kotlinx.android.synthetic.main.layout_title.*

class SettingActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        MyApplication.addDestoryActivity(this@SettingActivity, "SettingActivity")
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        llPwd.setOnClickListener(this)
        llClearCache.setOnClickListener(this)
        llVersion.setOnClickListener(this)
        tvLogout.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        tvVersion.text = CommonUtil.getVersion(this)
        getCache()
    }

    private fun getCache() {
        try {
            tvCache.text = DataCleanManager.getCacheSize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除缓存
     */
    private fun dialogCache() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.dialog_cache, null)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvContent.text = "确定清除缓存？"
        view.tvNegtive.setOnClickListener { dialog.dismiss() }
        view.tvPositive.setOnClickListener {
            dialog.dismiss()
            DataCleanManager.clearCache(this)
            getCache()
        }
    }

    /**
     * 登出对话框
     */
    private fun dialogLogout() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.dialog_cache, null)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvContent.text = "确定要退出登录？"
        view.tvNegtive.setOnClickListener { dialog.dismiss() }
        view.tvPositive.setOnClickListener {
            dialog.dismiss()
            MyApplication.clearUserInfo(this)
            MyApplication.destoryActivity()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
            R.id.llPwd -> startActivity(Intent(this, ModifyPwdActivity::class.java))
            R.id.llClearCache -> dialogCache()
            R.id.tvLogout -> dialogLogout()
        }
    }

}