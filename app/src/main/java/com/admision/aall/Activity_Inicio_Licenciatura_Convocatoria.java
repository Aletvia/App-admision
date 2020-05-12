package com.admision.aall;
/*
 *Activity_Inicio_Licenciatura_Convocatoria: El botón showDialogListView muestra el listado de carreras con
 * requisitos adicionales o curso de inducción y la información adicinal para los procesos
 * El botón y texto verConvocatoria dirige a "Activity_Desglose_Pasos_Convocatorias"
 * Los demás elementos son estáticos por lo que su edición deberá ser por medio del visor de layout
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

public class Activity_Inicio_Licenciatura_Convocatoria extends AppCompatActivity {
    ListView listView;
    String jsonString;
    String clave;
    ArrayList<Lista_entrada> datos = new ArrayList<>();
    HttpHandler sh = new HttpHandler();
    String actual;
    String mensaje;
    String filename;
    BroadcastReceiver bR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_licenciatura_convocatoria);
        clave=getIntent().getExtras().getString("convocatoria");
        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Inicio_Licenciatura_Convocatoria.ActualizarInformacion().execute();
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
                if (mensaje.equals("Convocatorias")){
                    Activity_Inicio_Licenciatura_Convocatoria.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog personalAlertDialog = new AlertDialog.Builder(Activity_Inicio_Licenciatura_Convocatoria.this).create();
                            personalAlertDialog.setTitle("Actualización");
                            personalAlertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            personalAlertDialog.setIcon(R.drawable.avisos_a);
                            personalAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Inicio_Licenciatura_Convocatoria.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            personalAlertDialog.show();
                        }
                    });
                }else {
                    Activity_Inicio_Licenciatura_Convocatoria.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                            alerta.showAlertDialog(Activity_Inicio_Licenciatura_Convocatoria.this, "ATENCIÓN",
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
    public void showDialogListView(View view){
        listView=new ListView(this);

        datos.clear();
        //JSON
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput("LicenciaturasPreProceso.txt")));

            jsonString = fin.readLine();
            fin.close();
        }catch (Exception ex) {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }
        try {
            JSONArray sedes = new JSONArray(jsonString);

            //RECORRIDO DE JSON
            for (int i = 0; i < sedes.length(); i++) {
                final JSONObject sede = sedes.getJSONObject(i);

                String texto = sede.getString("convocatorias");
                boolean resultado = texto.contains(clave);

                if(resultado) {
                    if (sede.getString("idTipoTramites").equals("indu")) {
                        datos.add(new Lista_entrada(R.drawable.induccion,
                                sede.getString("programaEducativo"),
                                sede.getString("informacion")));
                    }
                    if (sede.getString("idTipoTramites").equals("reqA")) {
                        datos.add(new Lista_entrada(R.drawable.adicional,
                                sede.getString("programaEducativo"),
                                sede.getString("informacion")));
                    }
                }
            }
        } catch (Exception e) {
            Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
        }//Adaptador de la lista con list_item
        listView.setAdapter(new Lista_adaptador(this, R.layout.list_item, datos){
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                Lista_entrada elegido = (Lista_entrada) pariente.getItemAtPosition(posicion);
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Inicio_Licenciatura_Convocatoria.this);
                builder.setMessage(elegido.get_textoDebajo())
                        .setTitle("ATENCIÓN")
                        .setCancelable(false)
                        .setNeutralButton("Aceptar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        AlertDialog.Builder builder=new AlertDialog.Builder(Activity_Inicio_Licenciatura_Convocatoria.this);
        builder.setCancelable(true);
        builder.setPositiveButton("OK",null);
        builder.setView(listView);
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    public void verConvocatoria(View view) {
        Intent intent = new Intent(Activity_Inicio_Licenciatura_Convocatoria.this, Activity_Desglose_Pasos_Convocatorias.class);
        intent.putExtra("convocatoria", getIntent().getExtras().getString("convocatoria"));
        intent.putExtra("color", getIntent().getExtras().getString("color"));
        intent.putExtra("pdf", getIntent().getExtras().getString("pdf"));
        startActivity(intent);
    }
}
