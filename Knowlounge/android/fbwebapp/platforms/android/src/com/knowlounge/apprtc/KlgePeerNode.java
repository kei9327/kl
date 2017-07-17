package com.knowlounge.apprtc;

import com.knowlounge.model.KlgePeer;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-07.
 */

public class KlgePeerNode {

    int mIndex;
    KlgePeer mPeer;
    boolean mIsConnected;

    private KlgePeerNode(int index, KlgePeer peer) {
        this.mIndex = index;
        this.mPeer = peer;
        this.mIsConnected = false;
    }

    static KlgePeerNode create(int index, KlgePeer peer) {
        return new KlgePeerNode(index, peer);
    }

    public int getId() {
        return mIndex;
    }

    public void setId(int index) {
        mIndex = index;
    }

    public KlgePeer getPeer() {
        return mPeer;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setIsConnected(boolean isConnected) {
        mIsConnected = isConnected;
    }

}
