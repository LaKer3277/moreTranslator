package com.tools.android.translator.ui

import android.os.Bundle
import com.tools.android.translator.base.BaseBindingActivity
import com.tools.android.translator.databinding.ActivitySubscribeBinding
import com.tools.android.translator.gp.GoogleBillingManager

class SubscribeActivity: BaseBindingActivity<ActivitySubscribeBinding>() {
    override fun obtainBinding(): ActivitySubscribeBinding {
        return ActivitySubscribeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.llcMonth.setOnClickListener {GoogleBillingManager.onQuerySkuDetailsAsync("vip_month",this) }
        binding.llcYear.setOnClickListener {GoogleBillingManager.onQuerySkuDetailsAsync("vip_year",this) }
        binding.llcTry.setOnClickListener {GoogleBillingManager.onQuerySkuDetailsAsync("vip_month2",this) }
    }
}