const Http = require('Http');
var interfaceSDK = require('interfaceSDK');


const AppID='tt4c843a39d3bf7e0e';
const AppSecret='de89a86b0b00114692b37370b2ac6eb6a04afb2a';


//const byteDanceSDK = {   
class byteDanceSDK extends interfaceSDK {
    constructor() {
        console.log('byteDanceSDK constructor');
        super();

        this.session_key = '';
        this.openid = '';
        this.anonymous_openid = '';
        this.isLogin = false;
    }
    
    print(){
        console.log(this.name);
    }

    valid(){
        if (typeof tt == "undefined")
            return false;
        else
            return true;
    }

    showLoginDialog(callback){
        if(!this.valid())
        {
            console.log('tt sdk is not valid')
            return;
        }
        
        var self = this;
        tt.login({
            force: true,
            success(res) {
              console.log(`login 调用成功${res.code} ${res.anonymousCode}`);
              self.getUserSession(self, res.code, res.anonymousCode, callback)
            },
            fail(res) {
              console.log(`login 调用失败`);
              if(callback)callback(false);
            },
          });
    }

    getUserSession(self, code, anonymousCode, callback) {
        let url=string.format('https://developer.toutiao.com/api/apps/jscode2session?appid=%s&secret=%s&code=%s&anonymous_code=%s',AppID, AppSecret, code, anonymousCode);        
        Http.httpGet(url,function (status, response) {
            let result = null;
            try {
                result = JSON.parse(response);
                console.log('getUserSession->result:', result);
                self.session_key = result.session_key
                self.openid = result.openid
                self.anonymous_openid = result.anonymous_openid
                if(callback)callback(true, self.openid==''?self.anonymous_openid:self.openid)
            } catch (error) {
                console.log("getUserSession-> json parse error : " + error);
                if(callback)callback(false)
            }
        });
    }

    showBannerAd(){
        if(!this.valid())
        {
            console.log('tt sdk is not valid')
            return;
        }

        this.hideBannerAd();
        
        const { windowWidth, windowHeight } = tt.getSystemInfoSync();
        const targetBannerAdWidth = 200;

        // 创建一个居于屏幕底部正中的广告
        this.bannerAd = tt.createBannerAd({
        adUnitId: "1f899g1d40j2hkq4pa",
        style: {
            width: windowWidth,
            top: windowHeight - (targetBannerAdWidth / 16) * 9, // 根据系统约定尺寸计算出广告高度
            left : 0,
        },
        });
        // 也可以手动修改属性以调整广告尺寸
        this.bannerAd.style.left = (windowWidth - targetBannerAdWidth) / 2;

        // 尺寸调整时会触发回调，通过回调拿到的广告真实宽高再进行定位适配处理
        // 注意：如果在回调里再次调整尺寸，要确保不要触发死循环！！！
        this.bannerAd.onResize((size) => {
            // good
            console.log('BannerOnResize' + size.width + ',' + size.height);
            this.bannerAd.style.top = windowHeight - size.height;
            this.bannerAd.style.left = (windowWidth - size.width) / 2;

            // bad，会触发死循环
            // bannerAd.style.width++;
        });
        
        this.bannerAd.onLoad(() => {
            this.bannerAd
              .show()
              .then(() => {
                console.log("Banner广告显示成功");
              })
              .catch((err) => {
                console.log("Banner广告组件出现问题", err);
              });
          });
    }

    hideBannerAd(){
        if(this.bannerAd){
            this.bannerAd.hide();
            this.bannerAd.destroy();
            this.bannerAd = null;
        }
    }
    
    //设置分享回调
    setShareAppMessageCallback(callback){
        if(!this.valid())
        {
            console.log('tt sdk is not valid')
            return;
        }

        tt.onShareAppMessage(function (res) {
            //当监听到用户点击了分享或者拍抖音等按钮后，会执行该函数
            console.log(res.channel);
            // do something
            return {
                title: '根本停不下来，你能超越我吗？',
                desc: '你能超越我吗？通过弹球将数字方块一个一个打击！守住底线！',
                path: '/pages/index/index?from=sharebuttonabc&otherkey=othervalue', // ?后面的参数会在转发页面打开时传入onLoad方法
                //imageUrl: 'https://e.com/e.png', // 支持本地或远程图片，默认是小程序icon
                templateId: '2k7d37mcbdjj64mbe9',
                success () {
                  console.log('转发发布器已调起，并不意味着用户转发成功，微头条不提供这个时机的回调');
                },
                fail () {
                  console.log('转发发布器调起失败');
                }
            } //返回的对象会传入tt.shareAppMessage进行最终分享
        });
    }

    //主动调起分享
    shareAppMessage(){
        if(!this.valid())
        {
            console.log('tt sdk is not valid')
            return;
        }

        //同时使用模板 templateId 和 分享内容
        tt.shareAppMessage({
            templateId: "2k7d37mcbdjj64mbe9", // 替换成通过审核的分享ID
            title: "测试分享",
            desc: "测试描述",
            imageUrl: "",
            query: "",
            success() {
              console.log("分享视频成功");
            },
            fail(e) {
              console.log("分享视频失败");
            },
          });
    }

    //展示激励视频
    showRewardedVideoAd(playCompleteCallback){
        if(!this.valid())
        {
            console.log('tt sdk is not valid')
            return;
        }

        const videoAd = tt.createRewardedVideoAd({
            adUnitId: "6ki998ece6hk4ujjed",
          });

        videoAd.onError(function (res) {
            console.log('激励视频广告Error:' + res.errCode + ',' + res.errMsg);
        });

        videoAd.onClose(({ isEnded }) => {
            console.log('激励视频广告Close.isEnded:' + isEnded);
            if (isEnded) {
                // 给予奖励
                if(playCompleteCallback)
                    playCompleteCallback();
            }
          });

        videoAd.show();
    }

    //开始录屏
    startGameRecorder(){
        console.log("bytedance.startGameRecorder");

        const recorder = tt.getGameRecorderManager();

        recorder.onStart((res) => {
            console.log('recorder onStarted');
          });

        
        
          
        recorder.start({
            duration: 30,
          });
    }

    //停止录屏
    stopGameRecorder(){
        console.log('bytedance.stopGameRecorder');
        const recorder = tt.getGameRecorderManager();

        let self = this;
        recorder.onStop((res) => {
            console.log('recorder onStoped, video path:' + res.videoPath);
            self.videoPath = res.videoPath;
        });

        recorder.stop();
    }

    //裁剪视频
    clipGameRecorder(callback){
        console.log('bytedance.clipGameRecorder');
        const recorder = tt.getGameRecorderManager();
        let self = this;
        recorder.clipVideo({
            path: self.videoPath,
            timeRange: [10, 0],
            success(res2) {
                console.log('clip vodeo path: ' + res2.videoPath); // 生成最后10秒的视频
                
                tt.shareAppMessage({
                    channel: "video",
                    extra: {
                        withVideoId: true,
                        videoPath:res2.videoPath,
                        hashtag_list:[],
                        video_title:'我很厉害哦，快来挑战我吧！',
                        createChallenge:true,
                        videoTag:'球球',
                      },
                    success: (res3) => {
                      console.log("分享视频成功");
                    },
                  });

                if(callback)callback(res2.videoPath);
            },
            fail(e) {
                console.error(e);
            },
        });
    }

}
//export default byteDanceSDK;
module.exports = byteDanceSDK