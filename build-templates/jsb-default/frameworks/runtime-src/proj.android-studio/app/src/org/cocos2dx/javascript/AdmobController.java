package org.cocos2dx.javascript;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import org.riserise.firingballs.R;

import java.util.Arrays;
import java.util.Collections;



public class AdmobController {
    //廣告參數
    public static final String TEST_DEVICE_HASHED_ID = "33209807812E37C2BB562BF3D0FB1EC9";//测试设备ID
    final static String ad_banner_bottom_ID = "ca-app-pub-6735461610773254/4824883703";//横幅广告
    final static String ad_interstitial_ID = "ca-app-pub-6735461610773254/5752604955";//插页式激励广告
    final static String ad_rewarded_ID = "ca-app-pub-6735461610773254/8284960027";//激励广告



    static String LOG_TAG = "AdmobController";
    private static AdmobController instance;
    private static GoogleMobileAdsConsentManager googleMobileAdsConsentManager;

    public static AppActivity appActivity;
    private AdView bannerAdView;
    private RewardedInterstitialAd rewardedInterstitialAd;
    private RewardedAd rewardedAd;
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
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
    }

    public void onResume() {
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
    }

    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
    }

    //视频广告奖励发放回调给游戏层
    private void rewardADEarnedCallback(final String earnedRewardCallback){
        Cocos2dxHelper.runOnGLThread(() -> {
            String jsCode = String.format("%s();", earnedRewardCallback);
            Cocos2dxJavascriptJavaBridge.evalString(jsCode);
        });
    }

    //显示全屏激励视频广告
    public static boolean showRewardedAd(final String earnedRewardCallback){
        if(null == instance.rewardedAd){
            Log.d(LOG_TAG, "rewardedAd: ad is null");
            new Thread(
                    () -> {
                        appActivity.runOnUiThread(instance::loadRewardedAd);
                    })
                    .start();
            return false;
        }

        // [START show_ad]
        appActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // [START show_ad]
                instance.rewardedAd.show(
                        appActivity,
                        new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                Log.d(LOG_TAG, "RewardedAd: User earned the reward.");
                                instance.rewardADEarnedCallback(earnedRewardCallback);
                            }
                        });
                instance.rewardedAd = null;
                new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedAd(), 2000);
                // [END show_ad]
            }
        });
        // [END show_ad]
        return true;
    }

    //显示全屏插页式激励视频广告
    public static boolean showRewardedInterstitialAd(final String earnedRewardCallback){
        if(null == instance.rewardedInterstitialAd){
            Log.d(LOG_TAG, "RewardedInterstitialAd: ad is null");
            new Thread(
                    () -> {
                        appActivity.runOnUiThread(instance::loadRewardedInterstitialAd);
                    })
                    .start();
            return false;
        }

        // [START show_ad]
        appActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                instance.rewardedInterstitialAd.show(
                        appActivity,
                        new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                Log.d(LOG_TAG, "RewardedInterstitialAd: The user earned the reward.");
                                instance.rewardADEarnedCallback(earnedRewardCallback);
                            }
                        });
                instance.rewardedInterstitialAd = null;
                new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedInterstitialAd(), 2000);
            }
        });
        // [END show_ad]
        return true;
    }


    //load横幅广告
    private void loadBanner() {
        // [START create_ad_view]
        // Create a new ad view.
        if (bannerAdView == null) {
            bannerAdView = new AdView(appActivity);
            bannerAdView.setAdListener(
                    new AdListener() {
                        @Override
                        public void onAdClicked() {
                            // Code to be executed when the user clicks on an ad.
                            Log.d(LOG_TAG, "bannerAd onAdClicked: ");
                        }

                        @Override
                        public void onAdClosed() {
                            // Code to be executed when the user is about to return
                            // to the app after tapping on an ad.
                            Log.d(LOG_TAG, "bannerAd onAdClosed: ");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            // Code to be executed when an ad request fails.
                            Log.d(LOG_TAG, "bannerAd onAdFailedToLoad: ");
                            loadBanner();
                        }

                        @Override
                        public void onAdImpression() {
                            // Code to be executed when an impression is recorded
                            // for an ad.
                            Log.d(LOG_TAG, "bannerAd onAdImpression: ");
                        }

                        @Override
                        public void onAdLoaded() {
                            // Code to be executed when an ad finishes loading.
                            Log.d(LOG_TAG, "bannerAd onAdLoaded: ");
                        }

                        @Override
                        public void onAdOpened() {
                            // Code to be executed when an ad opens an overlay that
                            // covers the screen.
                            Log.d(LOG_TAG, "bannerAd onAdOpened: ");
                        }
                    });
        }

        bannerAdView.setAdUnitId(ad_banner_bottom_ID);
        // [START set_ad_size]

        Display display = appActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;
        float adWidthPixels = outMetrics.widthPixels;
        int adWidth = (int) (adWidthPixels / density);
        bannerAdView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(appActivity, adWidth));
        // [END set_ad_size]

        // Replace ad container with new ad view.
//        adContainerView.removeAllViews();
//        adContainerView.addView(bannerAdView);
        // [END create_ad_view]
        // 将广告覆盖在Cocos视图上方（底部）
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        appActivity.addContentView(bannerAdView, params); // ✅ 不替换游戏画面，只叠加广告层

        // [START load_ad]
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        // [END load_ad]
    }

    //load全屏激励广告
    private void loadRewardedAd(){
        if (rewardedAd != null) {
            return;
        }

        // [START load_ad]
        RewardedAd.load(
                appActivity,
                ad_rewarded_ID,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        Log.d(LOG_TAG, "RewardedAd was loaded.");
                        rewardedAd = ad;

                        // [START set_content_callback]
                        rewardedAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d(LOG_TAG, "RewardedAd was dismissed.");
                                // Don't forget to set the ad reference to null so you
                                // don't show the ad a second time.
                                rewardedAd = null;
                                new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedAd(), 2000);

                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d(LOG_TAG, "RewardedAd failed to show.");
                                // Don't forget to set the ad reference to null so you
                                // don't show the ad a second time.
                                rewardedAd = null;
                                new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedAd(), 2000);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                Log.d(LOG_TAG, "RewardedAd showed fullscreen content.");
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(LOG_TAG, "RewardedAd recorded an impression.");
                            }

                            @Override
                            public void onAdClicked() {
                                // Called when an ad is clicked.
                                Log.d(LOG_TAG, "RewardedAd was clicked.");
                            }
                        });
                        // [END set_content_callback]
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(LOG_TAG, "RewardedAd onAdFailedToLoad: "+loadAdError.getMessage());
                        rewardedAd = null;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedAd(), 2000);
                    }
                });
        // [END load_ad]
    }

    //load插页式激励广告
    private void loadRewardedInterstitialAd(){
        // If the ad is already loaded, don't try to load it again.
        if (rewardedInterstitialAd != null) {
            return;
        }

        // [START load_ad]
        RewardedInterstitialAd.load(
                appActivity,
                ad_interstitial_ID,
                new AdRequest.Builder().build(),
                new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                        Log.d(LOG_TAG, "RewardedInterstitialAd was loaded.");
                        rewardedInterstitialAd = ad;

                        // [START set_content_callback]
                        rewardedInterstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        Log.d(LOG_TAG, "The RewardedInterstitialAd was dismissed.");
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        rewardedInterstitialAd = null;
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedInterstitialAd(), 2000);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        Log.d(LOG_TAG, "The RewardedInterstitialAd failed to show: " + adError.getMessage());
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        rewardedInterstitialAd = null;
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedInterstitialAd(), 2000);
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d(LOG_TAG, "The RewardedInterstitialAd was shown.");
                                    }

                                    @Override
                                    public void onAdImpression() {
                                        // Called when an impression is recorded for an ad.
                                        Log.d(LOG_TAG, "The RewardedInterstitialAd recorded an impression.");
                                    }

                                    @Override
                                    public void onAdClicked() {
                                        // Called when ad is clicked.
                                        Log.d(LOG_TAG, "The RewardedInterstitialAd was clicked.");
                                    }
                                });
                        // [END set_content_callback]
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(LOG_TAG, "RewardedInterstitialAd onAdFailedToLoad: " + loadAdError.getMessage());
                        rewardedInterstitialAd = null;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> instance.loadRewardedInterstitialAd(), 2000);
                    }
                });
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
                    appActivity.runOnUiThread(this::loadRewardedInterstitialAd);
                    appActivity.runOnUiThread(this::loadRewardedAd);
                    // [END_EXCLUDE]
                })
                .start();
        // [END initialize_sdk]
    }
}
