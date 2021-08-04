package com.ibm.sec.listener;

import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.services.CustomerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class NewConsumerListner implements CSSEventListener
{
	private final Logger logger 
	= LoggerFactory.getLogger(NewConsumerListner.class);

	@Autowired
	CustomerService customerService;
	
	@KafkaListener(topics = "new-user", groupId = "css-new", containerFactory = "userKafkaListenerDltContainerFactory")
	public void consume(ConsumerRecord<String, CustomerRegistrationDto> incomingMessage) {
		logger.info("NewConsumerListner Message recieved ->"+ incomingMessage.value());
		onData(incomingMessage.value());
	}
	@Override
	public void onData(Object event) {
		customerService.registerUser((CustomerRegistrationDto)event);
		logger.info("success in consumer");
	}
	
	@KafkaListener(topics = "new-user_dlt", groupId = "css-new_dlt",containerFactory = "userKafkaListenerContainerFactory")
	public void consumeDlt(ConsumerRecord<String, CustomerRegistrationDto> incomingMessage) {
	//	System.out.println("NewConsumerListner inside listener"+incomingMessage.topic()+incomingMessage.key()+incomingMessage.serializedValueSize()+incomingMessage.value()+incomingMessage.value().getFirstName());
		logger.info("NewConsumerListner Message recieved ->"+ incomingMessage.value());
	//	logger.info("NewConsumerListner value recieved ->"+ incomingMessage.value().getClusterId());
		dltOperation(incomingMessage.value());
	}
	
	@Override
	public void dltOperation(Object in) {
	logger.info("Started NewConsumerListner DLT operation");
		
	}


}
