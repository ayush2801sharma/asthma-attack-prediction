package com.example.firebaseapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView textView1,textView2,textView3,textView4,textView5,textView01,textView02,textView03,textView04,textView05;
    private FirebaseDatabase mData;
    private DatabaseReference myRef;
    EditText etPlace;
    Button btSubmit;
    TextView tvAddress;
    EditText et;
    EditText et1;
    TextView tv;
    TextView tv1;
    EditText humidity,temperature,gas,heartrate,spo2,pm2,pm10,pefr;
    Button predict;
    TextView result;
    String url ="https://flask-app-4.onrender.com/predict";

    private String TAG="LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mData = FirebaseDatabase.getInstance();

        myRef = mData.getReference();

        textView1 = findViewById(R.id.FirebaseData1);
        textView01 = findViewById(R.id.FirebaseData01);
        textView2 = findViewById(R.id.FirebaseData2);
        textView02 = findViewById(R.id.FirebaseData02);
        textView3 = findViewById(R.id.FirebaseData3);
        textView03 = findViewById(R.id.FirebaseData03);
        textView4 = findViewById(R.id.FirebaseData4);
        textView04 = findViewById(R.id.FirebaseData04);
        textView5 = findViewById(R.id.FirebaseData5);
        textView05 = findViewById(R.id.FirebaseData05);

        etPlace = findViewById(R.id.et_place);
        btSubmit = findViewById(R.id.bt_submit);
        tvAddress = findViewById(R.id.tv_address);
       et= findViewById(R.id.et);
        tv = findViewById(R.id.tv);
        et1= findViewById(R.id.et1);
        tv1 = findViewById(R.id.tv1);
        humidity = findViewById(R.id.humidity);
        temperature = findViewById(R.id.temperature);
        gas = findViewById(R.id.gas);
        heartrate = findViewById(R.id.heartrate);
        spo2 = findViewById(R.id.spo2);
        pm2 = findViewById(R.id.pm2);
        pm10 = findViewById(R.id.pm10);
        pefr = findViewById(R.id.pefr);
        predict = findViewById(R.id.predict);
        result = findViewById(R.id.result);









        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    for(DataSnapshot snap:snapshot.getChildren()){
                        if(snap.getKey().matches("DHT")){
                            Map map = (Map)snap.getValue();
                            textView01.setText("Humidity(%):");
                            textView1.setText(map.get("humidity").toString());
                            textView02.setText("Temp (C):");
                            textView2.setText(map.get("temperature").toString());
                        }
                        if(snap.getKey().matches("Gas")){
                            Map map = (Map)snap.getValue();
                            Double ppm = Double.parseDouble(map.get("Sensor").toString());
                            ppm = ppm*9.5;
                            textView03.setText("Gas (ppm):");
                           textView3.setText(ppm.toString());
                        }
                        if(snap.getKey().matches("MAX")){
                            Map map = (Map)snap.getValue();
                            textView04.setText("Heart Rate:");
                            textView4.setText(map.get("heartrate").toString());
                            textView05.setText("O2 level(%): ");
                            textView5.setText(map.get("oxygen").toString());
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }


        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = etPlace.getText().toString();
                GeoLocation geoLocation = new GeoLocation();
                geoLocation.getAddress(address,getApplicationContext(),new GeoHandler());

            }
        });

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hit the api-> volley
                StringRequest stringRequest= new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String data = jsonObject.getString("result");
                                    if(data.equals("1")){
                                        result.setText("Higher risk Of asthma attack chances");
                                    }else{
                                        result.setText("You are in good health");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params= new HashMap<String,String>();
                        params.put("humidity",humidity.getText().toString());
                        params.put("temperature",temperature.getText().toString());
                        params.put("gas",gas.getText().toString());
                        params.put("heartrate",heartrate.getText().toString());
                        params.put("spo2",spo2.getText().toString());
                        params.put("pm2",pm2.getText().toString());
                        params.put("pm10",pm10.getText().toString());
                        params.put("pefr",pefr.getText().toString());
                        return  params;
                    }


                };
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(stringRequest);
            }
        });

    }
    private  class GeoHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            String address ;
            switch(msg.what){
                case 1:
                    Bundle bundle = msg.getData();
                    address = bundle .getString("address");
                    break;
                default:
                    address = null;

            }
            tvAddress.setText(address);
        }
    }

    public void get(View v){
        String apikey ="ddbf071c3cd3d6e5a1a9373851c1b8fe";
        String latitude = et.getText().toString();
        String longitude = et1.getText().toString();
        String url = "https://api.openweathermap.org/data/2.5/air_pollution?lat="+latitude+"&lon="+longitude+"&appid=ddbf071c3cd3d6e5a1a9373851c1b8fe";
        RequestQueue queue= Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request= new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //JSONObject object = response.getJSONObject("list");
                    JSONArray array= response.getJSONArray("list");
                    JSONObject object= array.getJSONObject(0);
                    JSONObject object1= object.getJSONObject("components");
                    Double pmtwo = object1.getDouble("pm2_5");
                    Double pmten = object1.getDouble("pm10");
                    tv.setText("PM2.5 :"+pmtwo);
                    tv1.setText("PM10 :"+ pmten);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please check coordinates.",Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
    }






}