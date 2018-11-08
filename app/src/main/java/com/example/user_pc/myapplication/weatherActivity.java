package com.example.user_pc.myapplication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class weatherActivity extends AppCompatActivity {
    private ArrayList<String> nativeFunctionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        final TextView weatherText = findViewById(R.id.weather);
        final Spinner nativeFunctionSpinner = findViewById(R.id.nativeFunctionSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nativeFunctionList);

        weatherNow(weatherText, 0);


        adapter.notifyDataSetChanged();

        nativeFunctionSpinner.setAdapter(adapter);
    }

    public void weatherNow(TextView weatherText, int city) {
        switch(city) {
            case 0: // 서울
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT001013");
                break;
            case 1: // 강릉
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT004001");
                break;
            case 2: // 광주
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT011005");
                break;
            case 3: // 대구
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT007007");
                break;
            case 4: // 대전
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT006005");
                break;
            case 5: // 부산
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT008008");
                break;
            case 6: // 제주
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT012005");
                break;
            default: // 서울
                new WeatherAsyncTask(weatherText).execute("https://weather.naver.com/rgn/cityWetrCity.nhn?cityRgnCd=CT001013");
        }
    }
}

class WeatherAsyncTask extends AsyncTask<String, Void, String> {
    TextView textView;

    public WeatherAsyncTask(TextView textView) {
        this.textView = textView;
    }

    @Override
    protected String doInBackground(String... strings) {
        String URL = strings[0];
        String result = "";

        try {
            Document document = Jsoup.connect(URL).get();
            Element weather = document.select("div.w_now2").first();
            Elements now = weather.select("div.fl em");

            result = now.get(0).text();
            String result_celsius = result.substring(0, result.indexOf("℃") + 1);
            String result_weather = result.substring(result.indexOf("℃") + 1, result.length());

            result = "기온: "+result_celsius+"\n날씨: "+result_weather;
            System.out.println(result);

            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        textView.setText(result);
    }
}
