package com.knowrecorder.develop.fragment.LeftMenu;

import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.NoteManager;
import com.knowrecorder.develop.model.realmHoler.NotesHolder;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by we160303 on 2017-02-02.
 */

public class leftMenuController {

    public leftMenuController() {
    }

    public ArrayList<NoteInformation> getNoteList(ArrayList<NoteInformation> list){
        int i = 0;
        list.clear();
        //todo file list 가져오기

        for(NotesHolder data : NoteManager.getInstance().getAllNote())
        {
            String noteName = data.getNoteName();
            String thumbNailPath = FilePath.NOTE_DIRECTORY+noteName+"/"+noteName+".png";
            String title = data.getTitle();
            String createDate = data.getCreateDate().substring(0,10);
            float totalTime = data.getTotaltime();

            list.add(new NoteInformation(noteName, thumbNailPath, title, createDate, (long)totalTime ));
        }

        return list;
    }
}
