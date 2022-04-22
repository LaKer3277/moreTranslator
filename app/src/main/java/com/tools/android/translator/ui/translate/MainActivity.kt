package com.tools.android.translator.ui.translate

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityMainBinding

class MainActivity : BaseBindingActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun obtainBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mTranslateModel: TranslateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.includeNav.apply {
            ivText.isSelected = true
            tvText.setTextColor(Color.parseColor("#FBB79F"))
        }
        mTranslateModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(TranslateViewModel::class.java)

        mTranslateModel.translatedText.observe(
            this,
            { resultOrError ->
                if (resultOrError.error != null) {
                    //srcTextView.setError(resultOrError.error!!.localizedMessage)
                } else {
                    //targetTextView.text = resultOrError.result
                }
            }
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bg_source -> {
                binding.languagePanel.root.expand()
            }

            R.id.bg_target -> {
                binding.languagePanel.root.expand()
            }
        }
    }
}