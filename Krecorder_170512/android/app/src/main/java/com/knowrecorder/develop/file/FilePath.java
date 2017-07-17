package com.knowrecorder.develop.file;

import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.knowrecorder.Youtube.Constants;
import com.knowrecorder.develop.manager.NoteManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SimpleTimeZone;

/**
 * Created by Changha on 2017-02-15.
 */

public class FilePath {

    public static boolean isOpencouse;

    public static String thisNoteName = null;

    public static String ROOT_DIRECTORY;
    public static String NOTE_DIRECTORY;
    public static String VIEWER_DIRECTORY;
    public static String TEMP_ZIP_DIRECTORY;

    public static String NOTE_FOLDER="";
    public static String IMAGES_DIRECTORY = "";
    public static String FILES_DIRECTORY = "";

    public static String VIEWER_FOLDER_NAME = null;
    public static String VIEWERS_LOW_FOLDER = null;
    public static String VIEWERS_REALM_FOLDER = null;
    public static String VIEWERS_IMAGES_DIRECTORY = null;
    public static String VIEWERS_FILES_DIRECTORY = null;

    public static void setFilePath(Context context) {
        File[] fileList = ContextCompat.getExternalFilesDirs(context, ContactsContract.Directory.PACKAGE_NAME);
        if(fileList.length > 0){
            ROOT_DIRECTORY = fileList[0].toString();
        }else{
            ROOT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/knowrecorder/";
        }
        NOTE_DIRECTORY = ROOT_DIRECTORY + "/notes/";
        VIEWER_DIRECTORY = ROOT_DIRECTORY + "/viewers/";
        TEMP_ZIP_DIRECTORY = ROOT_DIRECTORY + "/tempzip/";
    }

    public static void setNoteFolder(String noteName){
        thisNoteName = noteName;
        NOTE_FOLDER = NOTE_DIRECTORY + noteName + "/";
        IMAGES_DIRECTORY = NOTE_FOLDER + "images/";
        FILES_DIRECTORY = NOTE_FOLDER + "files/";

        makeFolder(NOTE_FOLDER);
        makeFolder(IMAGES_DIRECTORY);
        makeFolder(FILES_DIRECTORY);

    }

    public static void setViewerFolderName(String folderName){
        VIEWER_FOLDER_NAME = folderName;
    }

    public static void setViewersLowFolder(String folderName){
        VIEWERS_LOW_FOLDER = VIEWER_DIRECTORY + folderName + "/";
    }
    public static void setViewersRealmFolder(String realmFolder){
        if(VIEWERS_REALM_FOLDER == null) {
            setViewerFolderName(realmFolder);
            VIEWERS_REALM_FOLDER = VIEWERS_LOW_FOLDER + realmFolder + "/";
            VIEWERS_IMAGES_DIRECTORY = VIEWERS_REALM_FOLDER + "images/";
            VIEWERS_FILES_DIRECTORY = VIEWERS_REALM_FOLDER + "files/";
        }
    }

    public static void makeFolder(String path){
        File file;

        file = new File(path);
        if(!file.exists())
            file.mkdirs();
    }

    public static boolean isRealmFolderNull(){
        if(VIEWERS_REALM_FOLDER != null)
            return false;
        else
            return true;
    }

    public static void clearVIewers(){
        VIEWER_FOLDER_NAME = null;
        VIEWERS_LOW_FOLDER = null;
        VIEWERS_REALM_FOLDER = null;
        VIEWERS_IMAGES_DIRECTORY = null;
        VIEWERS_FILES_DIRECTORY = null;
    }


    public static String getImagesDirectory(){
        return isOpencouse ? VIEWERS_IMAGES_DIRECTORY : IMAGES_DIRECTORY ;
    }

    public static String getFilesDirectory() {
        return isOpencouse ? VIEWERS_FILES_DIRECTORY : FILES_DIRECTORY ;
    }

    public static int getNoteCount(){
        File dir = new File(NOTE_DIRECTORY);
        return dir.listFiles().length;
    }
    public static void clearNoteDirectory(){
        File dir = new File(NOTE_DIRECTORY);
        deleteRecursive(dir);
    }
    public static void deleteNote(String noteName){
        File dir = new File(NOTE_DIRECTORY+noteName+"/");
        deleteRecursive(dir);
        NoteManager.getInstance().deleteNote(noteName);
    }
    public static void deleteRecursive(File fileOrDirectory){
        if(fileOrDirectory.isDirectory())
            for(File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static void copyFile(File src, File dst) throws IOException {
        if(!dst.exists())
            dst.createNewFile();
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while((len = in.read(buf)) > 0 ) {
            out.write(buf, 0, len);
        }
    }




}
