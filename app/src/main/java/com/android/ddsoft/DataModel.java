package com.android.ddsoft;

import android.graphics.Bitmap;

public class DataModel {
    private String qty;
    private String wgt;
    private String clr;
    private String nts;
    private String rt;
    private String prc;
    private Bitmap img;

    public DataModel(Bitmap img, String qty, String wgt, String clr, String nts, String rt, String prc) {
        this.img = img;
        this.qty = qty;
        this.wgt = wgt;
        this.clr = clr;
        this.nts = nts;
        this.rt = rt;
        this.prc = prc;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getQty() {
        return qty;
    }

    public String getWgt() {
        return wgt;
    }

    public String getClr() {
        return clr;
    }

    public String getNts() {
        return nts;
    }

    public String getRt() {
        return rt;
    }

    public String getPrc() {
        return prc;
    }

}
