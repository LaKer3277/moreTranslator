package com.tools.android.translator.gp

import android.content.Context
import android.content.Intent
import com.android.billingclient.api.*
import com.tools.android.translator.base.BaseActivity
import com.tools.android.translator.dialog.OpenSubSuccessDialog
import com.tools.android.translator.ui.SubscribeActivity

object GoogleBillingManager{
    var hasOpenSub=true
    private var baseActivity:BaseActivity?=null
    var billingClient: BillingClient? = null
    private val billHelper=GoogleBillHelper()


    private var billingListener=object :GoogleBillingListener{
        override fun onProductDetailsSus(list: MutableList<ProductDetails>?) {
            super.onProductDetailsSus(list)
            if(null==list||list.isEmpty()||null== baseActivity){
                return
            }
            billHelper.onOpenGooglePlay(this, baseActivity,list.first())
        }

        override fun onPurchasesUpdated(result: BillingResult?, purchases: MutableList<Purchase>?) {
            super.onPurchasesUpdated(result, purchases)
            if(null==purchases||purchases.isEmpty()){
                return
            }
            processPurchaseList(purchases)
            if(hasOpenSub&&null!= baseActivity){
                baseActivity?.let {base->
                    base.runOnUiThread {
                        OpenSubSuccessDialog().show(base.supportFragmentManager,"OpenSubSuccessDialog")
                    }
                }
            }
        }
    }

    fun createClient(context: Context?) {
        if (null == context) {
            return
        }
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                if (null != billingListener) {
                    billingListener!!.onPurchasesUpdated(billingResult, purchases)
                }
            }
            .build()
        //启动支付连接
        startConn()
    }

//    fun setBillingListener(billingListener: GoogleBillingListener?) {
//        this.billingListener = billingListener
//    }

    val isReady: Boolean
        get() = !(null == billingClient || !billingClient!!.isReady)

    private fun startConn() {
        if (isReady) {
            return
        }
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryPurchases(){
        billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS,object :PurchasesResponseListener{
            override fun onQueryPurchasesResponse(p0: BillingResult, p1: MutableList<Purchase>) {
                if(p0.responseCode==BillingClient.BillingResponseCode.OK){
                    processPurchaseList(p1)
                }
            }
        })
    }

    private fun processPurchaseList(list:MutableList<Purchase>){
        for (purchase in list) {
            if(purchase.isAutoRenewing){
                hasOpenSub=true
                break
            }
        }
    }

    fun onQuerySkuDetailsAsync(id:String, baseActivity: BaseActivity){
        if(hasOpenSub){
            return
        }
        this.baseActivity=baseActivity
        billHelper.onQuerySkuDetailsAsync(billingListener,BillingClient.ProductType.SUBS,id)
    }

    fun jumpToSubAc(baseActivity: BaseActivity){
        baseActivity.startActivity(Intent(baseActivity, SubscribeActivity::class.java))
    }

    fun endConn() {
        billingClient?.endConnection()
    }
}