import JSLibT = require('./byteDanceSDK');

export default class TSLib{
    public static TAG:string = 'TS';
    public libname:string = 'TSLib';
    private name:string = '一枚小工TS';
​
        private getResult(rst:string, num:number):void
      {
          console.log("get rusult:"+rst+num);
        
      }

    public print(){
        console.log(this.name);

        if(JSLibT != null){
            console.log('ts 调用 js');

            JSLibT.showLoginDialog(function (result:boolean){
                console.log("showLoginDialog:"+result);
            });
            
        }else{
            console.log('ts2js is null');
        }
    }    
}