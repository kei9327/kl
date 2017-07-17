package com.knowlounge.plugins;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.poll.DrawingPollFragment;
import com.knowlounge.fragment.poll.PollAnswerFragment;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Minsu on 2016-03-07.
 */
public class PollPlugin extends CordovaPlugin {

    private CallbackContext callbackContext = null;

    private final String TAG = "PollPlugin";

    private final String ACTION_GET_POLL_TMP_LIST = "getPollTmpList";
    private final String ACTION_GET_COMPLETE_POLL_LIST = "getCompletePollList";
    private final String ACTION_GET_COMPLETE_POLL_DETAIL = "getCompletePollDetail";
    private final String ACTION_GET_POLL_TMP_DETAIL = "getPollTmpDetail";
    private final String ACTION_MAKE_POLL_SHEET = "makePollSheet";
    private final String ACTION_ON_READY_SEND_POLL = "onReadySendPoll";
    private final String ACTION_EXIT_POLL = "exitPoll";
    private final String ACTION_SUCCESS_ANSWER = "successAnswer";
    private final String ACTION_CONFIRM_CAPTURE = "confirmCapture";
    private final String ACTION_ALERT_RECEIVE_POLL = "alertReceivePoll";
    private final String ACTION_MOVE_SUBROOM = "moveSubroom";
    private final String ACTION_MOVE_MYROOM = "moveMyRoom";
    private final String ACTION_UPDATE_ANSWER_USER = "updateAnswerUser";
    private final String ACTION_UPDATE_POLL_PROGRESS_STATE = "updatePollProgressState";
    private final String ACTION_SHOW_QUESTION_ARRIVED_DIALOG = "showQuestionArrivedDialog";

    private static EventListener mCallback;

    public interface EventListener {
        public void onExitPoll();
        public void onUpdateAnswerUser(String userNo);
    }
    public static void setOnEventListener(EventListener listener){
        mCallback = listener;
    }


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        //this.activity = (IndexActivity) cordova.getActivity();
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_GET_POLL_TMP_LIST)) {
            this.getPollTmpList(callbackContext, args);
        } else if (action.equals(ACTION_GET_COMPLETE_POLL_LIST)) {
            this.getCompletePollList(callbackContext, args);
        } else if (action.equals(ACTION_GET_COMPLETE_POLL_DETAIL)) {
            this.getCompletePollDetail(callbackContext, args);
        } else if (action.equals(ACTION_GET_POLL_TMP_DETAIL)) {
            this.getPollTmpDetail(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_MAKE_POLL_SHEET)) {
            this.makePollSheet(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_ON_READY_SEND_POLL)) {
            this.onReadySendPoll(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_EXIT_POLL)) {
            this.exitPoll(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_SUCCESS_ANSWER)) {
            this.successAnswer(callbackContext);
        } else if (action.equals(ACTION_CONFIRM_CAPTURE)) {
            this.confirmCapture(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_ALERT_RECEIVE_POLL)) {
            this.alertReceivePoll(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_MOVE_SUBROOM)) {
            this.moveSubroom(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_MOVE_MYROOM)) {
            this.moveMyRoom(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_UPDATE_ANSWER_USER)) {
            this.updateAnswerUser(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_UPDATE_POLL_PROGRESS_STATE)) {
            this.updatePollProgressState(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_SHOW_QUESTION_ARRIVED_DIALOG)) {
            this.showQuestionArrivedDialog(callbackContext, args.getJSONObject(0));
        }
        return true;
    }

    private void getPollTmpList(CallbackContext callbackContext, JSONArray arr) throws JSONException{
        // TODO : Activity로 JSONArray 전달하는 코드가 필요
        Log.d(TAG,"getPollTmpList");
        JSONObject obj = arr.getJSONObject(0);
//        RoomActivity.openPollDialogHandler(obj,0);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void getCompletePollList(CallbackContext callbackContext, JSONArray arr) {
        // TODO : Activity로 JSONArray 전달하는 코드가 필요
        Log.d(TAG, "getCompletePollList");
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void getCompletePollDetail(CallbackContext callbackContext, JSONArray arr) throws JSONException {
        // TODO : Activity로 JSONObject 전달하는 코드가 필요
        Log.d(TAG, "getCompletePollDetail");
        // handler 추가
        if(mCallback != null)
            mCallback.onExitPoll();
        JSONObject obj = arr.getJSONObject(0);
        RoomActivity.activity.openPollDialogHandler(obj, GlobalConst.ACTION_SHOW_POLL_RESULT);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void getPollTmpDetail(CallbackContext callbackContext, JSONObject obj) {
        // TODO : Activity로 JSONObject 전달하는 코드가 필요
        Log.d(TAG, "getPollTmpDetail");
//        RoomActivity.openPollDialogHandler(obj,0);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void makePollSheet(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        // TODO : Activity로 JSONObject 전달하는 코드가 필요
        Log.d(TAG, "makePollSheet");

        RoomActivity.activity.setPollAnswerData(obj);
        boolean isShow = obj.getBoolean("isshow");
        if (isShow)
            RoomActivity.activity.openPollDialogHandler(obj, GlobalConst.ACTION_MAKE_POLL_SHEET);
        RoomActivity.activity.setIsPollProgress(true);  // 참여자 쪽 폴 진행 상태값 업데이트
        RoomActivity.activity.setAnswerPollBtn();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void onReadySendPoll(CallbackContext callbackContext, JSONObject obj) {
        // TODO : Activity로 JSONObject 전달하는 코드가 필요.. 이 시점에서 pollno 값을 취하고, createPoll UI를 dismiss한 후, 폴 카운트다운 UI를 show 시켜주세요.
        Log.d(TAG, "onReadySendPoll");
        RoomActivity.activity.openPollDialogHandler(obj, GlobalConst.ACTION_SHOW_TIMER_PANEL);
        RoomActivity.activity.setIsPollProgress(true);  // 진행자 쪽 폴 진행 상태값 업데이트
        RoomActivity.activity.setAnswerPollBtn();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void exitPoll(CallbackContext callbackContext, JSONObject obj) {
        Log.d(TAG,"exitPoll : "+obj.toString());
        if (mCallback != null)
            mCallback.onExitPoll();
        RoomActivity.activity.setIsPollProgress(false);  // 참여자 쪽 폴 진행 상태값 업데이트
        RoomActivity.activity.setAnswerPollBtn();
        RoomActivity.activity.interruptPollTimerThread();
        RoomActivity.activity.dismissAlertDialog();  // 폴이 강제 종료되면 열려있는 AlertDialog를 닫아줌..
        RoomActivity.activity.closeDrawingPollNotifyFragment();   // 폴이 강제 종료되면 열려있는 다이얼로그 프레그먼트를 닫아줌..

        if (RoomActivity.activity.getIsSelectorMode()) {
            RoomActivity.activity.invokeAreaSelector("answer");
        }

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    /**
     * 답변이 완료되면 UI를 업데이트 해주는 메서드
     * @param callbackContext
     */
    private void successAnswer(CallbackContext callbackContext) {
        Log.d(TAG, "successAnswer");
        RoomActivity.activity.setIsPollProgress(false);  // 참여자 쪽 폴 진행 상태값 업데이트
        RoomActivity.activity.setAnswerPollBtn();
        if(mCallback != null)
            mCallback.onExitPoll();
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void confirmCapture(CallbackContext callbackContext, JSONObject obj) throws JSONException{
        Log.d(TAG, "[confirmCaputure] obj : " + obj.toString());

        boolean isFullScreen = obj.has("isfullscreen") ? obj.getBoolean("isfullscreen") : false;
        String binary = obj.has("img") ? obj.getString("img") : "";
        String mode = obj.has("mode") ? obj.getString("mode") : "";
        if(isFullScreen) {
            if (TextUtils.equals(mode, "question"))
                DrawingPollFragment._instance.applyFullScreenCapture(binary);
            else if (TextUtils.equals(mode, "answer"))
                PollAnswerFragment._instance.applyFullScreenCapture(binary);
        } else {
            RoomActivity.activity.confirmCapture(mode, binary);
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void alertReceivePoll(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(TAG, "alertReceivePoll");
        String mode = obj.getString("mode");
        String content = obj.getString("content");
        JSONObject data = obj.getJSONObject("data");

        RoomActivity.activity.openKnowloungePollDialog(mode, content, data);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    /**
     * 선생님 보드에서 학생이 판서형 질문을 받았을 때, 학생보드로 이동시켜 주는 메서드
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void moveSubroom(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(TAG, "moveSubroom");
        String roomCode = obj.getString("code");
        String pollNo = obj.getString("pollno");
        String timeLimit = obj.getString("timelimit");
        String isCountdown = obj.getString("iscountdown");

        Bundle bundle = new Bundle();
        bundle.putString("pollno", pollNo);
        bundle.putString("timelimit", timeLimit);
        bundle.putString("iscountdown", isCountdown);

        RoomActivity.activity.moveRoom(roomCode, bundle);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }

    private void moveMyRoom(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(TAG, "moveMyRoom");

        String pollNo = obj.getString("pollno");
        String timeLimit = obj.getString("timelimit");
        String isCountdown = obj.getString("iscountdown");

        Bundle bundle = new Bundle();
        bundle.putString("pollno", pollNo);
        bundle.putString("timelimit", timeLimit);
        bundle.putString("iscountdown", isCountdown);

        RoomActivity.activity.moveMyRoom(bundle);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));

    }

    // 응답한 유저들에 대한 UI 업데이트
    private void updateAnswerUser(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        String answerUserNo = obj.getString("userno");
        mCallback.onUpdateAnswerUser(answerUserNo);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void updatePollProgressState(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        boolean flag = obj.getBoolean("poll_state");
        RoomActivity.activity.setIsPollProgress(flag);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void showQuestionArrivedDialog(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        RoomActivity.activity.openDrawingPollNotifyFragment(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }
}
