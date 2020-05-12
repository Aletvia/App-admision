package com.admision.aall;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Semaforo: Compara las fechas de inicio y fin con la actual para saber el color del icono a mostrar
 * a=amarillo v=verde r=rojo
 * Devuelve en formato DD-MM-YYYY la fecha.
 * Aletvia Lecona
 * 12/10/2017.
 */

public class Semaforo {

    public Semaforo() {
    }

    public String colorIcono(String fechaI, String fechaF) {
        String[] parts = fechaF.split("-");
        int añoConvocatoria = Integer.parseInt(parts[0]);
        //Creamos la fecha actual.
        Date fechaActual = new Date();
        String fechaActual1 =new SimpleDateFormat("yyyy-MM-dd").format(fechaActual);
        //Pasamos ese String a un String con algún formato de fechas.
        SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd");
        fechaI = fechaI.replaceAll("UTC", "").trim();
        fechaF = fechaF.replaceAll("UTC", "").trim();
        fechaActual1 = fechaActual1.replaceAll("UTC", "").trim();
        Date fecha1 = null;
        Date fecha2 = null;
        Date fechaA = null;
        Calendar c = Calendar.getInstance();
        int añoActual = c.get(Calendar.YEAR);
        //Convertimos el String a Date, utilizando el formato que coincida con el String.
        try {
            fecha1 = formatoDelTexto.parse(fechaI);
            fecha2 = formatoDelTexto.parse(fechaF);
            fechaA = formatoDelTexto.parse(fechaActual1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //Hacemos la comprobación.
        int comparacionFechaInicio = fechaA.compareTo(fecha1);
        int comparacionFechaFin = fechaA.compareTo(fecha2);
        /*
        si  s1> s2, devuelve el número positivo
        si  s1 <s2, devuelve un número negativo
        si  s1 == s2, devuelve  0  */
        if (comparacionFechaFin > 0) {//fuera de fecha de termino
            return "r";
        }
        if (comparacionFechaFin == 0) {//en periodo
            return "v";
        }
        if ( comparacionFechaInicio == 0) {//en periodo
            return "v";
        }
        if (comparacionFechaInicio < 0 || añoConvocatoria < añoActual) { //fecha inicio menor a fecha actual
            return "a";
        }
        if (comparacionFechaInicio > 0 &&  comparacionFechaFin < 0 || comparacionFechaFin == 0 || comparacionFechaInicio == 0) {//en periodo
            return "v";
        }
        return "";
    }

    public String CambiarFormato(String fecha){
        String[] parts = fecha.split("-");
        String y = parts[0];
        String m = parts[1];
        String d = parts[2];
        return d+"-"+m+"-"+y;
    }
}
