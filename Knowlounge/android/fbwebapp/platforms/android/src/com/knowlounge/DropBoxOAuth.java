package com.knowlounge;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.knowlounge.manager.WenotePreferenceManager;

import java.util.Locale;

//import com.dropbox.core.DbxException;
//import com.dropbox.core.DbxRequestConfig;
//import com.dropbox.core.v2.DbxClientV2;
//import com.dropbox.core.v2.files.ListFolderResult;
//import com.dropbox.core.v2.files.Metadata;
//import com.dropbox.core.v2.users.FullAccount;
//import com.knowlounge.manager.WenotePreferenceManager;

/**
 * Created by we160303 on 2016-04-15.
 */
public class DropBoxOAuth {

    private final String TAG = "DropBoxOAuth";
    private final String ACCESSTOKEN = "9lP2IrMEb7AAAAAAAAAACfUQJCCDr6KfXE3zAB06TK9CS9iYWDy32Kq8wW8SqYM5";
    private WenotePreferenceManager prefManager;

    public DropBoxOAuth(Context context){
        prefManager = WenotePreferenceManager.getInstance(context);
        prefManager.setDropBoxAccessToken(ACCESSTOKEN);
    }

    public DbxClientV2 CreateClient(Locale locale){

        DbxRequestConfig config = new DbxRequestConfig("dropbox/user", locale.getCountry());
        DbxClientV2 client = new DbxClientV2(config, ACCESSTOKEN);
        return client;
    }

    public FullAccount getCurrentAccount(DbxClientV2 client) throws DbxException {
        FullAccount account = client.users().getCurrentAccount();
        Log.d(TAG,client.users().toString());
        Log.d(TAG, account.getEmail());
        prefManager.setDropBoxID(account.getEmail());
        return account;
    }

    public void getFilesOrFolder(DbxClientV2 client) throws DbxException{
        ListFolderResult result = client.files().listFolder("");
        while (true){
            for(Metadata metadata : result.getEntries()){
                Log.d(TAG, metadata.getPathLower());
            }
            if(!result.getHasMore()){
                break;
            }
            result = client.files().listFolderContinue(result.getCursor());
        }
    }


}
