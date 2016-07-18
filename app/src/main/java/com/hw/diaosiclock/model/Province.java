package com.hw.diaosiclock.model;

/**
 * Created by hw on 2016/2/15.
 */
public class Province {
    private String ProvinceName;
    private String ProvinceCode;
    private String ProvinceEn;

    public Province() {

    }

    public Province(final String name, final String code, final String en) {
        if(null != name && 0 <= en.length()) {
            ProvinceName = name;
            ProvinceCode = code;
            ProvinceEn = en;
        }
    }

    public String GetProvinceName() {
        return ProvinceName;
    }

    public String GetProvinceCode() {
        return ProvinceCode;
    }

    public String GetProvinceEn() {
        return ProvinceEn;
    }

    public boolean SetProvinceName(final String name) {
        if(null != name) {
            ProvinceName = name;
            return true;
        }
        return false;
    }

    public boolean SetProvinceCode(final String code) {
        ProvinceCode = code;
        return true;
    }

    public boolean SetProvinceEn(final String en) {
        if(null != en) {
            ProvinceEn = en;
            return true;
        }
        return false;
    }
}
