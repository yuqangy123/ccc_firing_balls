// author : lamyoung
import * as ui from "../../common/ui/pop_mgr";
import { POP_UI_BASE } from "../../common/ui/pop_ui_base";
import GameModel from "../model/GameModel";
import { AudioPlayer, AUDIO_CONFIG } from "../../common/audio/AudioPlayer";
import { EventDispatch, Event_Name } from "../../common/event/EventDispatch";
import { SdkHelper } from "../../common/sdk/SdkHelper";

const { ccclass, property } = cc._decorator;

@ccclass
export default class ResultView extends POP_UI_BASE {
    constructor() {
        super();
    }
    @property(cc.Node)
    btn_reset: cc.Node = null;
    @property(cc.Node)
    btn_revive: cc.Node = null;
    @property(cc.Node)
    btn_revive_back: cc.Node = null;
    @property(cc.Label)
    lb_revive_count: cc.Label = null;
    @property(cc.Node)
    node_no_revive: cc.Node = null;
    @property(cc.Node)
    node_revive: cc.Node = null;
    @property(cc.Node)
    btn_share: cc.Node = null;
    @property(cc.Label)
    lb_score: cc.Label = null;
    @property(cc.Label)
    newRecordLabel: cc.Label = null;

    private _sound: string[] = [AUDIO_CONFIG.Audio_gameover, AUDIO_CONFIG.Audio_win, AUDIO_CONFIG.Audio_congra];
    private new_best_score = false;

    onLoad() {
        this.btn_reset.on(cc.Node.EventType.TOUCH_END, this.backToMenu, this);
        this.btn_revive.on(cc.Node.EventType.TOUCH_END, this.gameRevive, this);
        this.btn_revive_back.on(cc.Node.EventType.TOUCH_END, this.closeGameRevive, this);
        this.btn_share.on(cc.Node.EventType.TOUCH_END, this.share, this);

        const scaleTime = 0.75;
        const action = cc.repeatForever(cc.sequence(
            cc.scaleTo(scaleTime, 1.1, 1.1).easing(cc.easeInOut(2.0)), 
            cc.scaleTo(scaleTime, 1, 1).easing(cc.easeInOut(2.0))
        ));
        this.btn_revive.runAction(action);
        this.btn_share.runAction(action.clone());
    }

    private backToMenu() {
        this.onCloseBtnTouch();
        ui.pop_mgr.get_inst().hide(ui.UI_CONFIG.game);
    }

    private share() {
        // EventDispatch.ins().fire(Event_Name.SHOW_TIPS, '分享失败')
        let shareStr = "";
        if(this.new_best_score)
            shareStr = "WOW~ 我創造了新的記錄，快來玩我發現的這款遊戲！\nhttps://play.google.com/store/apps/details?id=com.example.game";

        SdkHelper.ins().shareTextOnly(shareStr);
    }

    private closeGameRevive() {
        this.updateCanRevive(false);
    }

    private gameRevive() {
        SdkHelper.ins().showRewardedAd(() => {
            EventDispatch.ins().fire(Event_Name.GAME_RELIVE);
            this.onCloseBtnTouch();
        });
    }

    private _autoReviveCount = 10;
    private updateCanRevive(canRevive: boolean) {
        this.node_revive.active = canRevive;
        this.node_no_revive.active = !canRevive;
        if (canRevive) {
            this._autoReviveCount = 10;
            this.autoReviveCountFn();
            this.schedule(this.autoReviveCountFn, 1, this._autoReviveCount + 1, 0);
        } else {
            this.unschedule(this.autoReviveCountFn);
        }
    }

    private autoReviveCountFn() {
        this._autoReviveCount--;
        this.lb_revive_count.string = `${this._autoReviveCount}`;
        if (this._autoReviveCount <= 0) {
            this.closeGameRevive();
        }
    }

    on_show() {
        super.on_show();
        const score = GameModel.ins().score;
        const bestScore = GameModel.ins().best_score;
        const can_revive = GameModel.ins().revive_times < 1;
        this.new_best_score = score > bestScore;

        if (this.new_best_score) {
            GameModel.ins().best_score = score;
            AudioPlayer.ins().play_sound(AUDIO_CONFIG.Audio_congra);
            
        }
        else{
            AudioPlayer.ins().play_sound(Math.random() >= 0.6 ? AUDIO_CONFIG.Audio_win:AUDIO_CONFIG.Audio_gameover);
        }

        this.newRecordLabel.node.active = this.new_best_score;

        this.lb_score.string = `${score}`;
        
        this.updateCanRevive(can_revive);
    }

    on_hide() {
        super.on_hide();
    }
}
