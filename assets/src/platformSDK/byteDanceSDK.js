const Http = require('Http');

const byteDanceSDK = {
    user_session_key:'',
    openid:'',
    anonymous_openid:'',
    AppID:'tt4c843a39d3bf7e0e',
    AppSecret:'de89a86b0b00114692b37370b2ac6eb6a04afb2a',

    print(){
        console.log(this.name);
    },

    valid(){
        return tt;
    },

    showLoginDialog(callback){
        if(!this.valid())
        {
            console.log('tt sdk is not valid')
            return;
        }
        
        let self=this
        tt.login({
            force: true,
            success(res) {
              console.log(`login 调用成功${res.code} ${res.anonymousCode}`);
              self.getUserSession(res.code, res.anonymousCode, callback, self)
            },
            fail(res) {
              console.log(`login 调用失败`);
              if(callback)callback(false);
            },
          });

          let url ='https://developer.toutiao.com/api/apps/jscode2session?appid=tt4c843a39d3bf7e0e'
          Http.httpGet(url,function (status, response) {
            console.log('Http.httpGet.response:', response);
            let result = null;
            try {
                result = JSON.parse(response);
                console.log('getUserSession->result:', result);
                self.user_session_key = result.session_key
                self.openid = result.openid
                self.anonymous_openid = result.anonymous_openid
                if(callback)callback(true, self.openid==''?self.anonymous_openid:self.openid)
            } catch (error) {
                console.log("getUserSession-> json parse error : " + error);
                if(callback)callback(false)
            }
        });
        
    },

    getUserSession(code, anonymousCode, callback, self) {
        let url=string.format('https://developer.toutiao.com/api/apps/jscode2session?appid=%s&secret=%s&code=%s&anonymous_code=%s',self.AppID, self.AppSecret, code, anonymousCode);        
        console.log('getUserSession->url.len:', url.length);
        console.log('getUserSession->url:', url);
        Http.httpGet(url,function (status, response) {
            console.log('Http.httpGet.response:', response);
            let result = null;
            try {
                result = JSON.parse(response);
                console.log('getUserSession->result:', result);
                self.user_session_key = result.session_key
                self.openid = result.openid
                self.anonymous_openid = result.anonymous_openid
                if(callback)callback(true, self.openid==''?self.anonymous_openid:self.openid)
            } catch (error) {
                console.log("getUserSession-> json parse error : " + error);
                if(callback)callback(false)
            }
        });
    },

    createBannerAd(){
        //设计分辨率
        let designSize = cc.view.getDesignResolutionSize();
        //屏幕物理分辨率 也就是手机分辨率。
        let frameSize = cc.view.getFrameSize();
        //let windowWidth = designSize.width;
        //let windowHeight = designSize.height;
        const {
            windowWidth,
            windowHeight,
        } = tt.getSystemInfoSync();
        var targetBannerAdWidth = 200;
        
        // 创建一个居于屏幕底部正中的广告
        let bannerAd = tt.createBannerAd({
            adUnitId: 'm2j65emdb9c1amndbh',
            style: {
                width: targetBannerAdWidth,
                top: windowHeight - (targetBannerAdWidth / 16 * 9), // 根据系统约定尺寸计算出广告高度
            },
        });
        // 也可以手动修改属性以调整广告尺寸
        bannerAd.style.left = (windowWidth - targetBannerAdWidth) / 2;
        
        // 尺寸调整时会触发回调
        // 注意：如果在回调里再次调整尺寸，要确保不要触发死循环！！！
        bannerAd.onResize(size => {
            // console.log(size.width, size.height);
        
            // 如果一开始设置的 banner 宽度超过了系统限制，可以在此处加以调整
            if (targetBannerAdWidth != size.width) {
                targetBannerAdWidth = size.width;
                bannerAd.style.top = windowHeight - (size.width / 16 * 9);
                bannerAd.style.left = (windowWidth - size.width) / 2;
            }
        });
        bannerAd.onLoad(() => {
            bannerAd
              .show()
              .then(() => {
                console.log("广告显示成功");
              })
              .catch((err) => {
                console.log("广告组件出现问题", err);
              });
          });
        this._bannerAd = bannerAd;
    }
}
export default byteDanceSDK;