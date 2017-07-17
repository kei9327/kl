package com.knowlounge.sqllite;

import android.provider.BaseColumns;

/**
 * Created by we160303 on 2016-07-21.
 */
public class DataBases {
    public static final class CreateDB implements BaseColumns{
        public static final String _TABLENAME = "friend_list";
        public static final String NAME = "name";
        public static final String USER_ID = "id";
        public static final String THUMBNAIL = "thumbnail";

        public static final String _SEARCH_KEYWORD_TABLE = "search_keyword_list";
        public static final String SK_USER_ID = "user_id";
        public static final String SK_KEYWORD = "keyword";

        public static final String _CREATE =
                "create table "+_TABLENAME+"("
                    +_ID+" integer primary key autoincrement, "
                    +USER_ID+" text not null, "
                    +NAME+" text not null, "
                    +THUMBNAIL+" text not null );";

        public static final String _SEARCH_KEYWORD_CREATE =
                "create table "+_SEARCH_KEYWORD_TABLE+"("
                        +_ID+" integer primary key autoincrement, "
                        +SK_USER_ID+" text not null, "
                        +SK_KEYWORD+" text not null );";
    }
}
