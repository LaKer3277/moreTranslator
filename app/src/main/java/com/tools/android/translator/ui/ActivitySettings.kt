package com.tools.android.translator.ui

import android.graphics.Color
import android.os.Bundle
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivitySettingsBinding

/**
 * Created on 2022/4/22
 * Describe:
 */
class ActivitySettings: BaseBindingActivity<ActivitySettingsBinding>() {

    override fun obtainBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivSetting.isSelected = true
            tvSetting.setTextColor(Color.parseColor("#FBB79F"))
        }
    }
}