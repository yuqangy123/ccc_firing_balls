package org.cocos2dx.javascript;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerConversionListener;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import android.content.ClipboardManager;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.appsflyer.AppsFlyerLib;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static String jsCallbackMethodName = "";


    private static Map<String, String> ad_event_map = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            return;
        }

        instance=this;
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWakeLock != null) {
            mWakeLock.release();
        }

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


    /////////////////////////////////////////////////////相册选择//////////////////////////////////////////////////
    /////////////////////////////////////////////////////相册选择//////////////////////////////////////////////////
    /////////////////////////////////////////////////////相册选择//////////////////////////////////////////////////


    public static void openPhotoPicker(final String jsCallbackMethod) {
        jsCallbackMethodName = jsCallbackMethod;
//        AppActivity activity =  (AppActivity) getInstance();
        Log.d(LOG_TAG, "openPhotoPickerAndUpload.SDK_INT: " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(LOG_TAG, "openPhotoPickerAndUpload pickMedia ");
            // Android 13 (API 33) 及以上，使用 Photo Picker
//            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1); // 单选
            instance.pickMedia.launch(intent);

        } else {
            Log.d(LOG_TAG, "openPhotoPickerAndUpload openLegacyGallery ");
            instance.openLegacyGallery();
        }
    }

    // 用于启动Photo Picker的ActivityResultLauncher
    private final ActivityResultLauncher<Intent> pickMedia = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri selectedImageUri = null;

                    // 处理单选和多选
                    if (data.getClipData() != null) {
                        // 多选情况（这里示例处理第一张，可根据业务处理多张）
                        selectedImageUri = data.getClipData().getItemAt(0).getUri();
                    } else if (data.getData() != null) {
                        // 单选情况
                        selectedImageUri = data.getData();
                    }

                    if (selectedImageUri != null) {
                        // 在后台线程处理图像和上传
                        Uri finalSelectedImageUri = selectedImageUri;
                        new Thread(() -> {
                            try {
                                // 将Uri转换为尺寸为122*122的Bitmap
                                InputStream inputStream = getContentResolver().openInputStream(finalSelectedImageUri);
                                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 122, 122, true);
                                
                                // 创建临时文件保存调整尺寸后的图片
                                File tempFile = new File(getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".png");
                                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                                fileOutputStream.flush();
                                fileOutputStream.close();
                                
                                inputStream.close();
                                originalBitmap.recycle();
                                resizedBitmap.recycle();

                                // 将临时文件的Uri发送给JS层
                                Uri tempFileUri = Uri.fromFile(tempFile);
                                sendUriBackToJS(tempFileUri.toString());

                            } catch (IOException e) {
                                e.printStackTrace();
                                sendErrorBackToJS("Failed to process image: " + e.getMessage());
                            }
                        }).start();
                    } else {
                        sendErrorBackToJS("No image selected");
                    }
                } else {
                    // 用户取消了选择
                    sendErrorBackToJS("User cancelled image selection");
                }
            });

    // 用于启动传统选择器的ActivityResultLauncher (API < 33)
    private final ActivityResultLauncher<Intent> pickLegacyMedia = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleImageSelectionResult(result.getData());
                } else {
                    sendErrorBackToJS("User cancelled image selection");
                }
            });

    // 用于请求存储权限的ActivityResultLauncher (针对低版本Android)
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // 权限被授予，启动传统选择器
                    openLegacyGallery();
                } else {
                    // 权限被拒绝
                    sendErrorBackToJS("Storage permission is required to select images");
                }
            });


    /**
     * 为低版本Android打开传统图库选择器
     */
    private void openLegacyGallery() {
        Intent intent;
        // 优先尝试 ACTION_PICK
        try {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickLegacyMedia.launch(intent);
        } catch (Exception e) {
            // 如果ACTION_PICK不可用，回退到ACTION_GET_CONTENT
            try {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                pickLegacyMedia.launch(intent);
            } catch (Exception ex) {
                sendErrorBackToJS("No application available to handle image selection");
            }
        }
    }

    /**
     * 处理图片选择结果（统一处理Photo Picker和传统方式返回的Intent）
     */
    private void handleImageSelectionResult(Intent data) {
        Uri selectedImageUri = data.getData();
        if (selectedImageUri != null) {
            // 在后台线程处理图像和上传
            new Thread(() -> {
                try {
                    // 对于低版本Android，特别是某些厂商（如小米），可能需要解析真实的路径
                    String imagePath = getRealPathFromURI(selectedImageUri);
                    InputStream inputStream;
                    if (imagePath != null) {
                        // 如果成功解析到文件路径，使用FileInputStream
                        inputStream = new FileInputStream(new File(imagePath));
                    } else {
                        // 否则使用ContentResolver
                        inputStream = getContentResolver().openInputStream(selectedImageUri);
                    }

                    if (inputStream != null) {
                        // 将输入流解码为Bitmap并调整尺寸为122*122
                        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 122, 122, true);
                        
                        // 创建临时文件保存调整尺寸后的图片
                        File tempFile = new File(Cocos2dxHelper.getWritablePath(), "temp_avatar_" + System.currentTimeMillis() + ".png");
                        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        // 关闭资源
                        inputStream.close();
                        originalBitmap.recycle();
                        resizedBitmap.recycle();

                        // 将临时文件的Uri发送给JS层
                        Uri tempFileUri = Uri.fromFile(tempFile);
                        sendUriBackToJS(tempFileUri.toString());
                    } else {
                        sendErrorBackToJS("Failed to read image data");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    sendErrorBackToJS("Failed to process image: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorBackToJS("Unexpected error: " + e.getMessage());
                }
            }).start();
        } else {
            sendErrorBackToJS("No image selected or invalid data");
        }
    }

    /**
     * 尝试从URI获取文件的真实路径（主要针对低版本Android和某些特定厂商）
     * 这是一个复杂的过程，因为Android不同版本和不同厂商的处理方式不同
     */
    private String getRealPathFromURI(Uri uri) {
        // 在Android 10 (API 29) 及以上，直接使用文件路径的方式受到限制，推荐使用ContentResolver直接操作流。
        // 此方法主要针对API 29以下的设备，且需要READ_EXTERNAL_STORAGE权限。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上，通常不需要或不推荐获取绝对路径，直接使用Uri通过ContentResolver操作即可。
            // 但为了兼容某些旧逻辑或库，有时仍会尝试获取。
            // 注意：Scoped Storage下，直接文件路径访问可能失败。
            return null; // 返回null，让上层代码使用ContentResolver
        }

        String path = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是Document URI
            String docId = DocumentsContract.getDocumentId(uri);
            String authority = uri.getAuthority();
            if ("com.android.providers.media.documents".equals(authority)) {
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                path = getDataColumn(contentUri, selection, selectionArgs);
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                path = getDataColumn(contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是Content URI
            path = getDataColumn(uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是File URI
            path = uri.getPath();
        }
        return path;
    }

    /**
     * 辅助方法，从ContentProvider查询数据
     */
    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    private void sendUriBackToJS(final String uri) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String dcodeUri = uri.replace("file://", "");
                    String jsData = String.format("'{ \"uri\": \"%s\", \"result\": 0 }'", dcodeUri);
                    String callbackScript = String.format("%s(%s)", jsCallbackMethodName, jsData);
                    Cocos2dxJavascriptJavaBridge.evalString(callbackScript);
                } catch (Exception e) {
                    sendErrorBackToJS("sendUriBackToJS failed: " + e.getMessage());
                }
            }
        });
    }

    private void sendErrorBackToJS(final String errorMessage) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsData = String.format("'{ \"err_msg\": \"%s\", \"result\": 1 }'", errorMessage);
                    String callbackScript = String.format("%s(%s)", jsCallbackMethodName, jsData);
                    Cocos2dxJavascriptJavaBridge.evalString(callbackScript);

                } catch (Exception e) {
                }
            }
        });
    }
}
