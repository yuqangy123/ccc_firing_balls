
import * as utils from "../util"
import SingletonClass from "../base/SingletonClass";
// import { LocalStorage, CONST_STORAGE_KEY } from "../localStorage/LocalStorage";

const MUSIC_PATH = "sound/{0}";
const SOUND_PATH = "sound/{0}";

export class SdkHelper extends SingletonClass {
    

    static ins() {
        return super.ins() as SdkHelper;
    }

    init() {
        
    }


     /**
     * 分享文字 + 动态短链
     * @param shareStr 分享内容
     */
    shareTextOnly(shareStr: string) {
        if (cc.sys.isNative && cc.sys.os === cc.sys.OS_ANDROID) {
            shareStr = "快來玩我發現的這款遊戲！\nhttps://play.google.com/store/apps/details?id=com.example.game"

            jsb.reflection.callStaticMethod(
                "org/cocos2dx/javascript/AppActivity",
                "shareGameTextOnly",
                "(Ljava/lang/String;)V",
                shareStr
            );
        } else {
            cc.log("[SdkHelper] shareTextOnly 仅支持 Android 原生");
        }
    }

    /**
     * 截图当前画面并分享（带图片 + 动态短链）
     * @param shareStr 分享内容
     */
    captureAndShare(shareStr: string) {
        if (!(cc.sys.isNative && cc.sys.os === cc.sys.OS_ANDROID)) {
            cc.warn("[SdkHelper] captureAndShare 仅支持 Android 原生");
            return;
        }

        shareStr = "快來玩我發現的這款遊戲！\nhttps://play.google.com/store/apps/details?id=com.example.game"

        const winSize = cc.director.getWinSize();
        const renderTexture = new cc.RenderTexture();
        renderTexture.initWithSize(winSize.width, winSize.height);
        renderTexture.begin();
        cc.director.getScene().visit();
        renderTexture.end();

        const filePath = jsb.fileUtils.getWritablePath() + "screenshot.png";
        renderTexture.saveToFile("screenshot.png", cc.ImageFormat.PNG, true, () => {
            cc.log("[SdkHelper] 截图保存成功:", filePath);

            if (cc.sys.isNative && cc.sys.os === cc.sys.OS_ANDROID) {
                jsb.reflection.callStaticMethod(
                    "org/cocos2dx/javascript/AppActivity",
                    "shareGameWithImage",
                    "(Ljava/lang/String;Ljava/lang/String;)V",
                    shareStr,
                    filePath
                );
            }
        });
    }

    /**
     * 监听从 Dynamic Link 传回的 ref 参数
     * @param callback 回调函数 (ref: string)
     */
    static onDynamicLinkReceived(callback: (ref: string) => void) {
        cc.game.on("onDynamicLinkReceived", (ref: string) => {
            cc.log("[SdkHelper] 动态链接参数：", ref);
            callback(ref);
        });
    }
}

// enum AudioType {
//     Music = 1,
//     Sound = 2,
// }

// interface AudioPlayTask {
//     type: AudioType;
//     name: string;
//     path: string;
//     volume: number;
//     loop: boolean;
//     cb?: utils.handler;
// }

export const AUDIO_CONFIG = {
    Audio_Btn: "button",
    Audio_levelup: "levelup",
    Audio_star: "star",
    Audio_balls: "balls",
    Audio_Bgm: "bg",
    Audio_gameover: "gameover",
    Audio_win: "win",
    Audio_congra: "congra",

}