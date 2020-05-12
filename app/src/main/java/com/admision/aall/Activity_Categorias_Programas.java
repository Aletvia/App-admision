package com.admision.aall;
/*
 *Activity_Categorias_Programas: Dependiendo del nivel seleccionado en "Activity_Inicio_Oferta"
 * muestra un listado con las categorías de clasificacion:
 * Para Licenciaturas: Área de Ciencias Naturales y de la Salud, Área de Ciencias Sociales y Humanidades,
 *                     Área de Económico-Administrativas o Área de Ingenierías y Ciencias Exactas
 * Para Preparatorias: Preparatorias y Carreras Técnicas
 * Para Complejos Regionales: Listado de todos los complejos
 *Aletvia Lecona
 *05-11-2017
 * */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
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

public class Activity_Categorias_Programas extends AppCompatActivity {
    ListView lista;
    ArrayList<Lista_entrada> datos = new ArrayList<>();
    String jsonString;
    HttpHandler sh = new HttpHandler();
    String mensaje;
    String actual;
    String filename;
    BroadcastReceiver bR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias__programas);
        datos.clear();
        final String tipo= getIntent().getExtras().getString("tipo");
        TextView textoTitulo = (TextView) findViewById(R.id.textViewTitulo);
        textoTitulo.setText(tipo);
        if (tipo!=null && tipo.equals("Preparatorias")) {
            datos.add(new Lista_entrada(R.drawable.siguiente, "Preparatorias", "preparatoria"));
            datos.add(new Lista_entrada(R.drawable.siguiente, "Extensiones regionales", "regional"));
            datos.add(new Lista_entrada(R.drawable.siguiente, "Carrera Técnica", "tecnico"));
        }else{
            String archivo;
            if (tipo!=null && tipo.equals("Complejos Regionales")){
                archivo = "SeccionesRegionales.txt";
            }else {
                archivo = "Areas.txt";
                TableRow tableButton = (TableRow) findViewById(R.id.infoAdicional);
                TextView tipo_conv = new TextView(this);
                tipo_conv.setText("En respuesta a las necesidades y expectativas de la comunidad universitaria contamos con las siguientes modalidades: Escolarizada (Esc), Modalidad a Distancia (MaD) y Modalidad Semiescolarizada (MSE).\n\nLa disponibilidad para cada Licenciatura se encuentran marcadas en la fila correspondiente de cada una.");
                tipo_conv.setTextColor(Color.parseColor("#003b5c"));
                tipo_conv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                tipo_conv.setTextSize(15);
                tipo_conv.setPadding(10, 5, 5, 5);
                tipo_conv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                tableButton.addView(tipo_conv);
            }
            //JSON
            try
            {

                BufferedReader fin =
                        new BufferedReader(
                                new InputStreamReader(
                                        openFileInput(archivo)));
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
                        datos.add(new Lista_entrada(R.drawable.siguiente,
                                convocatoria.getString("area"),
                                convocatoria.getString("clave")));
                    }
                } catch (Exception e) {
                    Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
                }
            }else{
                AlertDialog alertDialog = new AlertDialog.Builder(Activity_Categorias_Programas.this).create();
                alertDialog.setTitle("Conexion a Internet");
                alertDialog.setMessage("Tu Dispositivo no tiene Conexión a Internet. Conéctate y reinicia la aplicación para poder actualizar la información.");
                alertDialog.setIcon(R.drawable.avisos_a);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Activity_Categorias_Programas.this, Activity_Home.class);
                        startActivity(intent);
                    }
                });
                alertDialog.show();
            }
        }
        //Adaptador de la lista con maq_img_txt_txt
        lista = (ListView) findViewById(R.id.listaCategorias);
        lista.setAdapter(new Lista_adaptador(this, R.layout.maq_txt_img, datos){
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
                Intent intent;
                String nombre;
                String area = elegido.get_textoDebajo();
                if (tipo!=null && tipo.equals("Licenciaturas")) {
                    intent = new Intent(Activity_Categorias_Programas.this, Activity_Programas_Licenciatura.class);
                    nombre = elegido.get_textoDebajo();
                } else {
                    intent = new Intent(Activity_Categorias_Programas.this, Activity_Programas_Educativos.class);
                    nombre = elegido.get_textoEncima();
                }
                intent.putExtra("tipo", tipo);
                intent.putExtra("area", area);
                intent.putExtra("nombre", nombre);
                startActivity(intent);
            }
        });
        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Categorias_Programas.ActualizarInformacion().execute();
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
                if (mensaje.equals("Oferta")){
                    Activity_Categorias_Programas.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog personalAlertDialog = new AlertDialog.Builder(Activity_Categorias_Programas.this).create();
                            personalAlertDialog.setTitle("Actualización");
                            personalAlertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            personalAlertDialog.setIcon(R.drawable.avisos_a);
                            personalAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Categorias_Programas.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            personalAlertDialog.show();
                        }
                    });
                }else {
                    Activity_Categorias_Programas.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                            alerta.showAlertDialog(Activity_Categorias_Programas.this, "ATENCIÓN",
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
