package com.example.toto.projertbutstop;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class loaddata extends AppCompatActivity {

    private Handler handler;
    private Runnable runnable;
    private ProgressDialog pd;
    private MyOpenHelper myOpenHelper;
    private SQLiteDatabase sqLiteDatabase;
    //For BusStop
    public static final String TABLE_BUSSTOP = "busstopTABLE"; //ชื่อเทเบิล
    public static final String COLUMN_ID_BUSSTOP = "_id";
    public static final String COLUMN_Lat = "Lat";
    public static final String COLUMN_Lng = "Lng";
    public static final String COLUMN_Namebusstop = "Namebusstop";

    //For Bus
    public static final String TABLE_BUS = "busTABLE";
    public static final String COLUMN_ID_BUS = "id_bus";
    public static final String COLUMN_bus = "bus";
    public static final String COLUMN_bus_details = "bus_details";

    //For BusRoute
    public static final String TABLE_BUSROUTE = "busrouteTABLE";
    public static final String COLUMN_ID_BUSROUTE = "id_busroute";
    public static final String COLUMN_direction = "direction";
    private String urlJSON_BusStop = "http://jsontosqlite.esy.es/php_get_data_busstoptable.php";
    private String urlJSON_Bus = "http://jsontosqlite.esy.es/php_get_data_bustable.php";
    private String urlJSON_BusRoute = "http://jsontosqlite.esy.es/inner%20join.php";

    String[] urlStrings = new String[]{urlJSON_BusStop,
            urlJSON_Bus, urlJSON_BusRoute};
    //สร้างฐานข้อมูล
    public static final String DATABASE_NAME = "Busstop.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loaddata);

        myOpenHelper = new MyOpenHelper(loaddata.this);
        sqLiteDatabase = myOpenHelper.getWritableDatabase();

        //Check Database
        String tag = "18AugV1";
        if (checkDatabase()) {
            //Have Database
            Log.d(tag, "Have Database");
            checkNetAndUpdateSQLite(true);
        } else {
            //No Database
            Log.d(tag, "No Database");
            checkNetAndUpdateSQLite(false);
        }

    }//Main method

    private void deleteAllData() {

        SQLiteDatabase objSQLite = openOrCreateDatabase("Busstop.db", MODE_PRIVATE, null);
        objSQLite.delete("busstopTABLE", null, null);
        objSQLite.delete("busTABLE", null, null);
        objSQLite.delete("busrouteTABLE",null,null);

    }


    private void checkNetAndUpdateSQLite(boolean statusHaveDatabase) {
        String tag = "18AugV1";
        if (checkInternet()) {
            //Connected Internet OK
            Log.d(tag, "Connected Internet OK");
            deleteAllData();
            new LongRunTask().execute();
        } else {
            //Cannot Connected Internet Intent ==> Home.java
            Log.d(tag, "Cannot Connected Internet Intent ==> Home.java");
            if (statusHaveDatabase) {
                //Have Old Data
                Log.d(tag, "Have Old Data");
                //Toast.makeText(loaddata.this, "มีฐานข้อมูลแล้ว", Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myIntent();
                    }
                }, 3000);
            } else {
                //Empty Data and No Internet ==> Load Data from Server
                Log.d(tag, "Empty Data and No Internet ==> Load Data from Server");
                Toast.makeText(loaddata.this, "กรุณาเปิดอินเตอร์เน็ตและเข้าแอปพลิเคชันใหม่อีกครั้งเพื่อดาวโหลดข้อมูลสายรถประจำทาง", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void myIntent() {
        Intent intent = new Intent(loaddata.this, home.class);
        startActivity(intent);
        finish();
    }


    private boolean checkInternet() {
        boolean result = false; // No Internet
        ConnectivityManager connectivityManager = (ConnectivityManager) loaddata.this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ((networkInfo != null) && (networkInfo.isConnected())) {
            result  = true; // Have Internet
        }
        return result;
    }

    private boolean checkDatabase() {
        boolean result = true; // Have Database
        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM busstopTABLE", null);
        cursor.moveToFirst();
        Log.d("18AugV1", "cusor.count ==> " + cursor.getCount());
        if (cursor.getCount() == 0) {
            result = false; // No Database
        }
        return result;
    }

    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ออกจากแอปพลิเคชัน");
        dialog.setIcon(R.drawable.iconn);
        dialog.setCancelable(true);
        dialog.setMessage("คุณต้องการออกจากแอปพลิเคชัน หรือไม่?");

        dialog.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }


    private class LongRunTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            myOpenHelper = new MyOpenHelper(loaddata.this);
            sqLiteDatabase = myOpenHelper.getWritableDatabase();
            try {


                for (int x = 0; x < urlStrings.length; x += 1) {
                    pd.incrementProgressBy(1);
                    try {

                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request.Builder builder = new Request.Builder();
                        Request request = builder.url(urlStrings[x]).build();
                        Response response = okHttpClient.newCall(request).execute();
                        String strJSON = response.body().string();
                        Log.d("17AugV1", "JSoN ==> " + strJSON);
                        JSONArray jsonArray = new JSONArray(strJSON);
                        for (int i = 0; i < jsonArray.length(); i += 1) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            switch (x) {

                                case 0: // for BusStop

                                    String strCOLUMN_ID_BUSSTOP = jsonObject.getString("id");
                                    String strCOLUMN_Lat = jsonObject.getString("X");
                                    String strCOLUMN_Lng = jsonObject.getString("Y");
                                    String strCOLUMN_Namebusstop = jsonObject.getString("Namebusstop");
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(COLUMN_ID_BUSSTOP, strCOLUMN_ID_BUSSTOP);
                                    contentValues.put(COLUMN_Lat, strCOLUMN_Lat);
                                    contentValues.put(COLUMN_Lng, strCOLUMN_Lng);
                                    contentValues.put(COLUMN_Namebusstop, strCOLUMN_Namebusstop);
                                    sqLiteDatabase.insert(TABLE_BUSSTOP, null, contentValues);

                                    break;

                                case 1: // for Bus

                                    String strCOLUMN_ID_BUS = jsonObject.getString("id_bus");
                                    String strCOLUMN_bus = jsonObject.getString("bus");
                                    String strCOLUMN_bus_details = jsonObject.getString("bus_details");
                                    ContentValues contentValues1 = new ContentValues();
                                    contentValues1.put(COLUMN_ID_BUS, strCOLUMN_ID_BUS);
                                    contentValues1.put(COLUMN_bus, strCOLUMN_bus);
                                    contentValues1.put(COLUMN_bus_details, strCOLUMN_bus_details);

                                    sqLiteDatabase.insert(TABLE_BUS, null, contentValues1);

                                    break;

                                case 2: // for BusRoute

                                    String strCOLUMN_id_busroute  = jsonObject.getString("id_busroute");
                                    String strCOLUMN_direction = jsonObject.getString("direction");
                                    String strCOLUMN_bus1 = jsonObject.getString("bus");
                                    String strCOLUMN_bus_details1 = jsonObject.getString("bus_details");
                                    String strCOLUMN_Namebusstop1 = jsonObject.getString("Namebusstop");
                                    String strCOLUMN_Lat1 = jsonObject.getString("X");
                                    String strCOLUMN_Lng1 = jsonObject.getString("Y");
                                    ContentValues contentValues2 = new ContentValues();
                                    contentValues2.put(COLUMN_ID_BUSROUTE, strCOLUMN_id_busroute);
                                    contentValues2.put(COLUMN_direction, strCOLUMN_direction);
                                    contentValues2.put(COLUMN_bus, strCOLUMN_bus1);
                                    contentValues2.put(COLUMN_bus_details, strCOLUMN_bus_details1);
                                    contentValues2.put(COLUMN_Namebusstop, strCOLUMN_Namebusstop1);
                                    contentValues2.put(COLUMN_Lat, strCOLUMN_Lat1);
                                    contentValues2.put(COLUMN_Lng, strCOLUMN_Lng1);

                                    sqLiteDatabase.insert(TABLE_BUSROUTE, null, contentValues2);

                                    break;

                            }   // switch
                        }   //for
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(loaddata.this);
            pd.setIcon(R.drawable.iconn1);
            pd.setTitle("กำลังอัพเดทข้อมูล");
            pd.setMessage("Downloading โปรดรอ . . .");
            pd.setCancelable(false);
            pd.setMax(urlStrings.length);
            pd.setProgress(0);
            pd.setInverseBackgroundForced(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();
        }

        /*try{
                while (pd.getProgress()<pd.getMax()){
                    pd.incrementProgressBy(1);
                Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

        @Override
        protected void onPostExecute(String x) {
            super.onPostExecute(x);
            pd.dismiss();
            myIntent();
            Toast.makeText(loaddata.this,"อัพเดทข้อมูลเสร็จแล้ว",Toast.LENGTH_LONG).show();
        }

    }


}   // Main Class