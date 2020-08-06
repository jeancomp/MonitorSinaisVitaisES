package br.lsdi.ufma.cddldemoapp;

import android.app.AlertDialog;
import java.io.IOException;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.message.SensorDataMessage;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.listeners.IMonitorListener;

public class MonitorPrincipal {
    PubSubActivity pubActivity;
    Publisher publisher;
    Subscriber subscriber;
    AlertDialog alerta;
    String monitorCode;
    //public ExampleMonitorListener monitorListener = new ExampleMonitorListener();

    private IMonitorListener monitorListener = new IMonitorListener() {
        @Override
        public void onEvent(Message message) {
            String line = String.valueOf(message.getServiceValue());
            System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>>ServiceValue: %s \n", line);
            String[] sinal = line.split(";|;\\s");
            String s = sinal[0];
            //double v = Double.parseDouble(sinal[1]);
            if (true) {
                new Thread() {
                    public void run() {
                            pubActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>Gerando alertas");
                                    pubActivity.geraAlerta(1);
                                }
                            });
                    }
                }.start();
            }
        }
    };
    /*
        public void metodo(Subscriber subsc, Publisher pub, PubSubActivity ps, Message msg) {
        String queryId = pub.getMonitor().addRule("select * from Message as msn where cast(msn.serviceValue[0], string) LIKE 'RESP%'", message -> {
            System.out.println("CODIGO A SER EXECUTADO QUANDO A REGRA DO MONITOR É SATISFEITA");
            System.out.println("A mensagem vem anexa: " + message);
            new Thread() {
                public void run() {
                    ps.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ps.geraAlerta(1);
                        }
                    });
                }
            }.start();
        });
        //monitorCode = sub.getMonitor().addRule("select * from SensorDataMessage where seviceValue='RESP'", monitorListener);
        //System.out.println("Monitor " + monitorCode);
    }
    */
    public void metodo(Subscriber sub, Publisher pub, PubSubActivity p, Message msg) {
        publisher = pub;
        subscriber = sub;
        pubActivity = p;

        String monitorCode = publisher.getMonitor().addRule("select * from Message", monitorListener);
        //monitorCode = sub.getMonitor().addRule("select * from SensorDataMessage where seviceValue='RESP'", monitorListener);
    }

    public void removeMonitor(String monitorCode){
        // remove the monitor added above
        subscriber.getMonitor().removeRule(monitorCode);
    }
}
