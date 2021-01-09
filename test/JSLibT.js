const jsT = {
    libname: 'JSLib',
    name: '一枚小工JS',
    
    print(){
        console.log(this.name);
    },

    valid(){
        return tt;
    },

    showLoginDialog(callback){
        /*if(!this.valid())return;

        tt.authorize({
            scope: "scope.userInfo",
            success() {
              // 用户同意授权用户信息
              if (callback) {
                callback(true);
                }
            },
          });*/
          callback("test callback", 1);
    }
}
export default jsT;