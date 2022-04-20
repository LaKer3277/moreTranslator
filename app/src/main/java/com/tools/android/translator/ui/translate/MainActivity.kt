package com.tools.android.translator.ui.translate

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivityMainBinding

class MainActivity : BaseBindingActivity<ActivityMainBinding>() {

    override fun obtainBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var mTranslateModel: TranslateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTranslateModel = ViewModelProvider(this).get(TranslateViewModel::class.java)

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

}