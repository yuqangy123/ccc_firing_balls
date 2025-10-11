package org.cocos2dx.javascript;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.appsflyer.AppsFlyerConversionListener;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import android.content.ClipboardManager;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import com.appsflyer.AppsFlyerLib;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;



public class AppActivity extends Cocos2dxActivity {
    static String channel_id = "240001";
    static String androidId = "";
    public static AppActivity instance = null;
    private PowerManager.WakeLock mWakeLock;
    public static Vibrator myVibrator;
    static String LOG_TAG = "AppActivity";
    static String Sylvia="Sylvia";
    private static final String AF_DEV_KEY = "49c9iumpbi4g";
    public static boolean bAFNonOrganic = false;
    public static String conversion_json_data = "";
    private static boolean af_or_ad = true; //true表示使用af 格局key 长度判断

    private static Map<String, String> ad_event_map = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            return;
        }
        instance=this;

        AdmobController.appActivity = instance;
        AdmobController.getInstance().initAdmob();

        af_or_ad = AF_DEV_KEY.length() > 15;

        if (af_or_ad) {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {

                /* Returns the attribution data. Note - the same conversion data is returned every time per install */
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    for (String attrName : conversionData.keySet()) {
                        Log.d(LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                    }
                    setInstallData(conversionData);
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
                }

                /* Called only when a Deep Link is opened */
                @Override
                public void onAppOpenAttribution(Map<String, String> conversionData) {
                    for (String attrName : conversionData.keySet()) {
                        Log.d(LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                    }
                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
                }
            };
            AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, this);
            AppsFlyerLib.getInstance().start(this);
//          AppsFlyerLib.getInstance().setDebugLog(true);
        }
        else {
            String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
            AdjustConfig config = new AdjustConfig(this, AF_DEV_KEY, environment);
            config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
                @Override
                public void onAttributionChanged(AdjustAttribution attribution) {
                    Log.d(LOG_TAG, "attribute: " + attribution.toString());
                }
            });
            config.setLogLevel(LogLevel.VERBOSE);
            Adjust.onCreate(config);

            ad_event_map.put("af_order_id", "uvs91m");
            ad_event_map.put("af_complete_registration", "wys06w");
            ad_event_map.put("af_login", "n8dd2c");
            ad_event_map.put("first_recharge", "dpxpqa");
            ad_event_map.put("after_recharge", "5t2lwz");
            ad_event_map.put("game_times_5", "q4lmm7");
            ad_event_map.put("game_times_15", "b0sl7r");
            ad_event_map.put("game_times_50", "u7vlzh");
            ad_event_map.put("game_times_100", "ey887u");
            ad_event_map.put("game_times_200", "ayupso");
            ad_event_map.put("game_times_300", "3q0wte");
            ad_event_map.put("game_times_500", "c4hg89");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
            }
        }

        //常亮功能
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,Sylvia);
        mWakeLock.acquire();
        //获得系统震动实例
        myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info advertisingInfo = AdvertisingIdClient.getAdvertisingIdInfo(getContext());
                    if (!advertisingInfo.isLimitAdTrackingEnabled()) {
                        androidId = advertisingInfo.getId();
                    }
                    Log.d(LOG_TAG, "adid:  " + androidId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }
    }

    //获取是否是非自然流量用户
    public static boolean getAFNonOrganic(){
        if (!af_or_ad) {
            try {
                AdjustAttribution attribution = Adjust.getAttribution();
                if (attribution != null){
                    bAFNonOrganic = attribution.network.equalsIgnoreCase("Organic");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return bAFNonOrganic;
    }
    //获取非自然归因数据
    public static String getAFConversionData(){
        return conversion_json_data;
    }
    /* IGNORE - USED TO DISPLAY INSTALL DATA */
    public static String InstallConversionData =  "";
    public static int sessionCount = 0;
    public static void setInstallData(Map<String, Object> conversionData){
        if(sessionCount == 0){
            final String install_type = "Install Type: " + conversionData.get("af_status") + "\n";
            final String media_source = "Media Source: " + conversionData.get("media_source") + "\n";
            final String install_time = "Install Time(GMT): " + conversionData.get("install_time") + "\n";
            final String click_time = "Click Time(GMT): " + conversionData.get("click_time") + "\n";
            final String is_first_launch = "Is First Launch: " + conversionData.get("is_first_launch") + "\n";
            final String ad_group = "AD Group Id: " + conversionData.get("adgroup_id") + "\n";
            final String adset = "AD Set: " + conversionData.get("adset") + "\n";
            final String adset_id = "AD Set Id: " + conversionData.get("adset_id") + "\n";
            final String campaign = "campaign: " + conversionData.get("campaign") + "\n";

            conversion_json_data = new Gson().toJson(conversionData);

            InstallConversionData += install_type + media_source + install_time + click_time + is_first_launch + ad_group + adset + adset_id + campaign;
            sessionCount++;
            String af_status = conversionData.get("af_status").toString();
            bAFNonOrganic = af_status.equalsIgnoreCase("Non-organic");
            instance.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    Cocos2dxJavascriptJavaBridge.evalString("if(Sdk && Sdk.setAfStatusFinish) Sdk.setAfStatusFinish()");
                }
            });
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
    //通用上报
    public static void sendEvent(final String eventName,final String json){
        if (af_or_ad) {
            try {
                AppsFlyerLib.getInstance().logEvent(instance.getApplicationContext(), eventName, new Gson().fromJson(json, HashMap.class));
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        else {
            HashMap<String, Object> hashMap = new Gson().fromJson(json, HashMap.class);
            AdjustEvent adjustEvent = new AdjustEvent(ad_event_map.get(eventName));
            // 使用 For-Each 遍历 KeySet
            for (String key : hashMap.keySet()) {
                if (key.equals("af_revenue")) {
					adjustEvent.setRevenue(Double.parseDouble(hashMap.get("af_revenue").toString()),"INR");
                }
				else {
                    adjustEvent.addCallbackParameter(key, String.valueOf(hashMap.get(key)));
				}
            }

            Adjust.trackEvent(adjustEvent);
        }
    }
    //震动功能
    @SuppressLint("MissingPermission")
    public static void VibrateEffect(int time){
        myVibrator.vibrate(time);
    };
    //拉起WhatsApp
    public static void ShareWhatsApp(final String uri) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.whatsapp");
            instance.startActivity(intent);
        } catch (Exception e) {
            //没有安装WhatsApp
            instance.openUrl(uri);
            e.printStackTrace();
        }
    }
    //拉起FaceBook
    public static void ShareFaceBook(final String uri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, uri);
            shareIntent.setPackage("com.facebook.katana");
            instance.startActivity(shareIntent);
        } catch (Exception e) {
            //没有安装FaceBook
            instance.openUrl("https://www.facebook.com/sharer.php?title=Refer%20Earn&u=" + uri);
            e.printStackTrace();
        }
    }
    public static void openUrl(final String url)
    {
        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW,uri);
        instance.startActivity(it);
    }
    //复制功能
    public static void ClipboardCopy(final String str){
        instance.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                ClipboardManager cm = (ClipboardManager)instance.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("kk",str);
                cm.setPrimaryClip(clip);
            }
        });
    }
    //渠道号
    public static String getSdkChannel() {
        return channel_id;
    }
    //获取设备的id
    public static String getID(){
        return androidId;
    }
    //获取版本
    public static String getGameVersion()
    {
        PackageManager manager = instance.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(instance.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void ShareOther(final String text)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        instance.startActivity(sendIntent);
    }

    public static boolean getVpnState() {
		boolean bResult = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        bResult = true;
                    }
                }
            }
        }
		if (!bResult && isDeviceInVPN()) {
			bResult = true;
		}
        return bResult;
    }
	
	//设备是否使用了VPN
	public static boolean isDeviceInVPN(){
		boolean isVPN = false;
		final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		String proxyAddress;
		int proxyPort;
		if (IS_ICS_OR_LATER){
			proxyAddress = System.getProperty("http.proxyHost");
			String portStr = System.getProperty("http.proxyPort");
			proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
		}else{
			proxyAddress = android.net.Proxy.getHost(instance);
			proxyPort = android.net.Proxy.getPort(instance);
		}

		if((!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1)){
			isVPN = true;
		}
		return isVPN;
	}

    public static String getSimCountry() {
        String countryDomain = "";
        try {
            TelephonyManager telManager = (TelephonyManager)instance.getSystemService(Context.TELEPHONY_SERVICE);
            countryDomain=telManager.getSimCountryIso();
            if (!TextUtils.isEmpty(countryDomain)) {
                countryDomain = countryDomain.toUpperCase(Locale.US);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return countryDomain;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        AdmobController.getInstance().onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);

        return glSurfaceView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWakeLock == null){
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,Sylvia);
            mWakeLock.acquire();
        }
        AdmobController.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
        AdmobController.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWakeLock != null) {
            mWakeLock.release();
        }

        AdmobController.getInstance().onDestroy();

        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public static void shareGameTextOnly(String shareStr){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareStr);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "分享遊戲");
        instance.startActivity(shareIntent);
    }

    public static void shareGameWithImage(String shareStr,String imagePath){
//        Bitmap bitmap = takeScreenshot();
        File imageFile = new File(imagePath);
        Uri imageUri = FileProvider.getUriForFile(instance, instance.getPackageName() + ".fileprovider", imageFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareStr);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        instance.startActivity(Intent.createChooser(shareIntent, "分享遊戲截圖"));

    }
}
