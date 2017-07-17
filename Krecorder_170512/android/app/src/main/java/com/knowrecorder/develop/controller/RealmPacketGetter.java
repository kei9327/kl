package com.knowrecorder.develop.controller;

import com.knowrecorder.develop.model.realm.PacketObject;
import com.knowrecorder.develop.model.realm.Page;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by we160303 on 2017-02-20.
 */

public class RealmPacketGetter {
    private final String TAG = "RealmPacketGetter";
    private static  RealmPacketGetter instance = null;
    private Realm realm;

    public static RealmPacketGetter getInstance(){
        if(instance ==null)
            instance = new RealmPacketGetter();
        return instance;
    }

    private RealmPacketGetter(){
    }


    public RealmResults<PacketObject> getallPacket(boolean isAscending){
        realm = Realm.getDefaultInstance();

        RealmResults<PacketObject> result =
                realm.where(PacketObject.class)
                .findAllSorted("id", isAscending ? Sort.ASCENDING : Sort.DESCENDING);
        return result;
    }

    public RealmResults<PacketObject> getPacketWidthPage(int pageNumber, boolean isAscending){
        realm = Realm.getDefaultInstance();

        long pageId = realm.where(Page.class)
                        .equalTo("pagenum", pageNumber).findFirst().getId();

        RealmResults<PacketObject> result =
                realm.where(PacketObject.class)
                .equalTo("pageid", pageId)
                .findAllSorted("id", isAscending ? Sort.ASCENDING : Sort.DESCENDING);

        realm.close();

        return result;
    }

}
