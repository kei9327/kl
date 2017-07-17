package com.knowrecorder.Utils;

/**
 * Created by majej on 2017-06-13.
 * 스태틱 변수 정리
 */

public interface E {
    public interface ALPHA{
        public final float ACTIVE = 1f;
        public final float INACTIVE = 0.3f;
    }

    public interface DATE{
        public final int EARLY  = -1;
        public final int SAME   = 0;
        public final int LATE   = 1;
    }
}
