package com.knowrecorder.develop.model;

import android.util.Log;

import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.packetHolder.ObjectControllPacket;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.utils.PacketUtil;

import java.util.ArrayDeque;

/**
 * Created by ChangHa on 2017-02-28.
 */

public class PageStorage {
    private final String TAG = "PageStorage";
    private int pageNum;
    private ArrayDeque<Long> drawingPacketMid;
    private ArrayDeque<Long> undoStack;

    public PageStorage(int pageNum) {
        this.pageNum = pageNum;
        this.drawingPacketMid = new ArrayDeque<>();
        this.undoStack = new ArrayDeque<>();
    }

    public void addDrawingPacket(long mid){

        if(drawingPacketMid.contains(mid))
            return;

        Log.d(TAG, "page : "+pageNum + "  packet mid :" + mid);
        drawingPacketMid.add(mid);
    }


    public boolean undoPacket(boolean saved) {
        if(!saved)
        {
            if(drawingPacketMid.isEmpty()) {
                return false;
            }else {
                return true;
            }
        }

        if(!drawingPacketMid.isEmpty()) {
            long mid = drawingPacketMid.removeLast();
            undoStack.push(mid);
            Log.d(TAG, "page : "+pageNum + " undo packet mid :" + mid);
            
            if(saved) {
                ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                        .setType("undo")
                        .setAction(999)
                        .setTarget(mid)
                        .build();
                PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
            }

            return true;  // 비어 있지 않다.
        }
        return false; // 비어 있다.
    }

    public boolean redoPacket(boolean saved) {
        if(!saved)
        {
            if(undoStack.isEmpty()) {
                return false;
            }else {
                return true;
            }
        }

        if(!undoStack.isEmpty()) {
            long mid = undoStack.pop();
            drawingPacketMid.add(mid);
            Log.d(TAG, "page : "+pageNum + " redo packet mid :" + mid);
            if(ProcessStateModel.getInstanse().isRecording()){
                ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                        .setType("redo")
                        .setAction(999)
                        .setTarget(mid)
                        .build();
                PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
            }else {
                if(!ProcessStateModel.getInstanse().isPlaying())
                    RealmPacketPutter.getInstance().DeleteUndoPacket(PageManager.getInstance().getCurrentPageId());
            }

            return  true; // 비어 있지 않다.
        }
        return false; // 비어 있다.
    }

    public void clearUndoList() {
        undoStack.clear();
    }

    public boolean isContainPacket(long mid){
        return drawingPacketMid.contains(mid);
    }
    public boolean isContainUndoPacket(long mid){
        return undoStack.contains(mid);
    }

    public boolean undoListIsEmpty() { return undoStack.isEmpty();}

    public void clearPage(){
        drawingPacketMid.clear();
        undoStack.clear();
    }

}
