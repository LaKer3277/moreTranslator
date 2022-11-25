package com.tools.android.translator.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.tencent.mmkv.MMKV
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivitySettingsBinding
import com.tools.android.translator.support.GpConsole
import com.tools.android.translator.support.setPoint
import com.tools.android.translator.ui.server.ConnectServerActivity

/**
 * Created on 2022/4/22
 * Describe:
 */
class SettingsActivity: BaseBindingActivity<ActivitySettingsBinding>(), View.OnClickListener {

    override fun obtainBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivSetting.isSelected = true
            tvSetting.setTextColor(Color.parseColor("#FBB79F"))

            ivText.setOnClickListener(this@SettingsActivity)
            tvText.setOnClickListener(this@SettingsActivity)
            ivCamera.setOnClickListener(this@SettingsActivity)
            tvCamera.setOnClickListener(this@SettingsActivity)
            serverLayout.setOnClickListener(this@SettingsActivity)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_text, R.id.tv_text -> finish()

            R.id.iv_camera, R.id.tv_camera -> {
                startActivity(Intent(this, CameraActivity::class.java))
                finish()
            }

            R.id.server_layout->{
                setPoint.point("itr_vpn_click")
                startActivity(Intent(this, ConnectServerActivity::class.java))
                finish()
            }

            R.id.tv_rate -> GpConsole.skip2Market(packageName)

            R.id.tv_privacy -> WebActivity.openPrivacy(this)

            R.id.tv_feedback -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("fgseiutu.2@gmail.com")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Title")
                intent.putExtra(Intent.EXTRA_TEXT, "")
                intent.type = "text/plain"
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(intent, "Choose Email App"))
                } else {
                    toastLong("Contact us by email: fgseiutu.2@gmail.com")
                }
            }
        }
    }
}