package com.tools.android.translator.ui

import android.graphics.Color
import android.os.Bundle
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityCameraBinding

/**
 * Created on 2022/4/22
 * Describe:
 */
class ActivityCamera: BaseBindingActivity<ActivityCameraBinding>() {

    override fun obtainBinding(): ActivityCameraBinding {
        return ActivityCameraBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivCamera.isSelected = true
            tvCamera.setTextColor(Color.parseColor("#FBB79F"))
        }
    }
}