package com.admision.aall;
/*
 *Activity_Home: Muestra botones de conexión a las redes y a las pantallas principales
 *               Activity_Inicio_Bienvenida, Activity_Inicio_Oferta, Activity_Inicio_Convocatorias,
 *               Activity_Inicio_Avisos, Activity_Inicio_Sedes y Activity_Inicio_Acerca
 *               Todos sus elementos son estáticos, para editarlos ir al editor de layout
 *Aletvia Lecona
 *05-11-2017
 * */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.FileOutputStream;

public class Activity_Home extends AppCompatActivity {
    EstadoConexion compConex= new EstadoConexion();
    Personal_Alert_Dialog alerta= new Personal_Alert_Dialog();
    String actual;
    String filename;
    String mensaje;
    private BroadcastReceiver bR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String cambios= getIntent().getExtras().getString("cambios");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        String cambiosMensajes="\n";
        if (cambios!=null){
            if(cambios.contains("Avisos")){
                TextView tableext = (TextView) findViewById(R.id.txt_AVISOS);
                tableext.setTextColor(Color.parseColor("#00b5e2"));
                cambiosMensajes= cambiosMensajes+"Avisos\n";
            }
            if(cambios.contains("Oferta")){
                TextView tableext = (TextView) findViewById(R.id.txt_OFERTA);
                tableext.setTextColor(Color.parseColor("#00b5e2"));
                cambiosMensajes= cambiosMensajes+"Oferta\n";
            }
            if(cambios.contains("Convocatorias")){
                TextView tableext = (TextView) findViewById(R.id.txt_CONVOCATORIAS);
                tableext.setTextColor(Color.parseColor("#00b5e2"));
                cambiosMensajes= cambiosMensajes+"Convocatorias\n";
            }
            if(cambios.contains("Sedes")){
                TextView tableext = (TextView) findViewById(R.id.txt_SEDES);
                tableext.setTextColor(Color.parseColor("#00b5e2"));
                cambiosMensajes= cambiosMensajes+"Sedes\n";
            }
            alerta.showAlertDialog(this, "CAMBIOS",
                    "Existen cambios en: "+cambiosMensajes, true);
        }
        //actualización en tiempo real
        bR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                actual = intent.getStringExtra("actualizar");
                mensaje = intent.getStringExtra("mensaje");
                if (actual!=null) {
                    new Activity_Home.ActualizarInformacion().execute();
                }
            }
        };
    }
    private  class ActualizarInformacion extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
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
                outputStream.close();
                Activity_Home.this.runOnUiThread(new Runnable() {
                    public void run() {
                        alerta.showAlertDialog(Activity_Home.this, "ATENCIÓN",
                                "Se acaba de actualizar la sección: " + mensaje, true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    public void mensaje(){

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

    private void start(Class activity) {
        Intent i = new Intent(this, activity);
        startActivity(i);
    }
    //actualización en tiempo real
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(compConex.verificaConexion(this)) {
            switch (item.getItemId()){
                case R.id.FACEBOOK:
                    Uri uri = Uri.parse("https://www.linkedin.com/in/aletvia-anaid-l-042360124/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                case R.id.TWITTER:
                    Uri uri1 = Uri.parse("https://twitter.com/Aletvia_Lecona");
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri1);
                    startActivity(intent1);
                    return  true;
                case R.id.YOUTUBE:
                    //Toast.makeText(Activity_Home.this, "Add", Toast.LENGTH_SHORT).show();
                    Uri uri2 = Uri.parse("https://www.youtube.com/channel/UCRlgmkNBOG0DquJJpPWZliw?view_as=subscriber");
                    Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                    startActivity(intent2);
                    return  true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }else{
            alerta.showAlertDialog(this, "Conexion a Internet",
                    "Tu Dispositivo no tiene Conexión a Internet. Conéctate para poder acceder a la página.", false);
        }
        return false;
    }

    public void btn_BIENVENIDA(View view) {
        start(Activity_Inicio_Bienvenida.class);
    }
    public void btn_OFERTA(View view) {
        TextView tableext = (TextView) findViewById(R.id.txt_OFERTA);
        tableext.setTextColor(Color.parseColor("#000000"));
        start(Activity_Inicio_Oferta.class);
    }
    public void btn_CONVOCATORIAS(View view) {
        TextView tableext = (TextView) findViewById(R.id.txt_CONVOCATORIAS);
        tableext.setTextColor(Color.parseColor("#000000"));
        start(Activity_Inicio_Convocatorias.class);
    }
    public void btn_AVISOS(View view) {
        TextView tableext = (TextView) findViewById(R.id.txt_AVISOS);
        tableext.setTextColor(Color.parseColor("#000000"));
        start(Activity_Inicio_Avisos.class);
    }
    public void btn_SEDES(View view) {
        TextView tableext = (TextView) findViewById(R.id.txt_SEDES);
        tableext.setTextColor(Color.parseColor("#000000"));
        start(Activity_Inicio_Sedes.class);
    }
    public void btn_ACERCADE(View view) {
        start(Activity_Inicio_Acerca.class); }
}
