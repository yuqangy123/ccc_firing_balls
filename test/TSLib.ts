import JSLibT = require('./JSLibT');

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

            JSLibT.showLoginDialog(function (rst:string, num:number){
                console.log("get rusultA:"+rst+num);
            });
            
        }else{
            console.log('ts2js is null');
        }
    }    
}