package com.tools.android.translator.ui

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


}