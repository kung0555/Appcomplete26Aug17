package com.example.toto.projertbutstop;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class home extends AppCompatActivity {
    @Bind(R.id.Onlinemode)
    Button Onlinemode;
    @Bind(R.id.Offlinemode)
    Button Offlinemode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Toast.makeText(this, "เลือกระบบการใช้งาน ออนไลน์ กดด้านซ้ายของหน้าจอ ออฟไลน์ กดด้านขวาของหน้าจอ", Toast.LENGTH_SHORT).show();
    }

    @OnClick({R.id.Onlinemode, R.id.Offlinemode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.Onlinemode: {
                CheckGPS();
                Log.d("toto", "gg" + CheckGPS());
                checkInternet();
                Log.d("toto", "hh" + checkInternet());
                if (CheckGPS() == false) {
                    Toast.makeText(this, "คุณไม่ได้เปิดจีพีเอส กรุณาเปิดจีพีเอส", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("ต้องการเปิดหน้าตั้งค่าจีพีเอส");
                    dialog.setIcon(R.drawable.iconn);
                    dialog.setCancelable(true);
                    dialog.setMessage("ต้องการเปิดหน้าตั้งค่าจีพีเอส หรือไม่?");

                    dialog.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });

                    dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();

                } else if (CheckGPS() == true) {
                    Toast.makeText(this, "คุณกำลังเปิดจีพีเอส", Toast.LENGTH_SHORT).show();
                    if (checkInternet() == true) {
                        Toast.makeText(this, "คุณเชื่อมต่อจีพีเอสและอินเตอร์เน็ต", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, input_online.class);
                        startActivity(intent);
                        finish();
                    } else if (checkInternet() == false) {
                        Toast.makeText(this, "คุณไม่ได้เชื่อมต่ออินเตอร์เน็ต กรุณาเปิดอินเตอร์เน็ต", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                        dialog.setTitle("ต้องการเปิดหน้าตั้งค่าอินเตอร์เน็ต");
                        dialog.setIcon(R.drawable.iconn);
                        dialog.setCancelable(true);
                        dialog.setMessage("ต้องการเปิดหน้าตั้งค่าอินเตอร์เน็ต หรือไม่?");

                        dialog.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.setComponent(new ComponentName("com.android.settings",
                                        "com.android.settings.Settings$DataUsageSummaryActivity"));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(intent);
                            }
                        });

                        dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.setClassName("com.android.phone","com.android.phone.NetworkSetting");
                                startActivity(intent);*/
                            }
                        });

                        dialog.show();
                    }
                }
            }
            break;
            case R.id.Offlinemode: {
                CheckGPS();
                if (CheckGPS() == false) {
                    Toast.makeText(this, "คุณไม่ได้เปิดจีพีเอส กรุณาเปิดจีพีเอส", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("ต้องการเปิดหน้าตั้งค่าจีพีเอส");
                    dialog.setIcon(R.drawable.iconn);
                    dialog.setCancelable(true);
                    dialog.setMessage("ต้องการเปิดหน้าตั้งค่าจีพีเอส หรือไม่?");

                    dialog.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });

                    dialog.setNegativeButton("ยกเลิก", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();

                } else if (CheckGPS() == true) {
                    Toast.makeText(this, "คุณกำลังเปิดจีพีเอส", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, input_offline.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        }
    }

    private boolean checkInternet() {
        boolean result = false; // No Internet
        ConnectivityManager connectivityManager = (ConnectivityManager) home.this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ((networkInfo != null) && (networkInfo.isConnected())) {
            result = true; // Have Internet
        }
        return result;
    }

    private boolean CheckGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean bolGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return bolGPS;
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
}


