package com.cylan.jiafeigou.rx.event;

/**
 * Created by yzd on 16-12-2.
 */

public class EventFactory {
    public static HideVideoViewEvent sHideVideoViewEvent = new HideVideoViewEvent();
    public static ShowVideoViewEvent sShowVideoViewEvent = new ShowVideoViewEvent();
    public static StartEnterAnimationEvent sStartEnterAnimationEvent = new StartEnterAnimationEvent();

    public static class HideVideoViewEvent {
    }

    public static class ShowVideoViewEvent {
    }

    public static class StartEnterAnimationEvent {
    }
}
