package com.tools.android.translator.gp;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

import java.util.List;

public interface GoogleBillingListener {
    /**
     * 购买监听
     *
     * @param result
     * @param purchases
     */
    default void onPurchasesUpdated(BillingResult result, List<Purchase> purchases) {

    }

    /**
     * 查询商品详情成功
     *
     * @param list
     */
    default void onProductDetailsSus(List<ProductDetails> list) {

    }

    /**
     * 商品消费成功
     *
     * @param purchaseToken
     */
    default void onConsumeSus(String purchaseToken) {

    }
}
