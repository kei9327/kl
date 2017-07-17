package com.knowlounge.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-03.
 */

public class KlgePeer {

    @SerializedName("userno")
    @Expose
    private String userno;
    @SerializedName("iscaller")
    @Expose
    private Boolean iscaller;
    @SerializedName("status")
    @Expose
    private KlgeStatus status;

    public String getUserId() {
        return userno;
    }

    public void setUserId(String userno) {
        this.userno = userno;
    }

    public Boolean isCaller() {
        return iscaller;
    }

    public void setCaller(Boolean iscaller) {
        this.iscaller = iscaller;
    }

    public KlgeStatus getStatus() {
        return status;
    }

    public void setStatus(KlgeStatus status) {
        this.status = status;
    }

}