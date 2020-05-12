package com.admision.aall;

/*
 *Activity_Desglose_Pasos_Convocatorias: Rescata los pasos de la convocatoria seleccionada en "Activity_Inicio_Convocatorias"
 *Aletvia Lecona
 *05-11-2017
 * */

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

public class Activity_Desglose_Pasos_Convocatorias extends AppCompatActivity {
    ListView listView;
    ListView lista;
    String jsonString;
    String tipo_convocatoria;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desglose__pasos__convocatorias);
        tipo_convocatoria= getIntent().getExtras().getString("convocatoria");
        final String estado= getIntent().getExtras().getString("color");
        if ( estado!= null){ if(estado.equals("a") || estado.equals("r")) { Alerta(estado); }}
        String conv  = getIntent().getExtras().getString("convocatoria");
        if (conv!=null) {
            if (conv.equals("LTSE") || conv.equals("MASD") || conv.equals("RL00")) {
                TableRow tableButton = (TableRow) findViewById(R.id.botonAdcicionalConv);
                Button btnTag = new Button(Activity_Desglose_Pasos_Convocatorias.this);
                btnTag.setLayoutParams(new TableRow.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
                btnTag.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        if(compConex.verificaConexion(Activity_Desglose_Pasos_Convocatorias.this)) {Guias();
                        }else{
                            alerta.showAlertDialog(Activity_Desglose_Pasos_Convocatorias.this, "Conexion a Internet",
                                    "Tu Dispositivo no tiene Conexión a Internet. Conéctate para poder descargar la guía temática.", false);
                        }
                    }
                });
                String text = "Descarga tu guía temática";
                btnTag.setText(text);
                tableButton.addView(btnTag);
            }
        }
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

                //extracción y comparación de datos json

                if(convocatoria.getString("titulo").equals("ATENCIÓN ASPIRANTE")){//AGREGA TEXTO DEL INICIO DE LA CONVOCATORIA
                    datos.add(new Lista_entrada(R.drawable.ojo,
                            convocatoria.getString("titulo") + "&" + " ",
                            convocatoria.getString("descripcion") + "&" + "v"));
                }else{
                    if(convocatoria.getString("titulo").equals("PUNTOS GENERALES")) {//AGREGA TEXTO DE LOS PUNTOS GENERALES
                        datos.add(new Lista_entrada(R.drawable.puntos_susp,
                                convocatoria.getString("titulo") + "&" + " ",
                                convocatoria.getString("descripcion") + "&" + "v"));
                    }else {
                        String fechaInicio = convocatoria.getString("fechaInicio");
                        String fechaFin = convocatoria.getString("fechaFin");
                        String iconoColor = ic.colorIcono(fechaInicio, fechaFin);
                        String name = convocatoria.getString("icono");
                        //Semaforo
                        if (iconoColor.equals("a")) {
                            name += "_a";
                        }
                        if (iconoColor.equals("v")) {
                            name += "_v";
                        }
                        if (iconoColor.equals("r")) {
                            name += "_r";
                        }

                        fechaInicio = convocatoria.getString("fechaInicio");
                        fechaFin = convocatoria.getString("fechaFin");
                        int resID = getResources().getIdentifier(name, "drawable", getPackageName());
                        datos.add(new Lista_entrada(resID,
                                convocatoria.getString("titulo") + "&" + "Del " + ic.CambiarFormato(fechaInicio) + " al " + ic.CambiarFormato(fechaFin),
                                convocatoria.getString("descripcion") + "&" + iconoColor));
                    }
                }
            }
        } catch (Exception e) {
            Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
        }
        //Adaptador de la lista con maq_txt_txt_img
        lista = (ListView) findViewById(R.id.listaPasos);
        lista.setAdapter(new Lista_adaptador(this, R.layout.maq_txt_txt_img, datos){
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    String texto = ((Lista_entrada) entrada).get_textoEncima();
                    String[] parts = texto.split("&");
                    String titulo = parts[0];
                    String fecha = parts[1];
                    TextView texto_superior_entrada =  view.findViewById(R.id.txtSup);
                    if (texto_superior_entrada != null)
                        texto_superior_entrada.setText(titulo);

                    TextView texto_inferior_entrada =  view.findViewById(R.id.txtInf);
                    if (texto_inferior_entrada != null)
                        texto_inferior_entrada.setText(fecha);

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
                String paso = elegido.get_textoEncima();
                String[] partes = paso.split("&");
                String titulo = partes[0];
                String texto = elegido.get_textoDebajo();
                String[] parts = texto.split("&");
                String color = parts[1];
                if ( color.equals("r") ) {
                    alerta.showAlertDialog(Activity_Desglose_Pasos_Convocatorias.this, "ALERTA DE VIGENCIA",
                            "Este paso ya se encuentra cerrado.", false);
                }
                else {
                    Intent intent = new Intent(Activity_Desglose_Pasos_Convocatorias.this, Activity_Subpasos.class);
                    intent.putExtra("paso", titulo);
                    intent.putExtra("tipo_convocatoria", tipo_convocatoria);
                    startActivity(intent);
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
                    new Activity_Desglose_Pasos_Convocatorias.ActualizarInformacion().execute();
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
                    Activity_Desglose_Pasos_Convocatorias.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog personalAlertDialog = new AlertDialog.Builder(Activity_Desglose_Pasos_Convocatorias.this).create();
                            personalAlertDialog.setTitle("Actualización");
                            personalAlertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            personalAlertDialog.setIcon(R.drawable.avisos_a);
                            personalAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Desglose_Pasos_Convocatorias.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            personalAlertDialog.show();
                        }
                    });
                }else {
                    Activity_Desglose_Pasos_Convocatorias.this.runOnUiThread(new Runnable() {
                        public void run() {
                            alerta.showAlertDialog(Activity_Desglose_Pasos_Convocatorias.this, "ATENCIÓN",
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
    public void btn_descarga(View view) {
        final String estado= getIntent().getExtras().getString("color");
        if(compConex.verificaConexion(this)) {
            if (estado!=null) {
                if (estado.equals("a") || estado.equals("r")) {
                    Alerta(estado);
                } else {
                    if(getIntent().getExtras().getString("pdf")!=null) {
                        Uri uri2 = Uri.parse(getIntent().getExtras().getString("pdf"));
                        Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                        startActivity(intent2);
                    } else {
                        alerta.showAlertDialog(this, "ALERTA",
                                "La convocatoria no se encuentra disponible para descargar.", false);
                    }
                }
            }

        }else{
            alerta.showAlertDialog(this, "Conexion a Internet",
                    "Tu Dispositivo no tiene Conexión a Internet. Conéctate para poder descargar la convocatoria.", false);
        }
    }
    public void Alerta(String color){
        if ( color.equals("a") ) {
            alerta.showAlertDialog(this, "ALERTA DE VIGENCIA",
                    "La información que verás a continuación corresponde a la convocatoria del año pasado. Espera el siguiente proceso de Admisión, recuerda que el icono deberá estar en color verde.", false);
        }
        else {
            alerta.showAlertDialog(this, "ALERTA DE VIGENCIA",
                    "Esta convocatoria ya se encuentra cerrada. Espera al próximo proceso de Admisión, recuerda que el icono deberá estar en color verde.", false);
        }
    }

    public void Guias(){
        if(compConex.verificaConexion(this)) {
            // Create an instance of a ListView
            listView=new ListView(this);
            datos1.clear();
            //JSON
            try
            {
                BufferedReader fin =
                        new BufferedReader(
                                new InputStreamReader(
                                        openFileInput("Areas.txt")));

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
                    datos1.add(new Lista_entrada(R.drawable.red,
                            convocatoria.getString("area"),
                            convocatoria.getString("guia")));
                }
            } catch (Exception e) {
                Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
            }//Adaptador de la lista con list_item
            listView.setAdapter(new Lista_adaptador(this, R.layout.list_item, datos1){
                @Override
                public void onEntrada(Object entrada, View view) {
                    if (entrada != null) {
                        TextView texto_superior_entrada =  view.findViewById(R.id.txtSup);
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
                    if (elegido.get_textoDebajo()!=null) {
                        Uri uri2 = Uri.parse(elegido.get_textoDebajo());
                        Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                        startActivity(intent2);
                    }
                }
            });

            AlertDialog.Builder builder=new AlertDialog.Builder(Activity_Desglose_Pasos_Convocatorias.this);
            builder.setCancelable(true);
            builder.setPositiveButton("OK",null);
            builder.setView(listView);
            AlertDialog dialog=builder.create();
            dialog.show();
        }else{
            alerta.showAlertDialog(this, "Conexion a Internet",
                    "Tu Dispositivo no tiene Conexión a Internet. Conéctate para poder descrgar la guía temática.", false);
        }
    }
}
