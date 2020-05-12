package com.admision.aall;
/*
 *Activity_Inicio_Sedes: Muestra el listado de las convocatorias activas a la fecha para posteriormente
 *  mostrar las sedes de registro en "Activity_Sedes_por_Convocatoria" en caso de no estar activa ninguna convocatoria
 *  se muestra en pantalla "No existen convocatorias activas por el momento"
 *Aletvia Lecona
 *05-11-2017
 * */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.admision.aall.model.Lista_entrada;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Activity_Inicio_Sedes extends AppCompatActivity {
    ListView lista;
    String jsonString;
    ArrayList<Lista_entrada> datos = new ArrayList<>();
    private Semaforo ic= new Semaforo();
    HttpHandler sh = new HttpHandler();
    String actual;
    String mensaje;
    String filename;
    BroadcastReceiver bR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sedes);

        //JSON
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput("Convocatorias.txt")));

            jsonString = fin.readLine();
            fin.close();
        }catch (Exception ex) {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }
        if (jsonString != null) {
            try {
                JSONArray convocatorias = new JSONArray(jsonString);

                //RECORRIDO DE JSON
                for (int i = 0; i < convocatorias.length(); i++) {
                    final JSONObject convocatoria = convocatorias.getJSONObject(i);

                    //extracción y comparación de datos json
                    String fechaInicio = convocatoria.getString("fechaInicio");
                    String fechaFin = convocatoria.getString("fechaFin");
                    String icono = ic.colorIcono(fechaInicio, fechaFin);

                    if (icono.equals("v")) {
                        datos.add(new Lista_entrada(R.drawable.siguiente,
                                convocatoria.getString("tipo"),
                                convocatoria.getString("clave")));
                    }
                }
            } catch (Exception e) {
                Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
            }
            if (datos.isEmpty()) {
                datos.add(new Lista_entrada(R.mipmap.i_convocatorias,
                        "No existen convocatorias activas por el momento",
                        "NA"));
            }
            //Adaptador de la lista con maq_img_txt_txt
            lista = (ListView) findViewById(R.id.listaConvocatoriaSedes);
            lista.setAdapter(new Lista_adaptador(this, R.layout.maq_txt_img, datos) {
                @Override
                public void onEntrada(Object entrada, View view) {
                    if (entrada != null) {
                        TextView texto_superior_entrada = view.findViewById(R.id.txtSup);
                        if (texto_superior_entrada != null)
                            texto_superior_entrada.setText(((Lista_entrada) entrada).get_textoEncima());

                        ImageView imagen_entrada = view.findViewById(R.id.img);
                        if (imagen_entrada != null)
                            imagen_entrada.setImageResource(((Lista_entrada) entrada).get_idImagen());
                    }
                }
            });

            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                    Lista_entrada elegido = (Lista_entrada) pariente.getItemAtPosition(posicion);
                    String convocatoria = elegido.get_textoDebajo();
                    if (!convocatoria.equals("NA")) {
                        Intent intent = new Intent(Activity_Inicio_Sedes.this, Activity_Sedes_por_Convocatoria.class);
                        intent.putExtra("convocatoria", convocatoria);
                        startActivity(intent);
                    }
                }
            });
        }else{
            AlertDialog alertDialog = new AlertDialog.Builder(Activity_Inicio_Sedes.this).create();
            alertDialog.setTitle("Conexion a Internet");
            alertDialog.setMessage("Tu Dispositivo no tiene Conexión a Internet. Conéctate y reinicia la aplicación para poder actualizar la información.");
            alertDialog.setIcon(R.drawable.avisos_a);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Activity_Inicio_Sedes.this, Activity_Home.class);
                    startActivity(intent);
                }
            });
            alertDialog.show();
        }
        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Inicio_Sedes.ActualizarInformacion().execute();
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
                if (mensaje.equals("Sedes")){
                    Activity_Inicio_Sedes.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(Activity_Inicio_Sedes.this).create();
                            alertDialog.setTitle("Actualización");
                            alertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            alertDialog.setIcon(R.drawable.avisos_a);
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Inicio_Sedes.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }else {
                    Activity_Inicio_Sedes.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                            alerta.showAlertDialog(Activity_Inicio_Sedes.this, "ATENCIÓN",
                                    "Se acaba de actualizar la sección: " + mensaje, true);
                        }
                    });
                }
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
