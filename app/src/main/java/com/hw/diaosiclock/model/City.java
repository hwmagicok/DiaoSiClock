package com.hw.diaosiclock.model;

/**
 * Created by hw on 2016/2/15.
 */
public class City {
    private String CityName;
    private String CityCode;
    private String CityEn;
    private String belongProvinceEn;
    
    public City() {

    }

    public City(final String name, final String code, final String en, final String provinceEn) {
        if(null != name && null != code && null != provinceEn){
            CityName = name;
            CityCode = code;
            CityEn = en;
            belongProvinceEn = provinceEn;
        }
    }

    public String GetCityName() {
        return CityName;
    }

    public String GetCityCode() {
        return CityCode;
    }

    public String GetCityEn() {
        return CityEn;
    }

    public String GetBelongProvinceEn() {
        return belongProvinceEn;
    }

    public boolean SetCityName(final String name){
        if(null != name && 0 < name.length()){
            CityName = name;
            return true;
        }
        return false;
    }

    public boolean SetCityCode(final String code) {
        CityCode = code;
        return true;
    }

    public boolean SetCityEn(final String en) {
        if(null != en && 0 < en.length()) {
            CityEn = en;
            return true;
        }
        return false;
    }

    public boolean SetBelongProvinceEn(final String en) {
        if(null != en && 0 < en.length()) {
            belongProvinceEn = en;
            return true;
        }
        return false;
    }
}
