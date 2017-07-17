package com.knowlounge.apprtc;

import android.text.TextUtils;
import android.util.Log;

import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.model.KlgePeer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-06.
 */

@PerActivity
public class KlgePeerWatcher {
    private static final String TAG = KlgePeerWatcher.class.getSimpleName();

    private String mUserId;

    private ConcurrentLinkedQueue<List> mRtcListQueue = new ConcurrentLinkedQueue<List>();

    /**
     * [user-id, peer]
     */
    private ConcurrentHashMap<String, KlgePeerNode> mPeers = new ConcurrentHashMap<>();

    /**
     * 피어 이벤트 리스너들
     */
    private Set<KlgePeerEvents> mKlgePeerEvents = new HashSet<>();

    private RtcListListener mRtcListListener;

    @Inject
    public KlgePeerWatcher(String myUserId) {
        mUserId = myUserId;
    }

    private boolean isValidUserId() {
        return !TextUtils.isEmpty(mUserId);
    }

    /**
     * KlgeClientController로부터 onRtcList에 대한 이벤트를 받아서 피어 리스트들을 관리하도록 로직을 수정한다.
     *
     * [UI Thread]
     * @param peers 피어 리스트
     */
    void onPeerList(List<KlgePeer> peers) {
        mRtcListQueue.add(peers);
        //Log.d(TAG, "<onPeerList> mRtcListQueue.size : " + mRtcListQueue.size());
        runOnPeerList();
    }


    private void runOnPeerList() {
        List<KlgePeer> peers = mRtcListQueue.poll();

        if (!isValidUserId()) {
            throw new IllegalArgumentException("KlgePeerWatcher must have valid self user id.");
        }

        Log.d(TAG, "<runOnPeerList / ZICO> incoming peer size: " + peers.size());

        KlgePeerNode myPeerNode = null;
        LinkedHashMap<String, KlgePeerNode> newRtcListMap = new LinkedHashMap<>();

        for (int i=0; i<peers.size(); i++) {
            KlgePeer peer = peers.get(i);
            KlgePeerNode node = KlgePeerNode.create(i, peer);
            newRtcListMap.put(peer.getUserId(), node);

            // find 'me' node
            if (mUserId.equals(peer.getUserId())) {
                myPeerNode = node;
                Log.d(TAG, "<runOnPeerList / ZICO> 나 : " + peer.getUserId());
            }
            else {
                Log.d(TAG, "<runOnPeerList / ZICO> mPeers size : " + mPeers.size());
                boolean currentCaller = newRtcListMap.get(peer.getUserId()).getPeer().isCaller();
                if (mPeers.containsKey(peer.getUserId())) {
                    boolean prevCaller = mPeers.get(peer.getUserId()).getPeer().isCaller();
                    Log.d(TAG, "<runOnPeerList / ZICO> Remote user prevCaller : " + prevCaller);
                    Log.d(TAG, "<runOnPeerList / ZICO> Remote user currentCaller : " + currentCaller);
                    if (prevCaller && !currentCaller) {
                        remoteNotCaller(peer.getUserId(), node);
                    } else if (!prevCaller && currentCaller) {
                        remoteCaller(peer.getUserId(), node);
                    }
                } else {
                    newRemoteCaller(peer.getUserId(), node);
                }
//                else {
//                    if (currentCaller) {
//                        remoteCaller(peer.getUserId(), node);
//                    }
//                }

            }

            // add new node to mPeers map.
            addPeerNode(node);
        }

        /*
        for (Map.Entry<String, KlgePeerNode> entry : newRtcListMap.entrySet()) {
            KlgePeer entryPeer = entry.getValue().mPeer;
            String entryUserNo = entry.getValue().mPeer.getUserId();
            boolean entryCaller = entry.getValue().mPeer.isCaller();

            if (myPeerNode.getPeer().getUserId().equals(entryUserNo))   // 나에 대한 노드는 Skip.
                continue;

            if (mPeers.isEmpty()) {  // rtcList 최초로 받았을 때
                if (entryCaller) {

                } else {

                }
            } else {  // 1회 이상 rtcList를 받았을 때
                if (!mPeers.containsKey(entryUserNo)) {
                    Log.d(TAG, "<runOnPeerList> 수업에서 퇴장한 유저는 삭제처리 한다. target : " + entryUserNo);
                    peerDestroy(entry);
                } else {
                    boolean prevCaller = mPeers.get(entryUserNo).getPeer().isCaller();
                    boolean currentCaller = entryPeer.isCaller();
                    Log.d(TAG, "<runOnPeerList> Remote user prevCaller : " + prevCaller);
                    Log.d(TAG, "<runOnPeerList> Remote user currentCaller : " + currentCaller);
                    if (prevCaller && !currentCaller) {
                        remoteNotCaller(entryUserNo, entry.getValue());
                    } else if (!prevCaller && currentCaller) {
                        remoteCaller(entryUserNo, entry.getValue());
                    }
                }
                if(!myPeerNode.mPeer.isCaller()) {
                    if (!entry.getValue().mPeer.isCaller()) {
                        Log.d(TAG, "<runOnPeerList> 비송출자 간의 PeerConnection은 끊는다. target : " + entry.getValue().getPeer().getUserId());
                        peerDestroy(entry);
                    }
                }
            }

            // add new node to mPeers map.
            addPeerNode(entry.getValue());
        }
        */

        // Step 1-1. 방에서 제거된 사용자 찾은 후 peer-connection 끊기
        // Step 1-2. 내가 비송출자일 경우, 비송출자인 peer와의 peer-connection 끊기
        for (Map.Entry<String, KlgePeerNode> entry : mPeers.entrySet()) {
            String userNo = entry.getValue().mPeer.getUserId();
            if (myPeerNode.getPeer().getUserId().equals(userNo))   // mPeers 맵에서 나에 대한 노드는 Skip.
                continue;

            if (!newRtcListMap.containsKey(entry.getKey())) {  // 새로운 rtcList(newMap)의 유저가 기존 rtcList(mPeers)에 존재하지 않는 유저면...
                //temp1.add(entry);
                peerDestroy(entry);
            }

            if (!entry.getValue().mPeer.isCaller()) {
                if(!myPeerNode.mPeer.isCaller()) {
                    Log.d(TAG, "<runOnPeerList / ZICO> 비송출자 간의 PeerConnection은 끊는다. target : " + entry.getValue().getPeer().getUserId());
                    peerDestroy(entry);
                }
            }
        }

        if(myPeerNode.mPeer.isCaller()) {
            Log.d(TAG, "<runOnPeerList / ZICO> 나는 송출자다!! 위치는 " + myPeerNode.getId() + "번째");
            peerCaller();    // rtcList를 한번 이상 받았다면 스트림 제어를 수행한다.
            peerCreate(myPeerNode, peers); // rtcList를 처음 받았을 때 내 뒤에 있는 녀석들에 대해서 커넥션을 생성한다.
        } else {
            Log.d(TAG, "<runOnPeerList / ZICO> 나는 비송출자다!! 위치는 " + myPeerNode.getId() + "번째");
            peerNotCaller();

            /*
            // Step 3. 자신이 비송출자 일 경우 다른 비송출자 와의 연결 끊기
            List<Map.Entry<String, KlgePeerNode>> temp2 = new LinkedList<>();
            for (Map.Entry<String, KlgePeerNode> entry : mPeers.entrySet()) {
                if (!entry.getValue().mPeer.isCaller()) {
                    //temp2.add(entry);
                    peerDestroy(entry);
                }
            }
            peerDestroy(temp2);
            */
        }

        /*
        if (me == null) {  // 최초 입장 시
            if (mMyPeerNode.mPeer.isCaller()) {

                // Stpe 2-1. 로컬 미디어 스트림을 만든다.,
                // Stpe 2-2. 자신이 송출자 일 경우 인덱스 뒤쪽에 있는 피어들과 연결을 다시 맺는다.
                peerCreate(mMyPeerNode, peers);
            } else {

            }
        } else {
            if (me.mPeer.isCaller()) {
                if (!mMyPeerNode.mPeer.isCaller()) {
                    Log.d(TAG, "<KlgePeerWatcher> 나는 비송출자 -> 송출자다!!");
                    // TODO "내가" 비송출자에서 송출자로 변경되었을 때의 처리가 필요하다.
                    // 1. 나의 로컬 스트림을 추가한다.
                    // 2. Offer를 다시 보낸다.
                }
                // Stpe 2-1. 로컬 미디어 스트림을 만든다.,
                // Stpe 2-2. 자신이 송출자 일 경우 인덱스 뒤쪽에 있는 피어들과 연결을 다시 맺는다.
                peerCreate(me, peers);
            } else {
                if(mMyPeerNode.mPeer.isCaller()) {
                    Log.d(TAG, "<KlgePeerWatcher> 나는 송출자 -> 비송출자다!!");
                    // TODO "내가" 송출자에서 비송출자로 변경되었을 때의 처리가 필요하다.
                    // 1 . 나의 로컬 스트림을 제거한다.
                }
                // Step 3. 자신이 비송출자 일 경우 다른 비송출자 와의 연결 끊기
                List<Map.Entry<String, KlgePeerNode>> temp2 = new LinkedList<>();
                for (Map.Entry<String, KlgePeerNode> entry : mPeers.entrySet()) {
                    if (!entry.getValue().mPeer.isCaller()) {
                        temp2.add(entry);
                    }
                }
                peerDestroy(temp2);
            }
        }
        */

        /*
        if (me.mPeer.isCaller()) {
            // Stpe 2-1. 로컬 미디어 스트림을 만든다.,
            // Stpe 2-2. 자신이 송출자 일 경우 인덱스 뒤쪽에 있는 피어들과 연결을 다시 맺는다.
            peerCreate(me, peers);
        }
        else {
            // Step 3. 자신이 비송출자 일 경우 다른 비송출자 와의 연결 끊기
            List<Map.Entry<String, KlgePeerNode>> temp2 = new LinkedList<>();
            for (Map.Entry<String, KlgePeerNode> entry : mPeers.entrySet()) {
                if (!entry.getValue().mPeer.isCaller()) {
                    temp2.add(entry);
                }
            }
            peerDestroy(temp2);
        }*/

        if (mRtcListListener != null)
            mRtcListListener.onPeerList();

        if (!mRtcListQueue.isEmpty())
            runOnPeerList();
    }


    private void peerCaller() {
        notifyOnPeerCaller();
    }


    private void peerNotCaller() {
        notifyOnPeerNotCaller();
    }


    private void remoteNotCaller(String toUserId, KlgePeerNode peer) {
        notifyOnRemoteNotCaller(toUserId, peer);
    }

    private void remoteCaller(String toUserId, KlgePeerNode peer) {
        notifyOnRemoteCaller(toUserId, peer);
    }

    private void newRemoteCaller(String toUserId, KlgePeerNode peer) {
        notifyOnNewRemoteCaller(toUserId, peer);
    }

    /**
     * 피어가 피어맵에서 제거 된다.
     * @param peer 제거
     */
    private void peerDestroy(Map.Entry<String, KlgePeerNode> peer) {
        mPeers.remove(peer.getKey());
        notifyOnPeerDestroy(peer.getKey(), peer.getValue());
    }

    /**
     * 피어가 피어맵에서 제거 된다.
     * @param peers 제거된 피어들의 목록
     */
    private void peerDestroy(List<Map.Entry<String, KlgePeerNode>> peers) {
        for (Map.Entry<String, KlgePeerNode> peer : peers) {
            mPeers.remove(peer.getKey());
            notifyOnPeerDestroy(peer.getKey(), peer.getValue());
        }
    }

    /**
     * Stpe 2-1. 로컬 미디어 스트림을 만든다.,
     * Stpe 2-2. 자신이 송출자 일 경우 인덱스 뒤쪽에 있는 피어들과 연결을 다시 맺는다.
     * @param me 자신의 피어 노드
     * @param peers onPeerList에서 들어오는 새로운 피어 리스트
     */
    private void peerCreate(KlgePeerNode me, List<KlgePeer> peers) {
        Log.d(TAG, "<peerCreate / ZICO>");
        for (int i = me.mIndex + 1; i < peers.size(); i++) {
            final String toUserId = peers.get(i).getUserId();

            /*
             * 새로운 사용자를 연결 해야하는 경우
             * 1. 피어맵에 피어노드가 있고,
             * 2. 연결된 상태가 아니면, (isConnected = false)
             * 3. peerCreate를 수행하고 isConnected = true로 업데이트한다.
             */
            if (mPeers.containsKey(toUserId)) {
                if(!mPeers.get(toUserId).isConnected()) {
                    Log.d(TAG, "<peerCreate / ZICO> 새로운 peer를 생성합니다.");
                    notifyOnPeerCreate(toUserId, mPeers.get(toUserId));
                    mPeers.get(toUserId).setIsConnected(true);
                }
            }
        }
    }

    /**
     * 새로운 피어가 들어올 경우 추가하며, 기존에 피어가 존재하다면 피어의 인덱스만 업데이트 한다.
     * @param node 새로 들어올 피어
     */
    private void addPeerNode(KlgePeerNode node) {
        final String id = node.mPeer.getUserId();
        if (!mPeers.containsKey(id)) {
            mPeers.put(id, node);
        } else {
            mPeers.get(id).setId(node.getId());
            mPeers.get(id).getPeer().setCaller(node.getPeer().isCaller());
            mPeers.get(id).getPeer().setStatus(node.getPeer().getStatus());
        }
    }

    private void notifyOnPeerCreate(String fromUserId, KlgePeerNode node) {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onPeerCreate(fromUserId, node);
        }
    }


    private void notifyOnPeerUpdate(String fromUserId, KlgePeerNode node) {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onPeerUpdate(fromUserId, node);
        }
    }

    public KlgePeerNode getPeerNode(String userId) {
        return mPeers.get(userId);
    }

    public int getPeerCount() {
        return mPeers.size();
    }

    public ConcurrentHashMap<String, KlgePeerNode> getPeers() {
        return mPeers;
    }

    private void notifyOnPeerDestroy(String fromUserId, KlgePeerNode node) {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onPeerDestroy(fromUserId, node);
        }
    }

    private void notifyOnRemoteNotCaller(String fromUserId, KlgePeerNode node) {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onRemoteNotCaller(fromUserId, node);
        }
    }

    private void notifyOnRemoteCaller(String fromUserId, KlgePeerNode node) {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onRemoteCaller(fromUserId, node);
        }
    }

    private void notifyOnNewRemoteCaller(String fromUserId, KlgePeerNode node) {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onNewRemoteCaller(fromUserId, node);
        }
    }

    private void notifyOnPeerCaller() {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onPeerCaller();
        }
    }

    private void notifyOnPeerNotCaller() {
        for (KlgePeerEvents event : mKlgePeerEvents) {
            event.onPeerNotCaller();
        }
    }

    public void addKlgePeerEventsListener(KlgePeerEvents events) {
        mKlgePeerEvents.add(events);
    }

    public void removeKlgePeerEventsListener(KlgePeerEvents events) {
        mKlgePeerEvents.remove(events);
    }


    public interface KlgePeerEvents {

        void onPeerCreate(String fromUserId, KlgePeerNode peer);

        void onPeerDestroy(String fromUserId, KlgePeerNode peer);

        void onRemoteNotCaller(String fromUserId, KlgePeerNode peer);

        void onRemoteCaller(String fromUserId, KlgePeerNode peer);

        void onNewRemoteCaller(String fromUserNo, KlgePeerNode peer);   // 2017.04.21 추가

        void onPeerUpdate(String fromUserId, KlgePeerNode peer);

        void onPeerCaller();

        void onPeerNotCaller();
    }

    public void addRtcListListener(RtcListListener event) {
        mRtcListListener = event;
    }

    public interface RtcListListener {
        void onPeerList();
    }

}
