package com.tools.android.translator.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityCameraBinding

/**
 * Created on 2022/4/22
 * Describe:
 */
class CameraActivity: BaseBindingActivity<ActivityCameraBinding>(), View.OnClickListener {

    override fun obtainBinding(): ActivityCameraBinding {
        return ActivityCameraBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivCamera.isSelected = true
            tvCamera.setTextColor(Color.parseColor("#FBB79F"))

            ivText.setOnClickListener(this@CameraActivity)
            tvText.setOnClickListener(this@CameraActivity)
            ivSetting.setOnClickListener(this@CameraActivity)
            tvSetting.setOnClickListener(this@CameraActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_text, R.id.tv_text -> finish()

            R.id.iv_setting, R.id.tv_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            }
        }
    }
}