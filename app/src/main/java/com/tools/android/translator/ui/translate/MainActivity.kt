package com.tools.android.translator.ui.translate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.tools.android.translator.R
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityMainBinding
import com.tools.android.translator.ui.CameraActivity
import com.tools.android.translator.ui.SettingsActivity

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

            ivCamera.setOnClickListener(this@MainActivity)
            tvCamera.setOnClickListener(this@MainActivity)
            ivSetting.setOnClickListener(this@MainActivity)
            tvSetting.setOnClickListener(this@MainActivity)
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

        // Update sync toggle button states based on downloaded models list.
        mTranslateModel.availableModels.observe(
            this,
            { translateRemoteModels ->
                val output = getString(
                    R.string.downloaded_models_label,
                    translateRemoteModels
                )
                /*downloadedModelsTextView.text = output
                sourceSyncButton.isChecked =
                    translateRemoteModels!!.contains(
                        adapter.getItem(sourceLangSelector.selectedItemPosition)!!.code
                    )
                targetSyncButton.isChecked = translateRemoteModels.contains(
                    adapter.getItem(targetLangSelector.selectedItemPosition)!!.code
                )*/
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

            R.id.iv_camera, R.id.tv_camera -> startActivity(Intent(this, CameraActivity::class.java))

            R.id.iv_setting, R.id.tv_setting -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}