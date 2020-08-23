package br.lsdi.ufma.cddldemoapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
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
    private String email = "jean.marques@lsdi.ufma.br";  // Observacao importante para topico definido: mhub/+/service_topic/my-service
    private List<String> sensorNames;
    private String currentSensor;
    private Subscriber subscriber;
    private Publisher pub;
    private boolean filtering;

    public Handler handler = new Handler();

    private String caminho = "/storage/emulated/0/Download/pacientes/055/05500001.csv";
    InputStream is;
    BufferedReader reader;
    Button startButton;
    Button stopButton;
    Message msg = new Message();
    String monitorCode;
    public AlertDialog alerta;
    String ms;
    EventBus eb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
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

    private void configArquivo() {
        try {
            is = new FileInputStream(caminho);
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private void configCDDL() {
        //Host leva o nome do microBroker
        //String host = CDDL.startMicroBroker();
        String host = "broker.hivemq.com";

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

        pub = PublisherFactory.createPublisher();
        pub.addConnection(cddl.getConnection());

        subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(cddl.getConnection());

        subscriber.subscribeServiceByName("my-service");
        subscriber.setSubscriberListener(this::onMessage);

        // EWS  Frequência  respiratória
        // Sinal:RESP Valor: 15 >= x <= 20
        monitorCode = subscriber.getMonitor().addRule("select * from Message where cast(serviceValue[0], string)='RESP' and cast(serviceValue[1], double)>=16 and cast(serviceValue[1], double)<=20", message -> {
            new Thread() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //atualizaTotalAlerta(t1,1);
                            if(true) {
                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                                geraAlerta(1, msgAlerta(message));
                            }
                        }
                    });
                }
            }.start();});

        //subscriber.setSubscriberListener(this::onMessageTopic);
    }

    public String msgAlerta(Message msg){
        Object[] valor = msg.getServiceValue();
        String mensagemRecebida = StringUtils.join(valor, ", ");
        String ms = "";
        ms = mensagemRecebida;
        String[] sin = ms.split(";|;\\s");
        //String ss = sin[0];
        //double vv = Double.parseDouble(sin[1]);
        return sin[0];
    }

    private void onMessage(Message message) {
        ////Log.d("Topico Jean", message.getTopic());
        //Log.d("Uuid Jean", message.getUuid());
        handler.post(() -> {
            Object[] valor = message.getServiceValue();
            //listViewMessages.add(0, StringUtils.join(valor, ", "));
            listViewMessages.add(StringUtils.join(valor[0], ", "));
            listViewAdapter.notifyDataSetChanged();
        });
    }

    public void onMessageTopic(Message message) {

        handler.post(() -> {
            Object valor = ms;
            Log.d("TESTANDOSinaisVitais", ms);
            //Object valor = message.getTopic();
            listViewMessages.add(StringUtils.join(valor, ", "));
            listViewAdapter.notifyDataSetChanged();
            //subscriber.setSubscriberListener(this::onMessageTopic);
        });
    }

    public void mensagemTela(String mm) {
        handler.post(() -> {
            Object valor = mm;
            //Log.d("TESTANDOSinaisVitais", ms);
            //Object valor = message.getTopic();
            listViewMessages.add(StringUtils.join(valor, ", "));
            listViewAdapter.notifyDataSetChanged();
            //subscriber.setSubscriberListener(this::onMessageTopic);
        });
    }

    //@Subscribe(threadMode = ThreadMode.MAIN)
    //public void on(MessageEvent event) {
    //}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(MessageEvent event) {
        Object[] valor = event.getMessage().getServiceValue();
        Log.d("JEANNNNNN3333", (String) String.valueOf(valor));
        listViewMessages.add(StringUtils.join((String) valor[0], ", "));
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

    public void limpaListView() {
        listViewMessages.clear();
        listViewAdapter.notifyDataSetChanged();
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
        //readDataByColumn(caminho);

        //subscriber.subscribeServiceByName("my-service");
        //subscriber.setSubscriberListener(this::onMessage);
        limpaListView();
        configArquivo();
        lerMsg();
        //readData(caminho);
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
            } else {
                subscriber.setFilter(filterEditText.getText().toString());
                button.setText(R.string.clear_filter_button_label);
            }
            filtering = !filtering;
        });
    }

    public void subscribeServiceByName(String m) {
        subscriber.subscribeServiceByName(m);
    }

    public void setListener(ISubscriberListener listener) {
        subscriber.setSubscriberListener(listener);
    }

    // Método ler o arquivo coluna por coluna
    public void readDataByColumn(String caminho) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(caminho));
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line = "";
            try {
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",");
                    System.out.println("Sinais vitais: " + cols[0]);

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
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    // Método ler arquivo linha por linha
    public void lerMsg() {
        subscriber.subscribeServiceByName("my-service");
        //subscriber.setSubscriberListener(this::onMessage);
        Message msn = new Message();
        //msn.setServiceName("my-service");
        //msn.getServiceName();
        //msn.setServiceName("my-service");
        msn.getServiceValue();
        //subscriber.subscribeServiceByName("my-service");
        //subscriber.setSubscriberListener(this::onMessage);
        onMessage(msn);
    }

    // Método ler arquivo linha por linha
    public void readData( String caminho) {
        //try{
            //InputStream is = new FileInputStream(caminho);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line = "";
            try {
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    //Log.d("SinaisVitais", line);

                    // envia os dados para tela celular
                    ms = "";
                    ms = line;

                    //mensagemTela(ms);

                    Message msn = new Message();
                    //msn.setServiceName("my-service");
                    msn.setServiceValue(ms);
                    subscriber.subscribeServiceByName("my-service");
                    subscriber.setSubscriberListener(this::onMessage);
                    onMessage(msn);
                }
                is.close();
            } catch (IOException e) {
                Log.wtf("Sinais_vitais", "Erro ao ler arquivo" + line, e);
                e.printStackTrace();
            }
        //}
        //catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            //e1.printStackTrace();
        //}
    }

    // Cria uma janela com alerta, informando o nível de degradação do paciente
    public void geraAlerta(int nivelAlerta, String str) {
        if ( nivelAlerta != 0 ) {
            //LayoutInflater é utilizado para inflar nosso layout em uma view.
            //-pegamos nossa instancia da classe
            LayoutInflater li = getLayoutInflater();

            //inflamos o layout alerta.xml na view
            //View view = li.inflate(R.layout.alerta, null);
            View view = li.inflate(R.layout.alerta1, null);
            if (nivelAlerta == 1) {
                view = li.inflate(R.layout.alerta1, null);
            } else if (nivelAlerta == 2) {
                view = li.inflate(R.layout.alerta2, null);
            } else if (nivelAlerta == 3) {
                view = li.inflate(R.layout.alerta3, null);
            }

            //definimos para o botão do layout um clickListener
            view.findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    //exibe um Toast informativo.
                    Toast.makeText(MainActivity.this, "alerta", Toast.LENGTH_SHORT).show();
                    //desfaz o alerta.
                    //alerta.dismiss();
                    alerta.cancel();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("Alerta de paciente");
            View viewText = view;
            TextView textView = viewText.findViewById(R.id.sinal);
            textView.setText(str);
            builder.setView(view);
            alerta = builder.create();
            alerta.show();
        }
    }
}