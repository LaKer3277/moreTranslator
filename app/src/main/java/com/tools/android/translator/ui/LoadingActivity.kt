package com.tools.android.translator.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityLoadingBinding
import com.tools.android.translator.ui.translate.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created on 2022/4/20
 * Describe:
 */
class LoadingActivity: BaseBindingActivity<ActivityLoadingBinding>() {

    override fun obtainBinding(): ActivityLoadingBinding {
        return ActivityLoadingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(2800L)
            if (isPaused()) return@launch
            enterMain()
        }
    }

    private fun enterMain() {
         val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}