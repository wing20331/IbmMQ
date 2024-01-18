package org.example;

import com.ibm.mq.jms.*;

import javax.jms.*;

public class Main {
    public static void main(String[] args) {
        try {
            MQQueueConnection mqConnection;
            MQQueueConnectionFactory mqCF;
            final MQQueueSession mqSession;
            MQQueue mqIn;
            MQQueueReceiver mqReceiver;

            mqCF = new MQQueueConnectionFactory();
            mqCF.setHostName("localhost");
            mqCF.setPort(1414);
            mqCF.setQueueManager("ADMIN");
            mqCF.setChannel("SYSTEM.DEF.SVRCONN");

            mqConnection = (MQQueueConnection) mqCF.createQueueConnection();
            mqSession = (MQQueueSession) mqConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            mqIn = (MQQueue) mqSession.createQueue("MQIN");
            mqReceiver = (MQQueueReceiver) mqSession.createReceiver(mqIn);

            MQQueueConnection mqConnectionOut;
            MQQueueConnectionFactory mqCFOut;
            MQQueueSession mqSessionOut;
            MQQueue mqOut;
            MQQueueSender mqSender;

            mqCFOut = new MQQueueConnectionFactory();
            mqCFOut.setHostName("localhost");
            mqCFOut.setPort(1414);
            mqCFOut.setQueueManager("ADMIN");
            mqCFOut.setChannel("SYSTEM.DEF.SVRCONN");

            mqConnectionOut = (MQQueueConnection) mqCFOut.createQueueConnection();
            mqSessionOut = (MQQueueSession) mqConnectionOut.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            mqOut = (MQQueue) mqSessionOut.createQueue("MQOUT");
            mqSender = (MQQueueSender) mqSessionOut.createSender(mqOut);

            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    System.out.println("Got Message");
                    if (message instanceof TextMessage) {
                        try {
                            TextMessage tmsg = (TextMessage) message;
                            String msgText = tmsg.getText();
                            System.out.println(msgText);

                            TextMessage outMessage = mqSessionOut.createTextMessage();
                            outMessage.setText(msgText);
                            mqSender.send(outMessage);
                            System.out.println("Передано во вторую очередь");

                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            mqReceiver.setMessageListener(listener);
            mqConnection.start();
            System.out.println("Stub started");

        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
