/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.knowlounge.multipage.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultiPageDataProvider extends AbstractDataProvider {
    private List<ConcreteData> mData;
    private List<ConcreteData> lastData;
    private ConcreteData mLastRemovedData;
    private int mLastRemovedPosition = -1;

    private long pageid = 0;

    public static final int ITEM_VIEW_TYPE_SECTION_FOOTER = 2;
    public static final int ITEM_VIEW_TYPE_SECTION_ITEM = 1;

    public MultiPageDataProvider(JSONArray arr) {
        Log.d("MultiPageDataProvider", "생성자");
        mData = new LinkedList<>();
        lastData = new LinkedList<>();

        try{
            for(int i=0; i<arr.length(); i++ )
            {
                long id = pageid++;
                int viewType = ITEM_VIEW_TYPE_SECTION_ITEM;

                String pageid = arr.getString(i);
                mData.add(new ConcreteData(id, false, viewType, pageid));
            }

            {
                final long id = 99999;
                final int viewType = ITEM_VIEW_TYPE_SECTION_FOOTER;
                mData.add(new ConcreteData(id,true, viewType, ""));
            }

            lastData.addAll(mData);
        }catch(JSONException j){
            j.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final ConcreteData item = mData.remove(fromPosition);

        mData.add(toPosition, item);
    }

    @Override
    public void swapItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        Collections.swap(mData, toPosition, fromPosition);
    }


    @Override
    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        mData.remove(position);
        lastData.remove(position);
    }

    public void delItem(String pageId){
        for(ConcreteData data : mData){
            if(data.getPageId().equals(pageId))
            {
                mData.remove(data);
                lastData.remove(data);
                break;
            }
        }
    }

    public void addItem(String pageId){

        long id = pageid++;
        int viewType = ITEM_VIEW_TYPE_SECTION_ITEM;

        mData.add(mData.size()-1, new ConcreteData(id, false, viewType, pageId));
        lastData.add(lastData.size()-1, new ConcreteData(id, false, viewType, pageId));
    }

    public boolean isCurrent(int position){
        return false;
    }

    public void orderSuccess(boolean isSuccess){
        Log.d("printLogList", "변경전====================");
        printLogList();

        if(isSuccess){
            lastData.clear();
            lastData.addAll(mData);
        }else{
            mData.clear();
            mData.addAll(lastData);
        }
        Log.d("printLogList", "변경후====================");
        printLogList();
    }

    private void printLogList() {
        for(int i=0 ; i < mData.size()-1; i++){
            Log.d("printLogList", "pageId : " + mData.get(i).getPageId() + " : " + lastData.get(i).getPageId());
        }
    }

    public void reOrder(JSONArray arr){
        try{
            mData.clear();
            lastData.clear();
            pageid = 0;

            for(int i=0; i<arr.length(); i++ )
            {
                long id = pageid++;
                int viewType = ITEM_VIEW_TYPE_SECTION_ITEM;

                String pageid = arr.getString(i);
                mData.add(new ConcreteData(id, false, viewType, pageid));
            }

            {
                final long id = 99999;
                final int viewType = ITEM_VIEW_TYPE_SECTION_FOOTER;
                mData.add(new ConcreteData(id,true, viewType, ""));
            }
            lastData.addAll(mData);
        }catch(JSONException j){
            j.printStackTrace();
        }
    }

    public class ConcreteData extends Data {

        private final long mId;
        private final int mViewType;
        private final boolean mIsSectionFooter;
        private boolean mPinned;

        private String pageId;

        public ConcreteData(long id, boolean isSectionFooter, int viewType, String pageId) {
            this.mId = id;
            this.mIsSectionFooter = isSectionFooter;
            this.mViewType = viewType;
            this.pageId = pageId;
        }
        @Override
        public boolean isSectionFooter() {
            return mIsSectionFooter;
        }


        @Override
        public int getViewType() {
            return mViewType;
        }

        @Override
        public long getId() {
            return mId;
        }

        @Override
        public boolean isPinned() {
            return mPinned;
        }

        @Override
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }

        @Override
        public String getPageId() { return pageId ; }
    }
}
