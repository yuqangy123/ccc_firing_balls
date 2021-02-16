const Http = require('Http');




session_key='';
openid='';
anonymous_openid='';
isLogin=false;
const AppID='tt4c843a39d3bf7e0e';
const AppSecret='de89a86b0b00114692b37370b2ac6eb6a04afb2a';

const byteDanceSDK = {    
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
        
        tt.login({
            force: true,
            success(res) {
              console.log(`login 调用成功${res.code} ${res.anonymousCode}`);
              this.getUserSession(res.code, res.anonymousCode, callback)
            },
            fail(res) {
              console.log(`login 调用失败`);
              if(callback)callback(false);
            },
          });
    },

    getUserSession(code, anonymousCode, callback) {
        let url=string.format('https://developer.toutiao.com/api/apps/jscode2session?appid=%s&secret=%s&code=%s&anonymous_code=%s',AppID, AppSecret, code, anonymousCode);        
        Http.httpGet(url,function (status, response) {
            let result = null;
            try {
                result = JSON.parse(response);
                console.log('getUserSession->result:', result);
                session_key = result.session_key
                openid = result.openid
                anonymous_openid = result.anonymous_openid
                if(callback)callback(true, openid==''?anonymous_openid:openid)
            } catch (error) {
                console.log("getUserSession-> json parse error : " + error);
                if(callback)callback(false)
            }
        });
    },

    setShareAppMessageCallback(callback){

    },

}
export default byteDanceSDK;