package com.admision.aall;
/*
 *Activity_Subpasos: Rescata los subpasos del seleccionado en "Activity_Desglose_Pasos_Convocatorias"
 * del archivo corrrespondiente a la convocatoria. Casos especiales:
 * (1)Si el paso tiene descripción agrega un texto al inicio
 * (2)Si el paso contiene en su título "REGISTRO EN INTERNET" agrega botón para acceder a Autoservicios
 * (3)Si la descripción de un subpaso es Fechas y Horarios agrega botón para desglosar las fechas y horarios.
 *Aletvia Lecona
 *09-10-2017
 * */

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.admision.aall.model.Lista_entrada;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Activity_Subpasos extends AppCompatActivity {
    ListView lista;
    ListView listView;
    String jsonString;
    private Semaforo ic= new Semaforo();
    ArrayList<Lista_entrada> datos = new ArrayList<>();
    ArrayList<Lista_entrada> datos1 = new ArrayList<>();
    EstadoConexion compConex= new EstadoConexion();
    Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
    HttpHandler sh = new HttpHandler();
    String actual;
    String mensaje;
    String filename;
    BroadcastReceiver bR;
    int band;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subpasos);
        final String tipo_convocatoria= getIntent().getExtras().getString("tipo_convocatoria");
        String paso=getIntent().getExtras().getString("paso");
        String fechaInicio="";
        String fechaFin ="";
        band=1;

        //JSON
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput(tipo_convocatoria + ".txt")));

            jsonString = fin.readLine();
            fin.close();
        }catch (Exception ex) {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }

        try {
            JSONArray convocatorias = new JSONArray(jsonString);

            //RECORRIDO DE JSON
            for (int i = 0; i < convocatorias.length(); i++) {
                final JSONObject convocatoria = convocatorias.getJSONObject(i);
                if (convocatoria.getString("titulo").equals(paso)) {
                    fechaInicio = convocatoria.getString("fechaInicio");
                    fechaFin = convocatoria.getString("fechaFin");
                    TableRow tableext = (TableRow) findViewById(R.id.textoAdicional);
                    TextView tipo_conv = new TextView(this);
                    tipo_conv.setText(convocatoria.getString("descripcion"));
                    tipo_conv.setTextColor(Color.parseColor("#003b5c"));
                    tipo_conv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    tipo_conv.setTextSize(15);
                    tipo_conv.setPadding(15,0,15,0);
                    tipo_conv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    //Caso especial (1)
                    if((convocatoria.getString("descripcion") != null) && (!convocatoria.getString("descripcion").equals(""))){
                        tableext.addView(tipo_conv);
                    }
                    //Caso especial (2)
                    if(convocatoria.getString("titulo").contains("REGISTRO EN INTERNET")){
                        Button btnTag = new Button(Activity_Subpasos.this);
                        btnTag.setLayoutParams(new TableRow.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                        btnTag.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if(compConex.verificaConexion(Activity_Subpasos.this)) {
                                    Uri uri2 = Uri.parse("http://webserver1.siiaa.siu.buap.mx:81/autoservicios/twbkwbis.P_WWWLogin");
                                    Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                                    startActivity(intent2);
                                }else{
                                    alerta.showAlertDialog(Activity_Subpasos.this, "Conexion a Internet",
                                            "Tu Dispositivo no tiene Conexión a Internet. Conéctate para acceder a Autoservicios.", false);
                                }
                            }
                        });
                        String te = "Ir a Autoservicios";
                        btnTag.setText(te);
                        tableext.addView(btnTag);
                    }
                    //Phone node is JSON object
                    JSONArray subpasos = convocatoria.getJSONArray("subpasos");
                    for (int l = 0; l < subpasos.length(); l++) {
                        final JSONObject subpaso = subpasos.getJSONObject(l);
                        String name = subpaso.getString("icono");
                        int resID = getResources().getIdentifier(name, "drawable", getPackageName());
                        if (subpaso.getString("descripcion").equals("Fechas y Horarios")){
                            final String hor=subpaso.getString("subfechas");
                            TableRow tableButton = (TableRow) findViewById(R.id.botonAdcicional);
                            Button btnTag = new Button(Activity_Subpasos.this);
                            btnTag.setLayoutParams(new TableRow.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
                            btnTag.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    MostrarHorarios(hor);
                                }
                            });
                            String str = "Fechas y Horarios";
                            btnTag.setText(str);
                            tableButton.addView(btnTag);
                        }else {
                            datos.add(new Lista_entrada(resID,
                                    subpaso.getString("descripcion"),
                                    subpaso.getString("subfechas")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
        }
        TextView titulo = (TextView) findViewById(R.id.tituloSubpaso);
        titulo.setText("Del "+ic.CambiarFormato(fechaInicio) +" al "+ic.CambiarFormato(fechaFin));
        //Adaptador de la lista datos con maq_img_txt_txt
        lista = (ListView) findViewById(R.id.listaSubpasos);
        lista.setAdapter(new Lista_adaptador(this, R.layout.maq_img_txt_txt, datos){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    TextView texto_superior_entrada = view.findViewById(R.id.txtSup);
                    if (texto_superior_entrada != null)
                        texto_superior_entrada.setText(((Lista_entrada) entrada).get_textoEncima());

                    TextView texto_inferior_entrada = view.findViewById(R.id.txtInf);
                    if (texto_inferior_entrada != null)
                        texto_inferior_entrada.setText(((Lista_entrada) entrada).get_textoDebajo());

                    ImageView imagen_entrada = view.findViewById(R.id.img);
                    if (imagen_entrada != null)
                        imagen_entrada.setImageResource(((Lista_entrada) entrada).get_idImagen());
                }
            }
        });
        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Subpasos.ActualizarInformacion().execute();
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
                    Activity_Subpasos.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog personalAlertDialog = new AlertDialog.Builder(Activity_Subpasos.this).create();
                            personalAlertDialog.setTitle("Actualización");
                            personalAlertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            personalAlertDialog.setIcon(R.drawable.avisos_a);
                            personalAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Subpasos.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            personalAlertDialog.show();
                        }
                    });
                }else {
                    Activity_Subpasos.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                            alerta.showAlertDialog(Activity_Subpasos.this, "ATENCIÓN",
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
    public void MostrarHorarios(String horario){
        listView=new ListView(this);
        datos1.clear();
        String[] parts = horario.split("&");
        for (int i = 0; i < parts.length; i++) {
            if(i==0){
                datos1.add(new Lista_entrada(R.drawable.adicional,
                        "Día para\nrecepción\nde \ndocumentos%Horario\nde\natención%Letra\ninicial de\napellido\npaterno",
                        "Día para\nrecepción\nde \ndocumentos"));}

            String[] part = parts[i].split("%");
            datos1.add(new Lista_entrada(R.drawable.adicional,
                    parts[i],
                    ic.CambiarFormato(part[0])));
        }
        //Adaptador de la lista con list_item
        listView.setAdapter(new Lista_adaptador(this, R.layout.maq_txt_txt_txt, datos1){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    String dia= ((Lista_entrada) entrada).get_textoEncima();
                    TextView texto_superior_entrada = view.findViewById(R.id.txt1);
                    TextView texto_intermedio_entrada = view.findViewById(R.id.txt2);
                    TextView texto_inferior_entrada = view.findViewById(R.id.txt3);
                    if (texto_superior_entrada != null) {
                        String[] part = dia.split("%");
                        texto_superior_entrada.setText(((Lista_entrada) entrada).get_textoDebajo());
                        texto_intermedio_entrada.setText(part[1]);
                        texto_inferior_entrada.setText(part[2]);
                    }
                }
            }
        });

        AlertDialog.Builder builder=new AlertDialog.Builder(Activity_Subpasos.this);
        builder.setCancelable(true);
        builder.setPositiveButton("OK",null);
        builder.setView(listView);
        AlertDialog dialog=builder.create();
        dialog.show();
    }
}
