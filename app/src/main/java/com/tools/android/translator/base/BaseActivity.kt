package com.tools.android.translator.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Created on 2022/4/20
 * Describe:
 */
open class BaseActivity: AppCompatActivity() {

    private var _isPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _isPause = false
    }

    override fun onStart() {
        super.onStart()
        _isPause = false
    }

    override fun onResume() {
        super.onResume()
        _isPause = false
    }

    override fun onPause() {
        super.onPause()
        _isPause = true
    }

    override fun onStop() {
        super.onStop()
        _isPause = true
    }

    open fun isPaused(): Boolean {
        return _isPause
    }
}