package br.lsdi.ufma.cddldemoapp;

import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private List<String> spinnerSensors;
    private ArrayAdapter<String> spinnerAdapter;

    private ListView listView;
    private List<String> listViewMessages;
    private ListViewAdapter listViewAdapter;
    private EditText filterEditText;

    private CDDL cddl;
    private String email = "jean.marques@lsdi.ufma.br";
    private List<String> sensorNames;
    private String currentSensor;
    private Subscriber subscriber;

    private boolean filtering;

    public Handler handler = new Handler();

    private String caminho = "/storage/emulated/0/Download/pacientes/055/05500001.csv";
    Button startButton;
    Button stopButton;
    Message msg = new Message();
    String ms;
    EventBus eb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        eb = EventBus.builder().build();
        eb.register(this);
        if (savedInstanceState == null) {
            configCDDL();
        }

        configSpinner();
        configListView();
        configStartButton();
        configStopButton();
        configClearButton();
        configFilterButton();
    }

    private void configCDDL() {
        //Host leva o nome do microBroker
        String host = CDDL.startMicroBroker();

        //Abre conecção
        Connection connection = ConnectionFactory.createConnection();
        connection.setHost(host);
        connection.setClientId(email);
        connection.connect();

        cddl = CDDL.getInstance();
        cddl.setConnection(connection);
        cddl.setContext(this);

        cddl.startService();
        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_ID);

        subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(cddl.getConnection());
        //subscriber.setSubscriberListener(this::onMessage);
        subscriber.setSubscriberListener(this::onMessageTopic);
    }

    private void onMessage(Message message) {

        handler.post(() -> {
            Object[] valor = message.getServiceValue();
            listViewMessages.add(0, StringUtils.join(valor, ", "));
            listViewAdapter.notifyDataSetChanged();
        });
    }

    public void onMessageTopic(Message message) {

        handler.post(() -> {
            Object valor = ms;
            listViewMessages.add(0, StringUtils.join(valor, ", "));
            listViewAdapter.notifyDataSetChanged();
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(MessageEvent event) {
    }

    @Override
    protected void onDestroy() {
        eb.unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        AppMenu appMenu = AppMenu.getInstance();
        appMenu.setMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppMenu appMenu = AppMenu.getInstance();
        appMenu.setMenuItem(MainActivity.this, item);
        return super.onOptionsItemSelected(item);
    }

    private void configSpinner() {
        //List<Sensor> sensors = cddl.getInternalSensorList();
        //sensorNames = sensors.stream().map(Sensor::getName).collect(Collectors.toList());

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensorNames);
        //spinner = findViewById(R.id.spinner);
        //spinner.setAdapter(adapter);
        List<String> listaPacientes = new ArrayList<>();
        listaPacientes.add("Paciente 055");
        listaPacientes.add("Paciente 201");
        listaPacientes.add("Paciente 403");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaPacientes);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
    }

    private void configListView() {
        listView = findViewById(R.id.listview);
        listViewMessages = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(this, listViewMessages);
        listView.setAdapter(listViewAdapter);
    }

    private void configStartButton() {
        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(e -> {
            // Identifica qual o paciente selecionado para monitorar os sinais vitais
            Spinner spinner = findViewById(R.id.spinner);
            String paciente_selecionado = spinner.getSelectedItem().toString();
            startSelectedSensor();
            //stopCurrentSensor();
            //startButton.setEnabled(false);
            //stopButton.setEnabled(true);
        });
    }

    private void startSelectedSensor() {
        //String selectedSensor = spinner.getSelectedItem().toString();
        //cddl.startSensor(selectedSensor); // inicializa o sensor

        //subscriber.subscribeServiceByName(selectedSensor);
        //currentSensor = selectedSensor;
        //cddl.startLocationSensor();
        readDataByColumn(caminho);
    }

    private void stopCurrentSensor() {
        //if (currentSensor != null) {
            //cddl.stopSensor(currentSensor);
        //}
    }

    private void configStopButton() {
        Button button = findViewById(R.id.stop_button);
        button.setOnClickListener(e -> stopCurrentSensor());
        //startButton.setEnabled(true);
        //stopButton.setEnabled(false);
    }

    private void configClearButton() {
        final Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(e -> {
            listViewMessages.clear();
            listViewAdapter.notifyDataSetChanged();
        });
    }

    private void configFilterButton() {

        filterEditText = findViewById(R.id.filter_edittext);

        Button button = findViewById(R.id.filter_button);
        button.setOnClickListener(e -> {
            if (filterEditText.getText().toString().equals(""))
                return;
            if (filtering) {
                subscriber.clearFilter();
                button.setText(R.string.filter_button_label);
            }
            else {
                subscriber.setFilter(filterEditText.getText().toString());
                button.setText(R.string.clear_filter_button_label);
            }
            filtering = !filtering;
        });
    }

    // Método ler o arquivo coluna por coluna
    public void readDataByColumn(String caminho) {
        try{
            InputStream is = new BufferedInputStream(new FileInputStream(caminho));
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line = "";
            try {
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    System.out.println("Sinais vitais: " + cols[0] );

                    // Passa a leitura do sensor para
                    //subscriber.setSubscriberListener(this::onMessage);
                    //Object obj = cols[0];
                    ms = "";
                    ms = cols[0];
                    onMessageTopic(msg);
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    // Método ler arquivo linha por linha
    public void readData( String caminho) {
        try{
            InputStream is = new FileInputStream(caminho);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            String line = "";
            try {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    Log.d("SinaisVitais", line);
                }
                is.close();
            } catch (IOException e) {
                Log.wtf("Sinais_vitais", "Erro ao ler arquivo" + line, e);
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
