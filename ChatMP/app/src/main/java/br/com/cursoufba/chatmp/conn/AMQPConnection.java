package br.com.cursoufba.chatmp.conn;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Angelo on 11/10/2015.
 */
public class AMQPConnection {

    public static String FANOUT_EXCHANGE = "UFBA_FANOUT";
    public static String DIRECT_EXCHANGE = "UFBA_DIRECT";
    public String MY_QUEUE_NAME = "";

    public String routingKey = "";

    public BlockingDeque<String> queue = new LinkedBlockingDeque<String>();

    ConnectionFactory factory = new ConnectionFactory();

    Thread subscribeThread;
    Thread publishThread;

    public AMQPConnection() {
        setupConnectionFactory();
        publishToAMQP();
    }

    public void onDestroy() {
        publishThread.interrupt();
        subscribeThread.interrupt();
    }

    private void setupConnectionFactory() {
        String uri = "amqp://mtm-amqp.cloudapp.net:5672";
        try {
            factory.setAutomaticRecoveryEnabled(true);
            factory.setUsername("ufba");
            factory.setPassword("ufba");
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IllegalArgumentException e1) {
            e1.printStackTrace();
        }
    }

    public void publishToAMQP() {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();

                        while (true) {
                            String message = queue.takeFirst();
                            try{
                                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                                        .type("Angelo")
                                        .build();
                                if (!routingKey.equals(""))
                                    ch.basicPublish(DIRECT_EXCHANGE, routingKey, null, message.getBytes());
                                else
                                    ch.basicPublish(FANOUT_EXCHANGE, "", null, message.getBytes());

                                Log.d("", "[s] " + message);

                                ch.waitForConfirmsOrDie();
                            } catch (Exception e){
                                Log.d("","[f] " + message);
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        Log.d("", "Falha na conexão: " + e.getMessage());
                        try {
                            Thread.sleep(5000); // Aguarda e tenta novamente
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }

    public void subscribe(final Handler handler) {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();

                        channel.exchangeDeclare(DIRECT_EXCHANGE, "direct", true);
                        channel.exchangeDeclare(FANOUT_EXCHANGE, "fanout", true);

                        AMQP.Queue.DeclareOk q = channel.queueDeclare(MY_QUEUE_NAME, true, false, false, null);
                        channel.queueBind(q.getQueue(), DIRECT_EXCHANGE, MY_QUEUE_NAME);
                        channel.queueBind(q.getQueue(), FANOUT_EXCHANGE, MY_QUEUE_NAME);

                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);

                        // Processa as entregas feitas pelo servidor
                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                            String message = new String(delivery.getBody());
                            Log.d("","[r] " + message);

                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();

                            bundle.putString("msg", message);

                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        Log.d("", "Falha na conexão: " + e.getMessage());
                        break;
                    } catch (Exception e1) {
                        Log.d("", "Falha na conexão: " + e1.getMessage());
                        try {
                            Thread.sleep(4000); // Aguarda e tenta novamente
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }

}