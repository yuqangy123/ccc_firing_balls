
class interfaceSDK {
	constructor() {
        // 默认返回实例对象 this
    }
    
    //显示登录对话框
    showLoginDialog(callback){}
    
    //显示横幅广告
    showBannerAd(){}

    //关闭横幅广告
    hideBannerAd(){}

    //设置分享回调
    setShareAppMessageCallback(callback){}

    //主动调起分享
    shareAppMessage(){}

    //展示激励视频
    showRewardedVideoAd(playCompleteCallback){}

}

module.exports = interfaceSDK