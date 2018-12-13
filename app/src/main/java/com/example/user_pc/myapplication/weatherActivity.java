package com.example.user_pc.myapplication;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;

import static com.example.user_pc.myapplication.MessageReservation.activate;

public class weatherActivity extends AppCompatActivity {
    TimePicker alarm_timepicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        String[] cities = {"서울", "강릉", "광주", "대구", "대전", "부산", "제주"};

        final Spinner nativeFunctionSpinner = findViewById(R.id.nativeFunctionSpinner);
        final TextView weatherText = findViewById(R.id.weather);
        final Button weatherMessageButton = findViewById(R.id.weatherMessageButton);
        final Button messageCancelButton = findViewById(R.id.messageCancelButton);
        // final Calendar calendar = Calendar.getInstance();
        alarm_timepicker = findViewById(R.id.time_picker);

        MyAdapter adapter = new MyAdapter(this, R.layout.activity_adapter_element, cities);
        Spinner spinner = findViewById(R.id.nativeFunctionSpinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long ID) {
                weatherNow(weatherText, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        weatherMessageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Calendar calendar = Calendar.getInstance();
                int alarm_hour = alarm_timepicker.getHour();
                int alarm_minute = alarm_timepicker.getMinute();

                WeatherData weatherData = new WeatherData(weatherText.getText().toString(), alarm_hour, alarm_minute);
                Intent intent = new Intent(weatherActivity.this, messageActivity.class);
                intent.putExtra("weatherData", weatherData);
                startActivity(intent);
                finish();
            }
        });

        messageCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activate = false;
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("USER");
                myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "message").setValue(0);
                myRef.child(mAuth.getCurrentUser().getUid() + "/MACRO/" + "weather").setValue(0);
                Toast.makeText(getApplicationContext(), "날씨 정보 매크로를 취소하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });

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

class MyAdapter extends BaseAdapter {
    Context context;
    String[] cities;
    int resourceID;

    public MyAdapter(Context context, int resourceID, String[] cities) {
        this.context = context;
        this.cities = cities;
        this.resourceID = resourceID;
    }

    @Override
    public int getCount() {
        return cities.length;
    }

    @Override
    public Object getItem(int i) {
        return cities[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ConstraintLayout layout = (ConstraintLayout) view;
        if(layout == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (ConstraintLayout) inflater.inflate(resourceID, null);

            TextView textView = layout.findViewById(R.id.adapterText);

            textView.setText(cities[i]);
        }
        return layout;
    }
}

class WeatherAsyncTask extends AsyncTask<String, Void, String> {
    TextView textView;
    String result, sms, phoneNo, result_weather, additional_text;
    boolean smsSend;

    public WeatherAsyncTask(TextView textView) {
        this.textView = textView;
        smsSend = false;
    }

    public WeatherAsyncTask(String sms, String phoneNo) {
        this.sms = sms;
        this.phoneNo = phoneNo;
        smsSend = true;
    }

    @Override
    protected String doInBackground(String... strings) {
        String URL = strings[0];

        try {
            Document document = Jsoup.connect(URL).get();
            Element weather = document.select("div.w_now2").first();
            Elements now = weather.select("div.fl em");

            result = now.get(0).text();
            String result_celsius = result.substring(0, result.indexOf("℃") + 1);
            result_weather = result.substring(result.indexOf("℃") + 1, result.length());

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

        if (!smsSend) {
            textView.setText(result);
        }
        else {
            switch(result_weather) {
                case "맑음":
                    additional_text = "화창한 날씨에요. 오늘도 좋은 하루 되세요~";
                    break;
                case "구름조금":
                    additional_text = "구름이 조금 있지만 좋은 날씨에요";
                    break;
                case "구름많음":
                    additional_text = "구름이 조금 있지만 좋은 날씨에요";
                    break;
                case "흐림":
                    additional_text = "날씨가 흐리니 나중에 비가 올지 확인해주세요~";
                    break;
                case "비":
                    additional_text = "비가 오니 나가실 계획이라면 우산 꼭 챙겨주세요~";
                    break;
                case "눈":
                    additional_text = "눈이 오고 있으니 외출시 미끄러지지 않도록 조심하세요.";
                    break;
                default:
                    additional_text = "오늘의 날씨입니다.";
                    break;
            }
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, result +"\n" + sms + additional_text, null, null);
            Log.d("msgTest", "send Message");
        }
    }
}

/*
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class weatherActivity extends AppCompatActivity {
    int lat;
    int lon;
    int temprature;
    int cloudy;
    String city;

    public void setLat(int lat){ this.lat = lat;}
    public void setIon(int lon){ this.lon = lon;}
    public void setTemprature(int t){ this.temprature = t;}
    public void setCloudy(int cloudy){ this.cloudy = cloudy;}
    public void setCity(String city){ this.city = city;}

    public int getLat(){ return lat;}
    public int getIon() { return lon;}
    public int getTemprature() { return temprature;}
    public int getCloudy() { return cloudy; }
    public String getCity() { return city; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_on_activitiy);


        OpenWeatherAPITask t = new OpenWeatherAPITask();
        try {
            weatherActivity w = t.execute(lon, lat).get();
            System.out.println("Temp :" + w.getTemprature());
            TextView tem = (TextView) findViewById(R.id.weather);
            String temperature = String.valueOf(w.getTemprature());
            tem.setText(temperature);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}






class OpenWeatherAPIClient {

    final static String openWeatherURL = "http://api.openweathermap.org/data/2.5/weather";
    public weatherActivity getWeather(int lat,int lon){
        weatherActivity w;
        String urlString = openWeatherURL + "?lat="+lat+"&lon="+lon;

        try {
            // call API by using HTTPURLConnection
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JSONObject json = new JSONObject(getStringFromInputStream(in));

            w = parseJSON(json);
            w.setIon(lon);
            w.setLat(lat);

        } catch(MalformedURLException e){
            System.err.println("Malformed URL");
            e.printStackTrace();
            return null;

        }catch(JSONException e) {
            System.err.println("JSON parsing error");
            e.printStackTrace();
            return null;

        }catch(IOException e){
            System.err.println("URL Connection failed");
            e.printStackTrace();
            return null;
        }
        return w;
    }



    private weatherActivity parseJSON(JSONObject json) throws JSONException {
        weatherActivity w = new weatherActivity();
        w.setTemprature(json.getJSONObject("main").getInt("temp"));
        w.setCity(json.getString("name"));
        //w.setCloudy();
        return w;
    }



    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}

class OpenWeatherAPITask extends AsyncTask<Integer, Void, weatherActivity> {

    @Override
    public weatherActivity doInBackground(Integer... params) {
        OpenWeatherAPIClient client = new OpenWeatherAPIClient();
        int lat = params[0];
        int lon = params[1];

        // API 호출
        weatherActivity w = client.getWeather(lat,lon);

        return w;
    }
}
*///