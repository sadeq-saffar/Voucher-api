import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.*;
import ngs.common.exception.ServerValidationExceptionPool;
import ngs.common.utility.NGSDate;
import ngs.ivc.IVCClientCore;
import ngs.ivc.businessrules.acc.InvoiceCorrectionVoucherRequest;
import ngs.ivc.businessrules.acc.InvoiceVoucherRequest;
import ngs.ivc.database.acc.VoucherResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class VoucherServer {
    private static final String RPC_QUEUE_NAME = "invoice_voucher_queue";

    Logger logger;
    public VoucherServer(Logger logger) throws IOException, TimeoutException {
        this.logger = logger;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            AMQP.Queue.DeclareOk queue = channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);


            channel.basicQos(1);

//            System.out.println(" [x] Awaiting RPC Invoice requests from " + RPC_QUEUE_NAME);
            logger.info(" [x] Awaiting RPC Voucher requests from " + RPC_QUEUE_NAME);

            Object monitor = new Object();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                long startTime = System.nanoTime();
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();
//                AsyncResponseModel response = new AsyncResponseModel();
                Gson gson = new Gson();

                logger.info(" Queue length is :  " + queue.getMessageCount());

                VoucherResponse response = new VoucherResponse("Not Issued Yet");
                try {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    logger.info(" [.] Voucher Input (" + message + ")");

                    VoucherRequest voucherRequest = gson.fromJson(message, VoucherRequest.class);
                    if (voucherRequest.getType().equals("invoice")) {
                        IssuInvoiceVoucher(logger, response, voucherRequest);
                    } else if (voucherRequest.getType().equals("invoiceCorrection")) {
                        IssueInvoiceCorrectionVoucher(logger, response, voucherRequest);
                    }

//                    response = new VoucherResponse("Issued Success !!");
                } catch (JsonSyntaxException e) {
                    logger.severe(" [.] Voucher Resp Has Error : " + e.getMessage());
                    response.setResponse("HasError");
                    response.setMessage(e.getLocalizedMessage());
                } finally {
                    logger.info(" [.] Voucher Resp " + gson.toJson(response));
                    long endTime = System.nanoTime();
                    logger.info(" [X] Issue Voucher time: Second " + ((endTime - startTime)/1000000000));
//                    System.out.println(" List time: Second " + ((endTime - startTime)/1000000000) +  " For Currency : " + currency);

                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, gson.toJson(response).getBytes(StandardCharsets.UTF_8));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        logger.severe(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }


    }

    private void IssueInvoiceCorrectionVoucher(Logger logger, VoucherResponse response, VoucherRequest voucherRequest) throws IOException {
        IVCClientCore clientCore = new IVCClientCore(voucherRequest.getHostName(), voucherRequest.getPort(),true, voucherRequest.getAppId());
        clientCore.connectToHttpServer();
        boolean loggedIn = false;
        try {
            loggedIn = clientCore.login(Main.loginName, Main.password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(!loggedIn){
            logger.warning("Can not login to IVC !!!!");
            response.setResponse("HasError");
            response.setMessage("Can not login to IVC !!!!");
            return;
//            throw new IOException("Can not login to IVC !!!!");
        }

        try {
            clientCore.setDatabase(clientCore.getDatabases().getActiveDatabase());
        } catch (RemoteException e) {
//            e.printStackTrace();
        }
        InvoiceCorrectionVoucherRequest invoiceCorrectionVoucherRequest = new InvoiceCorrectionVoucherRequest(clientCore);
        invoiceCorrectionVoucherRequest.setRunFromRabbit(true);
        invoiceCorrectionVoucherRequest.setInvoiceCorrectionIDs(voucherRequest.getIds());
        invoiceCorrectionVoucherRequest.setVoucherDate(NGSDate.getDate(voucherRequest.getVoucherDate()));
        invoiceCorrectionVoucherRequest.setAccStartDate(NGSDate.getDate(voucherRequest.getAccStartDate()));
        Object a = null;
        try {
            a = clientCore.performMiscRequest(invoiceCorrectionVoucherRequest);
            response.setResponse("Success");
            response.setMessage("");
            logger.info(voucherRequest.getType() + " Issued Success !!");

        } catch (RemoteException e) {
            System.out.println(e);
            response.setResponse("HasError");
            response.setMessage(e.getLocalizedMessage());
            response.setException(((ServerValidationExceptionPool) e).getExceptions());
//                            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
//        System.out.println(a);
    }

    private void IssuInvoiceVoucher(Logger logger, VoucherResponse response, VoucherRequest voucherRequest) {
        IVCClientCore clientCore = new IVCClientCore(voucherRequest.getHostName(), voucherRequest.getPort(),true, voucherRequest.getAppId());
        clientCore.connectToHttpServer();
        boolean loggedIn = false;
        try {
            loggedIn = clientCore.login(Main.loginName, Main.password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(!loggedIn){
            logger.warning("Can not login to IVC !!!!");
            response.setResponse("HasError");
            response.setMessage("Can not login to IVC !!!!");
            return;
//            throw new IOException("Can not login to IVC !!!!");
        }

        try {
            clientCore.setDatabase(clientCore.getDatabases().getActiveDatabase());
        } catch (RemoteException e) {
//            e.printStackTrace();
        }
        InvoiceVoucherRequest invoiceVoucherRequest = new InvoiceVoucherRequest(clientCore);
        invoiceVoucherRequest.setRunFromRabbit(true);
        invoiceVoucherRequest.setInvoiceIDs(voucherRequest.getIds());
        invoiceVoucherRequest.setVoucherDate(NGSDate.getDate(voucherRequest.getVoucherDate()));
        invoiceVoucherRequest.setAccStartDate(NGSDate.getDate(voucherRequest.getAccStartDate()));
        Object a = null;
        try {
            a = clientCore.performMiscRequest(invoiceVoucherRequest);
            response.setResponse("Success");
            response.setMessage("");
            logger.info(voucherRequest.getType() + " Issued Success !!");

        } catch (RemoteException e) {
            System.out.println(e);
            response.setResponse("HasError");
            response.setMessage(e.getLocalizedMessage());
            response.setException(((ServerValidationExceptionPool) e).getExceptions());
//                            e.printStackTrace();
            logger.severe(e.getLocalizedMessage());
        }
//        System.out.println(a);
    }
}
