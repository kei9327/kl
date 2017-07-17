package com.knowlounge.network.websocket.command;

import com.knowlounge.common.Command;
import com.knowlounge.apprtc.WebSocketChannelClient;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p/>
 * Alo
 *
 * WebSocket protocol command
 *
 * author: Jun-hyoung Lee
 * date: 2016-03-21.
 */
public abstract class WebCommand implements Command {

    private WebSocketChannelClient mWebSocket;


    /*
     *----------------------------------------------------------------------------------------------
     *--<implements : Command>
     */

    @Override
    public void execute() {
        String message = makeJson();
        mWebSocket.send(message);
    }


    /*
     *----------------------------------------------------------------------------------------------
     *--<methods>
     */

    abstract String makeJson();

    public WebCommand socket(WebSocketChannelClient socket) {
        if (socket == null) {
            throw new NullPointerException("WebCommand's socket argument cannot be null");
        }
        mWebSocket = socket;
        return this;
    }

}
