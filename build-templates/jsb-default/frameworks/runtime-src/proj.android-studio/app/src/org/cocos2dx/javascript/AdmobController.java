package org.cocos2dx.javascript;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import org.riserise.firingballs.R;

import java.util.Arrays;
import java.util.Collections;



public class AdmobController {
    //廣告參數
    public static final String TEST_DEVICE_HASHED_ID = "ABCDEF012345";
    final static String ad_banner_bottom_ID = "ca-app-pub-3940256099942544/9214589741";


    static String LOG_TAG = "AdmobController";
    private static AdmobController instance;
    private static GoogleMobileAdsConsentManager googleMobileAdsConsentManager;

    public static AppActivity appActivity;
    private AdView adView;
    private FrameLayout adContainerView;
    private boolean isMobileAdsInitializeCalled = false;


    public static AdmobController getInstance() {
        if (instance == null) {
            instance = new AdmobController();
        }
        return instance;
    }

    public void initAdmob(){
        // Log the Mobile Ads SDK version.
        Log.d(LOG_TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        // 获取布局中的容器
//        appActivity.setContentView(R.layout.activity_main);
//        FrameLayout gameContainer = appActivity.findViewById(R.id.game_container);
//        gameContainer.removeAllViews();
//        gameContainer.addView(appActivity.getGLSurfaceView());
//        adContainerView = appActivity.findViewById(R.id.ad_view_container);

        googleMobileAdsConsentManager =
                GoogleMobileAdsConsentManager.getInstance(appActivity.getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(
                appActivity,
                consentError -> {
                    if (consentError != null) {
                        // Consent not obtained in current session.
                        Log.w(LOG_TAG,
                                String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                    }

                    if (googleMobileAdsConsentManager.canRequestAds()) {
                        initializeMobileAdsSdk();
                    }

                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                        // Regenerate the options menu to include a privacy setting.
                        appActivity.invalidateOptionsMenu();
                    }
                });

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk();
        }
    }



//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        View menuItemView = appActivity.findViewById(item.getItemId());
//        PopupMenu popup = new PopupMenu(appActivity, menuItemView);
//        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
//        popup
//                .getMenu()
//                .findItem(R.id.privacy_settings)
//                .setVisible(googleMobileAdsConsentManager.isPrivacyOptionsRequired());
//        popup.show();
//        popup.setOnMenuItemClickListener(
//                popupMenuItem -> {
//                    if (popupMenuItem.getItemId() == R.id.privacy_settings) {
//                        // Handle changes to user consent.
//                        googleMobileAdsConsentManager.showPrivacyOptionsForm(
//                                appActivity,
//                                formError -> {
//                                    if (formError != null) {
//                                        Toast.makeText(appActivity, formError.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                        return true;
//                    } else if (popupMenuItem.getItemId() == R.id.ad_inspector) {
//                        MobileAds.openAdInspector(
//                                appActivity,
//                                error -> {
//                                    // Error will be non-null if ad inspector closed due to an error.
//                                    if (error != null) {
//                                        Toast.makeText(appActivity, error.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                        return true;
//                    }
//                    return false;
//                });
//        return super.onOptionsItemSelected(item);
//    }


    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void onResume() {
        if (adView != null) {
            adView.resume();
        }
    }

    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
    }

    private void loadBanner() {
        // [START create_ad_view]
        // Create a new ad view.
        adView = new AdView(appActivity);
        if (adView != null) {
            adView.setAdListener(
                    new AdListener() {
                        @Override
                        public void onAdClicked() {
                            // Code to be executed when the user clicks on an ad.
                            Log.d(LOG_TAG, "AdListener onAdClicked: ");
                        }

                        @Override
                        public void onAdClosed() {
                            // Code to be executed when the user is about to return
                            // to the app after tapping on an ad.
                            Log.d(LOG_TAG, "AdListener onAdClosed: ");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            // Code to be executed when an ad request fails.
                            Log.d(LOG_TAG, "AdListener onAdFailedToLoad: ");
                        }

                        @Override
                        public void onAdImpression() {
                            // Code to be executed when an impression is recorded
                            // for an ad.
                            Log.d(LOG_TAG, "AdListener onAdImpression: ");
                        }

                        @Override
                        public void onAdLoaded() {
                            // Code to be executed when an ad finishes loading.
                            Log.d(LOG_TAG, "AdListener onAdLoaded: ");
                        }

                        @Override
                        public void onAdOpened() {
                            // Code to be executed when an ad opens an overlay that
                            // covers the screen.
                            Log.d(LOG_TAG, "AdListener onAdOpened: ");
                        }
                    });
        }

        adView.setAdUnitId(ad_banner_bottom_ID);
        // [START set_ad_size]
        // Request an anchored adaptive banner with a width of 360.
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(appActivity, 360));
        // [END set_ad_size]

        // Replace ad container with new ad view.
//        adContainerView.removeAllViews();
//        adContainerView.addView(adView);
        // [END create_ad_view]
        // 将广告覆盖在Cocos视图上方（底部）
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        appActivity.addContentView(adView, params); // ✅ 不替换游戏画面，只叠加广告层

        // [START load_ad]
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        // [END load_ad]
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled) {
            return;
        }
        isMobileAdsInitializeCalled=true;

        // Set your test devices.
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(Collections.singletonList(TEST_DEVICE_HASHED_ID))
                        .build());

        // [START initialize_sdk]
        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(appActivity, initializationStatus -> {});
                    // [START_EXCLUDE silent]
                    // Load an ad on the main thread.
                    appActivity.runOnUiThread(this::loadBanner);
                    // [END_EXCLUDE]
                })
                .start();
        // [END initialize_sdk]
    }
}
