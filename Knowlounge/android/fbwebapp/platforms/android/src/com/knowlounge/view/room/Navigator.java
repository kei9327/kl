package com.knowlounge.view.room;

import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;
import com.knowlounge.model.RoomSpec;

import javax.inject.Singleton;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-13.
 * Description : RoomActvity로 이동할 때 (수업으로 입장할 때) 사용되는 클래스. RoomSpec 인스턴스를 생성하고 RoomActivity로 넘길 파라미터들을 Bundle안에 set해준다.
 */
@Singleton
public class Navigator {

    public Navigator() {

    }

    public void navigateToRoomActivityView(Context context, JsonObject params, JsonObject extraParams) {
        if (context != null) {
            Intent intent = new Intent(context, RoomActivity.class);

            // 서브 룸의 룸아이디가 올 경우, 처리 루틴..
            String roomIdParam = params.get("roomid").getAsString();
            int specialCharPosition = roomIdParam.indexOf("_");
            String roomId = specialCharPosition < 0 ? roomIdParam : roomIdParam.substring(0, specialCharPosition);

            intent.putExtra("arguments",
                    new RoomSpec.Builder()
                            .host(params.get("host").getAsString())
                            .name(params.get("name").getAsString())
                            .port(params.get("port").getAsString())
                            .userNo(params.get("userno").getAsString())
                            .userNm(params.get("usernm").getAsString())
                            .roomId(roomId)
                            .accessToken(params.get("token").getAsString())
                            .enableVideo(params.get("video").getAsBoolean())
                            .enableAudio(params.get("audio").getAsBoolean())
                            .enableVolume(params.get("volume").getAsBoolean())
                            .build());
            intent.putExtra("type", extraParams.get("type").getAsString());
            intent.putExtra("roomurl", extraParams.get("roomurl").getAsString());
            intent.putExtra("deviceid", extraParams.get("deviceid").getAsString());
            intent.putExtra("mode", extraParams.get("mode").getAsInt());
            if (extraParams.has("guest"))
                intent.putExtra("guest", extraParams.get("guest").getAsString());

            context.startActivity(intent);
        }
    }
}
