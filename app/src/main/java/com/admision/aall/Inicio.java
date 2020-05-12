package com.admision.aall;

/*
 *Inicio: Aparece unos segundos mientras consulta el servicio guardando las consultas en archivos
 *       para su posterior  consulta en caso de tener conexión a internet y dirige a la actividad principal "Activity_Home"
 *Aletvia Lecona
 *09-10-2017
 * */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Inicio extends AppCompatActivity {
    String jsonString;
    String jsonArchivo;
    String cambios;
    static  int SPLASH_TIME_OUT = 2000;
    EstadoConexion compConex= new EstadoConexion();
    String [] urls = {"Oferta", "Licenciaturas", "Regionales", "SeccionesRegionales","Convocatorias", "LicenciaturasPreProceso", "Sedes", "Areas", "Avisos"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        if(compConex.verificaConexion(Inicio.this)) {
            new GetContacts().execute();
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent homeIntent = new Intent(Inicio.this, Activity_Home.class);
                    startActivity(homeIntent);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
    }


    private  class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            //CNSULTA SERVICIO GENERAL Y GUARDA EN ARCHIVO
            for (int i = 0; i  < 9; i++) {
                String url = "http://148.228.110.126/serv/" + urls[i];
                String jsonStr = sh.makeServiceCall(url);
                try //ALMACENA EN STRING EL ARCHIVO
                {
                    BufferedReader fin =
                            new BufferedReader(
                                    new InputStreamReader(
                                            openFileInput(urls[i]+".txt")));

                    jsonArchivo = fin.readLine();
                    fin.close();
                }catch (Exception ex) {
                    String filename = urls[i] + ".txt";
                    FileOutputStream outputStream;
                    try {
                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        outputStream.write(jsonStr.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
                    }
                }
                try {//CONVIERTE A JSONARRAY Y COMPARA
                    JSONArray jsonArc = new JSONArray(jsonArchivo);
                    JSONArray jsonUrl = new JSONArray(jsonStr);

                    if (jsonArc.toString().equals(jsonUrl.toString())) {
                        Log.e("","");
                    }else{
                        if (urls[i].equals("Oferta")|| urls[i].equals("Licenciaturas")|| urls[i].equals("Regionales")|| urls[i].equals("SeccionesRegionales")){
                            cambios=cambios + " Oferta ";
                        }
                        if (urls[i].equals("Convocatorias")|| urls[i].equals("LicenciaturasPreProceso")|| urls[i].equals("Areas")){
                            cambios=cambios + " Convocatorias ";
                        }
                        if (urls[i].equals("Sedes")){
                            cambios=cambios + " Sedes ";
                        }
                        if (urls[i].equals("Avisos")){
                            cambios=cambios + " Avisos ";
                        }
                        String filename = urls[i] + ".txt";
                        FileOutputStream outputStream;
                        try {
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(jsonStr.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
                }
            }

            //ABRE EL ARCHIVO DE CONVOCATORIAS
            try
            {
                BufferedReader fin =
                        new BufferedReader(
                                new InputStreamReader(
                                        openFileInput("Convocatorias.txt")));

                jsonString = fin.readLine();
                fin.close();

                //CONSULTAR CADA CONVOCATORIA Y GUARDAR
                try {
                    JSONArray convocatorias = new JSONArray(jsonString);

                    //RECORRIDO DE JSON
                    for (int i = 0; i < convocatorias.length(); i++) {
                        final JSONObject convocatoria = convocatorias.getJSONObject(i);
                        String urlConv = "http://148.228.110.126/serv/Convocatoria/" + convocatoria.getString("clave");
                        String jsonStr = sh.makeServiceCall(urlConv);

                        try//ALMACENA EN STRING EL ARCHIVO
                        {
                            fin =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    openFileInput(convocatoria.getString("clave")+".txt")));

                            jsonArchivo = fin.readLine();
                            fin.close();


                            try {//CONVIERTE A JSONARRAY Y COMPARA
                                JSONArray jsonArc = new JSONArray(jsonArchivo);
                                JSONArray jsonUrl = new JSONArray(jsonStr);

                                if (jsonArc.toString().equals(jsonUrl.toString())) {
                                    Log.e("","");
                                } else {
                                    cambios = cambios + " Convocatorias ";
                                    String filename = convocatoria.getString("clave") + ".txt";
                                    FileOutputStream outputStream;

                                    try {
                                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                        outputStream.write(jsonStr.getBytes());
                                        outputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
                            }
                        }catch (Exception ex) {
                            String filename = convocatoria.getString("clave") + ".txt";
                            FileOutputStream outputStream;

                            try {
                                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                outputStream.write(jsonStr.getBytes());
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
                }
            }catch (Exception ex) {
                Log.d("ReadPlacesFeedTask", ex.getLocalizedMessage());
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            Intent homeIntent = new Intent(Inicio.this, Activity_Home.class);
            homeIntent.putExtra("cambios", cambios);
            startActivity(homeIntent);
            finish();
        }
    }
}
