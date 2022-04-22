package com.tools.android.translator.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivitySettingsBinding

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
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_text, R.id.tv_text -> finish()

            R.id.iv_camera, R.id.tv_camera -> {
                startActivity(Intent(this, CameraActivity::class.java))
                finish()
            }
        }
    }
}