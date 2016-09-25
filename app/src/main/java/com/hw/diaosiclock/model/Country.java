package com.hw.diaosiclock.model;

/**
 * Created by hw on 2016/9/10.
 */
public class Country {
    private static final String ERRTAG = "Country";
    private String code;
    private String pinyin;
    private String countryName;
    private String cityName;
    private String provinceName;

    public Country() {
        code = null;
        pinyin = null;
        countryName = null;
        cityName = null;
        provinceName = null;
    }

    public Country(String code, String pinyin, String country, String city, String province) {
        this.code = code;
        this.pinyin = pinyin;
        this.countryName = country;
        this.cityName = city;
        this.provinceName = province;
    }

    public String getCode() {
        return code;
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setCode(String code) {
        if(null != code && 0 != code.length()) {
            this.code = code;
        }
    }

    public void setPinyin(String pinyin) {
        if(null != pinyin) {
            this.pinyin = pinyin;
        }
    }

    public void setCountryName(String name) {
        if(null != name) {
            countryName = name;
        }
    }

    public void setCityName(String name) {
        if(null != name) {
            cityName = name;
        }
    }

    public void setProvinceName(String name) {
        if(null != name) {
            provinceName = name;
        }
    }
}
