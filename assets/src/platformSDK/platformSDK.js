const Http = require('Http');

var byteDanceSDK = require('byteDanceSDK');
var sdk = new byteDanceSDK();


const platformSDK = {

    showLoginDialog(callback){
        sdk.showLoginDialog(callback);
    },

    showBannerAd(){
        sdk.showBannerAd();
    },

    hideBannerAd(){
        sdk.hideBannerAd();
    },

    setShareAppMessageCallback(callback){
        sdk.setShareAppMessageCallback(callback);
    },

    //主动调起分享
    shareAppMessage(){
        sdk.shareAppMessage();
    },

    //展示激励视频
    showRewardedVideoAd(playCompleteCallback){
        sdk.showRewardedVideoAd(playCompleteCallback);
    },

    //开始录屏
    startGameRecorder(){
        if(sdk.startGameRecorder)
            sdk.startGameRecorder();
    },

    //停止录屏
    stopGameRecorder(){
        if(sdk.stopGameRecorder)
            sdk.stopGameRecorder();
    },

    //裁剪视频
    clipGameRecorder(callback){
        if(sdk.clipGameRecorder)
            sdk.clipGameRecorder(callback);
    }

}
export default platformSDK;