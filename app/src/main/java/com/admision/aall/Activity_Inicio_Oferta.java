package com.admision.aall;
/*
 *Activity_Inicio_Oferta: Muestra el listado de niveles ofertados por la BUAP para mostrar los programas educativos
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.admision.aall.model.Lista_entrada;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class Activity_Inicio_Oferta extends AppCompatActivity {
    ListView lista;
    ArrayList<Lista_entrada> datos = new ArrayList<>();
    HttpHandler sh = new HttpHandler();
    String actual;
    String mensaje;
    String filename;
    BroadcastReceiver bR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferta);

        datos.add(new Lista_entrada(R.drawable.siguiente,"Preparatorias","Preparatorias"));
        datos.add(new Lista_entrada(R.drawable.siguiente,"Licenciaturas","Licenciaturas"));
        datos.add(new Lista_entrada(R.drawable.siguiente,"Complejos Regionales","SeccionesRegionales"));
        datos.add(new Lista_entrada(R.drawable.siguiente,"Programas Educativos Complementarios","complementarios"));
        //Adaptador de la lista con maq_txt_img
        lista = (ListView) findViewById(R.id.listaOferta);
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
                String tipo = elegido.get_textoEncima();
                if (tipo.equals("Preparatorias") || tipo.equals("Licenciaturas") || tipo.equals("Complejos Regionales")) {
                    Intent intent = new Intent(Activity_Inicio_Oferta.this, Activity_Categorias_Programas.class);
                    intent.putExtra("tipo", tipo);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(Activity_Inicio_Oferta.this, Activity_Programas_Educativos.class);
                    intent.putExtra("tipo", tipo);
                    intent.putExtra("area", elegido.get_textoDebajo());
                    intent.putExtra("nombre", elegido.get_textoEncima());
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
                    new Activity_Inicio_Oferta.ActualizarInformacion().execute();
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
                    Activity_Inicio_Oferta.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(Activity_Inicio_Oferta.this).create();
                            alertDialog.setTitle("Actualización");
                            alertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            alertDialog.setIcon(R.drawable.avisos_a);
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Inicio_Oferta.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }else {
                    Activity_Inicio_Oferta.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                            alerta.showAlertDialog(Activity_Inicio_Oferta.this, "ATENCIÓN",
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
