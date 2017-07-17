package com.knowlounge.view.room;

import com.knowlounge.base.MvpView;
import com.knowlounge.widget.RenderView;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 */

public interface RoomView extends MvpView {

    RenderView addRemoteVideoView(String userId);

    void removeRemoteVideoView(String userId);
}
