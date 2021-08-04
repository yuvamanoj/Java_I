package com.ibm.sec.producer;


import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageKafkaProducer {
  private final KafkaTemplate<String, Object> template;
  
	private final Logger logger 
	= LoggerFactory.getLogger(MessageKafkaProducer.class);
  public Future send(String topic,Object message ) {
	  ListenableFuture<SendResult<String, Object>> future =this.template.send(new ProducerRecord<>(topic, message));
	  logger.info("Sending message {} into topic {}", message, topic);

    future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

        @Override
        public void onSuccess(SendResult<String, Object> result) {
            logger.debug(String.format("Message sent. with partition=%d offset=%d", result.getRecordMetadata().partition(), result.getRecordMetadata().offset()));
        }
        @Override
        public void onFailure(Throwable ex) {
        	logger.error("Failed to send message to queue in topic--> "+topic);
        }
    });

    return future;
}
}
