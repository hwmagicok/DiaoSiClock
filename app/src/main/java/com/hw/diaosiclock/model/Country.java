package com.hw.diaosiclock.model;

/**
 * Created by hw on 2016/2/15.
 */
public class Country {
    private String CountryName;
    private String CountryCode;
    private String CountryEn;
    private String belongCityEn;
    

    public Country() {

    }

    public Country(final String name, final String cityEn, final String en, String countryCode) {
        if(null != name && null != en) {
            CountryName = name;
            CountryEn = en;
            belongCityEn = cityEn;
            CountryCode = countryCode;
        }
    }

    public String GetCountryName() {
        return CountryName;
    }

    public String GetCountryEn() {
        return CountryEn;
    }

    public String GetBelongCityEn() {
        return belongCityEn;
    }

    public String GetCountryCode() {
        return CountryCode;
    }

    public boolean SetCountryName(final String name){
        if(null != name && 0 < name.length()){
            CountryName = name;
            return true;
        }
        return false;
    }

    public boolean SetBelongCityEn(final String en) {
        if(null != en && 0 < en.length()) {
            belongCityEn = en;
            return true;
        }
        return false;
    }

    public boolean SetCountryCode(String countryCode) {
        CountryCode = countryCode;
        return true;
    }

    public boolean SetCountryEn(String en) {
        if(null != en && 0 < en.length()) {
            CountryEn = en;
            return true;
        }
        return false;
    }
}
