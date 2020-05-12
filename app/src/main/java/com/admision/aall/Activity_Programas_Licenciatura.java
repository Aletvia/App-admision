package com.admision.aall;
/*
 *Activity_Programas_Licenciatura: Extrae las licenciaturas del  area seleccionada en "Activity_Categorias_Programas
 * En caso de tener URL se da la opción de descargar el programa a partir del botón correspondiente a su modalidad
 * Existen tres modalidades: Escolarizada (Esc), Semiescolarizada (MSE) y A distancia (MaD)
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class Activity_Programas_Licenciatura extends AppCompatActivity {
    String jsonString;
    EstadoConexion compConex= new EstadoConexion();
    Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
    TableLayout tabla;
    private  int num_celda=1;
    HttpHandler sh = new HttpHandler();
    String actual;
    String mensaje;
    String filename;
    BroadcastReceiver bR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__programas_licenciatura);

        tabla = (TableLayout) findViewById(R.id.tabla);

        //JSON
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput("Licenciaturas.txt")));

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
                if (convocatoria.getString("area").equals(getIntent().getExtras().getString("area"))) {
                    //CREACIÓN DE ELEMENTOS
                    //TEXTO
                    TableRow fila = new TableRow(this);
                    fila.setId(100 + i);
                    TextView tipo_conv = new TextView(this);
                    tipo_conv.setId(200 + i);
                    tipo_conv.setText(convocatoria.getString("programaEducativo"));
                    tipo_conv.setTextColor(Color.parseColor("#003b5c"));
                    tipo_conv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    tipo_conv.setTextSize(15);
                    tipo_conv.setPadding(10,5,5,5);
                    tipo_conv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    //IMAGE
                    ImageButton btn = new ImageButton(this);
                    final String Esc = convocatoria.getString("Esc");
                    if (Esc.equals("")) {
                        //btn.setBackgroundColor(Color.WHITE);
                        btn.setImageResource(R.drawable.program_no);
                    } else {
                        btn.setImageResource(R.drawable.program);
                        btn.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if(compConex.verificaConexion(Activity_Programas_Licenciatura.this)) {
                                    Uri uri = Uri.parse(Esc);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }else{
                                    alerta.showAlertDialog(Activity_Programas_Licenciatura.this, "Conexion a Internet",
                                            "Tu Dispositivo no tiene Conexión a Internet. Conéctate para poder descargar el programa.", false);
                                }
                            }
                        });
                    }

                    final String MSE = convocatoria.getString("MSE");
                    ImageButton btn1 = new ImageButton(this);
                    if (MSE.equals("")) {
                        //btn1.setBackgroundColor(Color.WHITE);
                        btn1.setImageResource(R.drawable.program_no);
                    } else {
                        btn1.setImageResource(R.drawable.program);
                        btn1.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Uri uri1 = Uri.parse(MSE);
                                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri1);
                                startActivity(intent1);
                            }
                        });
                    }
                    ImageButton btn2 = new ImageButton(this);
                    final String MaD = convocatoria.getString("MaD");
                    if (MaD.equals("")) {
                        //btn2.setBackgroundColor(Color.BLACK);
                        btn2.setImageResource(R.drawable.program_no);
                    } else {
                        btn2.setImageResource(R.drawable.program);
                        btn2.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Uri uri2 = Uri.parse(MaD);
                                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                                startActivity(intent2);
                            }
                        });
                    }
                    //AÑADE A LA FILA
                    fila.addView(tipo_conv);
                    fila.addView(btn);
                    fila.addView(btn1);
                    fila.addView(btn2);
                    tabla.addView(fila);
                    num_celda = num_celda + 2;
                }
            }
        } catch (Exception e) {
            Log.d("ReadPlacesFeedTask", e.getLocalizedMessage());
        }

        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Programas_Licenciatura.ActualizarInformacion().execute();
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
                    Activity_Programas_Licenciatura.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(Activity_Programas_Licenciatura.this).create();
                            alertDialog.setTitle("Actualización");
                            alertDialog.setMessage("Este módulo ha sido actualizado, presiona OK para salir de este apartado y entra de nuevo para ver los cambios.");
                            alertDialog.setIcon(R.drawable.avisos_a);
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Activity_Programas_Licenciatura.this, Activity_Home.class);
                                    startActivity(intent);
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }else {
                    Activity_Programas_Licenciatura.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
                            alerta.showAlertDialog(Activity_Programas_Licenciatura.this, "ATENCIÓN",
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
