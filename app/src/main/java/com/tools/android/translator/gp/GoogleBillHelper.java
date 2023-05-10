package com.tools.android.translator.gp;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class GoogleBillHelper {
    public static final String TAG = GoogleBillHelper.class.getSimpleName();

    public void onQuerySkuDetailsAsync(GoogleBillingListener billingListener, String productType, String... productIds) {
        if (null == productIds || productIds.length == 0
                || !GoogleBillingManager.INSTANCE.isReady()
        ) {
            return;
        }
        List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
        for (String productId : productIds) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams
                    .Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(productType)
                    .build();
            //添加对应的 产品id 去查询详情
            skuList.add(product);
        }

        QueryProductDetailsParams params = QueryProductDetailsParams
                .newBuilder()
                .setProductList(skuList)
                .build();

        GoogleBillingManager.INSTANCE.getBillingClient().queryProductDetailsAsync(params, (billingResult, list) -> {
            if (BillingClient.BillingResponseCode.OK == billingResult.getResponseCode()) {
                if (null != billingListener) {
                    billingListener.onProductDetailsSus(list);
                }
            }
        });
    }

    public void onOpenGooglePlay(GoogleBillingListener billingListener, Activity activity, ProductDetails details) {
        if (null == details) {
            return;
        }
        List<BillingFlowParams.ProductDetailsParams> params = new ArrayList<>();
        //添加购买数据
        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams
                .newBuilder()
                .setProductDetails(details)
                .build();
        params.add(productDetailsParams);

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(params)
                .build();
        //响应code 码
        int responseCode = GoogleBillingManager.INSTANCE.getBillingClient().launchBillingFlow(activity, billingFlowParams).getResponseCode();
//        //成功换起
//        if (BillingClient.BillingResponseCode.OK == responseCode) {
//            //添加购买监听
//            GoogleBillingManager.INSTANCE.setBillingListener(billingListener);
//        }
    }

    public void onConsumeAsync(GoogleBillingListener billingListener, Purchase purchase) {
        if (!GoogleBillingManager.INSTANCE.isReady()) {
            return;
        }
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (null != billingListener) {
                    billingListener.onConsumeSus(purchaseToken);
                } else {
                    Log.e(TAG, "消费失败 code : " + billingResult.getResponseCode() + " message : " + billingResult.getDebugMessage());
                }
            }
        };
        GoogleBillingManager.INSTANCE.getBillingClient().consumeAsync(consumeParams, listener);
    }
}
