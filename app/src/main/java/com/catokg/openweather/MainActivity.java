package com.catokg.openweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText                    editZipCode;
    TextView                    tvMain,tvDescription,tvTemp,tvHumidity,tvCountry,tvLocation;
    Button                      buttonWeather;
    WeatherConnectionTask       weatherConnectionTask;

    private class WeatherConnectionTask extends AsyncTask<String,Void,String>{

        // http connection initialization
        URL                     url;
        HttpURLConnection       httpURLConnection;
        InputStream             inputStream;
        InputStreamReader       inputStreamReader;
        BufferedReader          bufferedReader;
        String                  str;
        StringBuilder           stringBuilder;

        @Override
        protected String doInBackground(String... strings) {
            try
            {
                url = new URL(strings[0]);
                httpURLConnection= (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);

                bufferedReader = new BufferedReader(inputStreamReader);
                stringBuilder = new StringBuilder();

                str = bufferedReader.readLine();
                    while (str!=null){
                        stringBuilder.append(str);       //add each json line to string builder
                        str=bufferedReader.readLine();    //read each line from buffered reader
                    }
                    //return json to on post
                    return stringBuilder.toString();
            }

            catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("OpenWeather: ","URL IS WRONG" + e);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("OpenWeather: ","NETWORK PROBLEM" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            if(s==null){
                Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject j=new JSONObject(s);
                JSONArray arr=j.getJSONArray("weather");
                for(int i=0;i<arr.length();i++){

                    JSONObject obj=arr.getJSONObject(i);
                    String main=obj.getString("main");
                    String description=obj.getString("description");
                    tvMain.setText("Main: "+main);
                    tvDescription.setText("Description: "+description);

                }
                JSONObject main=j.getJSONObject("main");

                String temp=main.getString("temp");
                tvTemp.setText("Temp: "+temp);

                String humidity=main.getString("humidity");
                tvHumidity.setText("Humidity: "+humidity);

                JSONObject sys=j.getJSONObject("sys");

                String country=sys.getString("country");
                tvCountry.setText("Country: "+country);

                String name=j.getString("name");
                tvLocation.setText("Name: "+name);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // All view initialization
        editZipCode = findViewById(R.id.edit_ZipCode);
        buttonWeather = findViewById(R.id.button_weather);

        tvMain = findViewById(R.id.text_main);
        tvDescription = findViewById(R.id.text_description);
        tvTemp = findViewById(R.id.text_temp);
        tvHumidity = findViewById(R.id.text_humidity);
        tvCountry = findViewById(R.id.text_count);
        tvLocation = findViewById(R.id.text_location);

        // AsyncTask initialization
        weatherConnectionTask = new WeatherConnectionTask();

        buttonWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternet()){
                    //Internet available
                    weatherConnectionTask.execute("http://api.openweathermap.org/data/2.5/weather?zip="+editZipCode.getText().toString().trim()+",in&appid=bae1ba8dfee48308590210608acabab1");
                    buttonWeather.setEnabled(false);    //disable button to avoid extra clicks
                }
                else{
                    Toast.makeText(MainActivity.this, "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean checkInternet(){
        ConnectivityManager manager= (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=manager.getActiveNetworkInfo();

        if(info != null && info.isConnected()==true) {
            return true;    //Internet available
        }
        else{
            return false;   //No internet
        }
    }
}
