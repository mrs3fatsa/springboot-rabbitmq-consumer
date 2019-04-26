package com.javainuse.service;

import com.prowidesoftware.swift.model.SwiftMessage;
import com.prowidesoftware.swift.model.mt.mt1xx.MT101;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.adapter.ReplyFailureException;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.prowidesoftware.swift.model.Tag;
import com.prowidesoftware.swift.model.field.Field;
import org.springframework.amqp.core.Message;
import com.prowidesoftware.swift.io.ConversionService;

@Component
public class RabbitMQConsumer {

    @Value("${determine.destination}")
    private String determineDestinationURL;

    @Value("${retrieve.failed.payment.message.status}")
    private String retrieveFailedPaymentMessageStatusURL;

    @Value("${retrieve.successful.payment.message.status}")
    private String retrieveSuccessfulPaymentMessageStatusURL;

    @Value("${send.validated.payment.message}")
    private String sendValidatedPaymentMessageURL;

	@RabbitListener(queues = "${absa.validated.payment.queue}")
	public void receivedValidatedPaymentMessage(Message message) throws IOException {
        ConversionService srv = new ConversionService();

	    try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("absa-spring-boot-request.txt");
            String encodedMT101Request = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Locale locale = Locale.getDefault();
            SwiftMessage sm = SwiftMessage.parse(encodedMT101Request);

            System.out.println("Sender: " + sm.getSender());
            System.out.println("Receiver: " + sm.getReceiver() + "\n");

            for (Tag tag : sm.getBlock4().getTags()) {
                Field field = tag.asField();
                System.out.println(Field.getLabel(field.getName(), "101", null, locale));
                System.out.println(field.getValueDisplay(locale) + "\n");
            }

            String xml = srv.getXml(encodedMT101Request);
            System.out.println(xml);

            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> params = new HashMap<>();
            params.put("paymentXml", xml);
            String result = restTemplate.postForObject(retrieveSuccessfulPaymentMessageStatusURL, params, String.class);

            System.out.println(result);
        } catch(ListenerExecutionFailedException e){
            System.out.println(e.getMessage());
        } catch(ReplyFailureException e){
            System.out.println(e.getMessage());
        } catch(AmqpException e) {
            System.out.println(e.getMessage());
        }
	}
}