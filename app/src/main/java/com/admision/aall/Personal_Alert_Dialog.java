package com.admision.aall;
/*AlertDialog: Clase llamada para emitir avisos con el mensaje requerido
 *Aletvia Lecona
 *09-10-2017
 * */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Personal_Alert_Dialog {

    public Personal_Alert_Dialog(){}

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon((status) ? R.drawable.avisos_v : R.drawable.avisos_a);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create();
        builder.show();
    }
}
