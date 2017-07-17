package com.knowlounge.dagger.component;

import com.knowlounge.dagger.modules.KlgeClientControllerModule;
import com.knowlounge.dagger.modules.RoomMvpViewModule;
import com.knowlounge.dagger.modules.RtcContextModule;
import com.knowlounge.dagger.scopes.PerFragment;
import com.knowlounge.apprtc.KlgeClientController;
import com.knowlounge.view.room.MultiVideoChatFragment;
import com.knowlounge.view.room.RoomView;
import com.wescan.alo.rtc.RtcChatContext;

import dagger.Component;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

@PerFragment
@Component(dependencies = AppComponent.class, modules = {
        RtcContextModule.class,
        RoomMvpViewModule.class,
        KlgeClientControllerModule.class}
)
public interface MultiVideoChatFragmentComponent {
    void inject(MultiVideoChatFragment fragment);

    RtcChatContext getRtcContext();

    RoomView getRoomView();

    KlgeClientController getController();
}
