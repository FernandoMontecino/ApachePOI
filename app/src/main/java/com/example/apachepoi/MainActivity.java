package com.example.apachepoi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
//import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements  SensorEventListener{


    Button btnGuardarExcel, btLeerExcel;
    TextView tvDatos;
    SensorManager sensorManager;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] gyroscopeReading = new float[3];

    //TextView texto;

    double ax, ay, az, mx, my, mz, gx, gy, gz, Latitud, Longitud;//xh,yh;
    int fila = 0;
    double Boton1, Boton2;
    //double roll,pitch,yaw;
    long time;

    Workbook wb = new HSSFWorkbook();
    Cell cell = null;
    Row row = null;
    Sheet sheet = null;
    LocationManager locationManager;
    LocationListener locationListener;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGuardarExcel = findViewById(R.id.btnGuardarExcel);
        btLeerExcel = findViewById(R.id.btnLeerExcel);
        tvDatos = findViewById(R.id.tvDatos);

        /* Del Programa de Sensores */
        //texto = (TextView) findViewById(R.id.texto);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        btnGuardarExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
            }
        });

        btLeerExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leer();
            }
        });

    }





    public void Tomardatos(View view) {
        Boton1 = 1;
        Boton2 = 0;

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        sheet = wb.createSheet("LISTA DE DATOS");

        row = sheet.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue("Timestamp");
        cell.setCellStyle(cellStyle);

        sheet.createRow(1);
        cell = row.createCell(1);
        cell.setCellValue("ax");
        cell.setCellStyle(cellStyle);

        sheet.createRow(2);
        cell = row.createCell(2);
        cell.setCellValue("ay");
        cell.setCellStyle(cellStyle);

        sheet.createRow(3);
        cell = row.createCell(3);
        cell.setCellValue("az");
        cell.setCellStyle(cellStyle);

        sheet.createRow(4);
        cell = row.createCell(4);
        cell.setCellValue("gx");
        cell.setCellStyle(cellStyle);

        sheet.createRow(5);
        cell = row.createCell(5);
        cell.setCellValue("gy");
        cell.setCellStyle(cellStyle);

        sheet.createRow(6);
        cell = row.createCell(6);
        cell.setCellValue("gz");
        cell.setCellStyle(cellStyle);

        sheet.createRow(7);
        cell = row.createCell(7);
        cell.setCellValue("Latitud");
        cell.setCellStyle(cellStyle);

        sheet.createRow(8);
        cell = row.createCell(8);
        cell.setCellValue("Longitud");
        cell.setCellStyle(cellStyle);

        Toast.makeText(getApplicationContext(),"INICIO DE TOMA DE DATOS",Toast.LENGTH_LONG).show();

    }

    public void guardar() {
        Boton1 = 0;
        Boton2 = 1;


        File file = new File(getExternalFilesDir(null),"Datos.xls");
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            wb.write(outputStream);
            Toast.makeText(getApplicationContext(),"FIN DE TOMA DE DATOS",Toast.LENGTH_LONG).show();
        } catch (java.io.IOException e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"NO OK",Toast.LENGTH_LONG).show();
            try {
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void leer() {
        File file = new File(this.getExternalFilesDir(null), "Datos.xls");
        FileInputStream inputStream = null;

        String datos = "";

        try {
            inputStream = new FileInputStream(file);

            POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream);

            HSSFWorkbook workbook = new HSSFWorkbook(fileSystem);

            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row>  rowIterator = sheet.rowIterator();

            while (rowIterator.hasNext()) {
                HSSFRow row = (HSSFRow) rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    HSSFCell cell = (HSSFCell) cellIterator.next();

                    datos = datos+" - "+cell.toString();

                }
                datos = datos+"\n";
            }

            tvDatos.setText(datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Del GPS
        LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocation(Location location) {

            }

            @Override
            public void onLocationChanged(@NonNull Location location) {
                Latitud = location.getLatitude();
                Longitud = location.getLongitude();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnable(String provider) {
            }

            public void onProviderDisable(String provider) {
            }
        };
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        //
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(sensorEvent.values, 0, gyroscopeReading,
                    0, gyroscopeReading.length);
        }

        //Etiqueta de tiempo para los sensores;
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss:ms").format(new Date());
        //time = new Date().getTime();

        ax = accelerometerReading[0];
        ay = accelerometerReading[1];
        az = accelerometerReading[2];
        mx = magnetometerReading[0];
        my = magnetometerReading[1];
        mz = magnetometerReading[2];
        gx= gyroscopeReading[0];
        gy= gyroscopeReading[1];
        gz= gyroscopeReading[2];


        // Ya teniendo los valores podemos calcular los angulos
        /*roll = Math.toDegrees(Math.atan(ax/az));
        pitch = Math.toDegrees(Math.atan(ay/az));
        //Estimación de yaw con magnetometro
        xh=mx * Math.toDegrees(Math.cos(roll))-my * Math.toDegrees(Math.sin(roll)) * Math.toDegrees(Math.sin(pitch))-
                mz * Math.toDegrees(Math.sin(roll)) * Math.toDegrees(Math.cos(pitch));
        yh=my * Math.toDegrees(Math.cos(pitch))-mz * Math.toDegrees(Math.sin(pitch));
        yaw= Math.toDegrees(Math.atan2(xh,yh));*/

        //TOMO LOS VALORES DE LOS ACELEROMETROS SOLAMENTE, PODRIAMOS TOMAR LOS VALORES DE ROLL, PITCH
        // Y YAW O HACER EL ANALISIS DESPUÉS
        //CON RESPECTO AL TIMESTAMP POR AHI HABRIA QUE VERLO DADO CAMBIA CADA CUATRO VALORES
        // OTRA QUE SE OCURRE ES TOMAR LAS FOTOS CADA 1 SEGUNDO, NO YA QUE NO SE SI ES NECESARIO TAN
        //SENSIBLE

        if (Boton1==1 && Boton2==0) {

            fila=fila+1;
            row = sheet.createRow(fila);
            cell = row.createCell(0);
            cell.setCellValue(timeStamp);

            cell = row.createCell(1);
            cell.setCellValue(ax);
            cell = row.createCell(2);
            cell.setCellValue(ay);
            cell = row.createCell(3);
            cell.setCellValue(az);

            cell = row.createCell(4);
            cell.setCellValue(gx);
            cell = row.createCell(5);
            cell.setCellValue(gy);
            cell = row.createCell(6);
            cell.setCellValue(gz);

            cell = row.createCell(7);
            cell.setCellValue(Longitud);
            cell = row.createCell(8);
            cell.setCellValue(Latitud);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
