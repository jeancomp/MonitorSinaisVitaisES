package br.lsdi.ufma.cddldemoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Sinais_vitais extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sinais_vitais);

        //readData();                  //Se você precisar ler o arquivo inteiro linha por linha
        readDataByColumn();        //Se você precisar ler colunas específicas linha por linha
    }

    private void readDataByColumn() {
        // Leia o arquivo csv bruto
        //InputStream is = getResources().openRawResource(R.raw.data);
        try{
            InputStream is = new BufferedInputStream(new FileInputStream("/storage/emulated/0/Download/pacientes/055/05500001.csv"));
            // Lê texto do fluxo de entrada de caracteres, armazenando em buffer caracteres para uma leitura eficiente
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            // Inicialização
            String line = "";

            // Handling exceptions
            try {
                br.readLine();
                // If buffer is not empty
                while ((line = br.readLine()) != null) {
                    // use comma as separator columns of CSV
                    String[] cols = line.split(",");

                    // Print in logcat
                    // + "', Column 1 = '" + cols[1] + "', Column 2: '" + cols[2] + "'"
                    System.out.println("Sinais vitais: " + cols[0] );
                }
                is.close();
            } catch (IOException e) {
                // Prints throwable details
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private void readData() {
        // Read the raw csv file
        //InputStream is = getResources().openRawResource(R.raw.data);
        try{
            InputStream is = new FileInputStream("/storage/emulated/0/Download/pacientes/055/05500001.csv");
            // Reads text from character-input stream, buffering characters for efficient reading
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );

            // Initialization
            String line = "";

            // Initialization
            try {
                // Step over headers
                reader.readLine();

                // If buffer is not empty
                while ((line = reader.readLine()) != null) {
                    Log.d("SinaisVitais", line);
                    // use comma as separator columns of CSV
                    //String[] tokens = line.split(",");
                    // Read the data
                    //WeatherSample sample = new WeatherSample();

                    // Setters
                    //sample.setMonth(tokens[0]);
                    //sample.setRainfall(Double.parseDouble(tokens[1]));
                    //sample.setSumHours(Integer.parseInt(tokens[2]));

                    // Adding object to a class
                    //weatherSamples.add(sample);

                    // Log the object
                    //Log.d("My Activity", "Just created: " + sample);
                    //Log.d("Sinais_vitais", "Leitura dos daos: " + tokens);
                }
                is.close();
            } catch (IOException e) {
                // Logs error with priority level
                Log.wtf("Sinais_vitais", "Erro ao ler arquivo" + line, e);

                // Prints throwable details
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
