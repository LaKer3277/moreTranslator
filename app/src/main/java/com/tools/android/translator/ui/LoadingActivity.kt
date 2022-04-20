package com.tools.android.translator.ui

import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityLoadingBinding

/**
 * Created on 2022/4/20
 * Describe:
 */
class LoadingActivity: BaseBindingActivity<ActivityLoadingBinding>() {

    override fun obtainBinding(): ActivityLoadingBinding {
        return ActivityLoadingBinding.inflate(layoutInflater)
    }


}