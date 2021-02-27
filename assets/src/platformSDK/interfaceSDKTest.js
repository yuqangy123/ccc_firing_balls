class interfaceSDKTest extends require('interfaceSDK') {
	constructor() {
		super()
	}
	showLoginDialog(callback){
        console.log("interfaceSDKTest's showLoginDialog")
    }
}
module.exports = interfaceSDKTest