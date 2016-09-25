package com.hw.diaosiclock.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hw.diaosiclock.R;
import com.hw.diaosiclock.model.AlarmDB;
import com.hw.diaosiclock.model.Country;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hw on 2016/9/22.
 */
public class SelectCountryActivity extends AppCompatActivity {
    public static final String ERRTAG = "SelectCountryActivity";

    private ArrayList<Country> countryList;
    private ListView countryListView;
    private CountryListAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country);

        AlarmDB db = AlarmDB.getInstance(this);
        if(null == db){
            Log.e(ERRTAG, "db is null");
            return;
        }

        final Intent intent = getIntent();
        if(null == intent) {
            Log.e(ERRTAG, "intent is null");
            return;
        }

        String countryName = intent.getStringExtra("country_name");
        if(null == countryName || 0 == countryName.length()) {
            Log.e(ERRTAG, "country name is illegal");
            return;
        }

        countryListView = (ListView)findViewById(R.id.country_list);
        if(null == countryListView) {
            Log.e(ERRTAG, "listview is null");
            return;
        }
        Cursor cursor = db.querySpecificCountry(countryName);
        countryList = new ArrayList<>();
        adapter = new CountryListAdapter(this, R.layout.country_info, countryList);
        countryListView.setAdapter(adapter);

        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Country country = countryList.get(position);
                if(null == country) {
                    Log.e(ERRTAG, "country list is wrong");
                    return;
                }

                Intent intent1 = new Intent(SelectCountryActivity.this, WeatherDetailActivity.class);
                intent1.putExtra("return_country_code", country.getCode());
                setResult(RESULT_OK, intent1);
                finish();
            }
        });

        if(cursor.moveToFirst()) {
            do {
                Country country = new Country();
                country.setCode(cursor.getString(cursor.getColumnIndex("code")));
                country.setProvinceName(cursor.getString(cursor.getColumnIndex("province")));
                country.setCityName(cursor.getString(cursor.getColumnIndex("city")));
                country.setCountryName(cursor.getString(cursor.getColumnIndex("country")));
                countryList.add(country);
            }while (cursor.moveToNext());
        }
        cursor.close();

        Toolbar toolbar = (Toolbar)findViewById(R.id.select_country_toolbar);
        if(null != toolbar) {
            toolbar.setTitle("\"" + countryName + "\"" + "的搜索结果:");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // 返回键点击逻辑
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }



    private class CountryListAdapter extends ArrayAdapter<Country> {
        int viewId;

        public CountryListAdapter(Context context, int viewId, List<Country> list) {
            super(context, viewId, list);
            this.viewId = viewId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(null == convertView) {
                view = LayoutInflater.from(getContext()).inflate(viewId, null);
            }else {
                view = convertView;
            }

            TextView countryName = (TextView)view.findViewById(R.id.country);
            TextView countryInfo = (TextView)view.findViewById(R.id.country_info);

            if(null != countryName && null != countryInfo) {
                Country country = getItem(position);
                if(null == country) {
                    Log.e(ERRTAG, "country is null");
                    return null;
                }
                countryName.setText(country.getCountryName());
                countryInfo.setText(country.getProvinceName() + " " + country.getCityName());
            }
            return view;
        }
    }
}
