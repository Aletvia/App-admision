package com.admision.aall;
/*
 *Activity_Inicio_Bienvenida: Muestra una imagen, mensaje por parte de la dae,
 *                               logo buap y nombre de la dae
 *                               Todos sus elementos son estáticos, para editarlos ir al editor de layout
 *
 *Aletvia Lecona
 *05-11-2017
 * */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.FileOutputStream;

public class Activity_Inicio_Bienvenida extends AppCompatActivity {
    HttpHandler sh = new HttpHandler();
    String actual;
    String mensaje;
    String filename;
    BroadcastReceiver bR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);
        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Inicio_Bienvenida.ActualizarInformacion().execute();
                }

            }
        };
    }
    private  class ActualizarInformacion extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String url = "http://148.228.110.126/serv/"+actual;
            String jsonStr = sh.makeServiceCall(url);
            if(actual.contains("/")){
                String[] parts = actual.split("/");
                filename = parts[1] + ".txt";
            }else{
                filename = actual + ".txt";
            }
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(jsonStr.getBytes());
                Activity_Inicio_Bienvenida.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                        alerta.showAlertDialog(Activity_Inicio_Bienvenida.this, "ATENCIÓN",
                                "Se acaba de actualizar la sección: " + mensaje, true);
                    }
                });
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bR, new IntentFilter("my-event"));
    }
    //actualización en tiempo real
}
