package com.ibm.sec.configurations;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.dtos.InstallationDto;
import com.ibm.sec.dtos.TaskStatusDto;

@Configuration
public class KafkaConsumerConfig {
	@Bean
	public SeekToCurrentErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template, (data,ex)->new
				TopicPartition(data.topic()+"_dlt", data.partition()));
		var errorHandler=new SeekToCurrentErrorHandler(recoverer,new FixedBackOff(0L, 0));
		return errorHandler;
	}

	/*
	 * public ConsumerFactory<String, CustomerRegistrationDto> userConsumerFactory()
	 * { KafkaProperties properties=new KafkaProperties();
	 * properties.buildConsumerProperties().put(JsonDeserializer.TRUSTED_PACKAGES,
	 * "*"); return new DefaultKafkaConsumerFactory<>(
	 * properties.buildConsumerProperties(), new StringDeserializer(), new
	 * JsonDeserializer<>(CustomerRegistrationDto.class)); }
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomerRegistrationDto> 
	userKafkaListenerContainerFactory( KafkaOperations<Object, Object> template, KafkaProperties properties) {
		ConcurrentKafkaListenerContainerFactory<String, CustomerRegistrationDto> factory 
		= new ConcurrentKafkaListenerContainerFactory<>();
		/*
		 * KafkaProperties properties=new KafkaProperties();
		 * properties.buildConsumerProperties().put(JsonDeserializer.TRUSTED_PACKAGES,
		 * "*");
		 */

		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>( properties.buildConsumerProperties()
				,new StringDeserializer(), 
				new ErrorHandlingDeserializer<>(new JsonDeserializer<>(CustomerRegistrationDto.class))
				));
		return factory;
	}
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CustomerRegistrationDto> 
	userKafkaListenerDltContainerFactory( KafkaOperations<Object, Object> template,KafkaProperties properties) {
		ConcurrentKafkaListenerContainerFactory<String, CustomerRegistrationDto> factory 
		= userKafkaListenerContainerFactory(template,properties);
		factory.setErrorHandler(errorHandler(template));
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, TaskStatusDto> 
	updateDBKafkaListenerContainerFactory( KafkaOperations<Object, Object> template,KafkaProperties properties) {
		ConcurrentKafkaListenerContainerFactory<String,TaskStatusDto> factory 
		= new ConcurrentKafkaListenerContainerFactory<>();
		/*
		 * KafkaProperties properties=new KafkaProperties();
		 * properties.buildConsumerProperties().put(JsonDeserializer.TRUSTED_PACKAGES,
		 * "*");
		 */
		ConsumerFactory<String,TaskStatusDto> consumerFactory=new DefaultKafkaConsumerFactory<>( properties.buildConsumerProperties(), 
				new StringDeserializer(), 
				new ErrorHandlingDeserializer<>(new JsonDeserializer<>(TaskStatusDto.class)));
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, TaskStatusDto> 
	updateDBDltKafkaListenerContainerFactory( KafkaOperations<Object, Object> template,KafkaProperties properties) {
		ConcurrentKafkaListenerContainerFactory<String,TaskStatusDto> factory 
		=updateDBKafkaListenerContainerFactory(template,properties);
		factory.setErrorHandler(errorHandler(template));
		return factory;
	}

	@Bean public ConcurrentKafkaListenerContainerFactory<String,
	InstallationDto> cssKafkaListenerContainerFactory(
			KafkaOperations<Object, Object> template,KafkaProperties properties) {
		ConcurrentKafkaListenerContainerFactory<String, InstallationDto>
		factory = new ConcurrentKafkaListenerContainerFactory<>();
		ConsumerFactory<String,InstallationDto> consumerFactory=new DefaultKafkaConsumerFactory<>( properties.buildConsumerProperties(), 
				new StringDeserializer(), 
				new ErrorHandlingDeserializer<>(new JsonDeserializer<>(InstallationDto.class)));
		factory.setConsumerFactory(consumerFactory);
		return factory; 
		}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String,InstallationDto> cssDltKafkaListenerContainerFactory(
			KafkaOperations<Object, Object> template, KafkaProperties properties) {
		ConcurrentKafkaListenerContainerFactory<String,InstallationDto> factory
		=cssKafkaListenerContainerFactory(template,properties);
		factory.setErrorHandler(errorHandler(template));
		return factory;
		}

	/*
	 * @Bean public ReceiverOptions<String, CustomerRegistrationDto>
	 * kafkaReceiverOptions(String topic, KafkaProperties kafkaProperties) {
	 * ReceiverOptions<String, CustomerRegistrationDto> basicReceiverOptions =
	 * ReceiverOptions.create(kafkaProperties.buildConsumerProperties()); return
	 * basicReceiverOptions.subscription(Collections.singletonList(topic)); }
	 * 
	 * @Bean public ReactiveKafkaConsumerTemplate<String, CustomerRegistrationDto>
	 * reactiveKafkaConsumerTemplate(ReceiverOptions<String,
	 * CustomerRegistrationDto> kafkaReceiverOptions) { return new
	 * ReactiveKafkaConsumerTemplate<String,
	 * CustomerRegistrationDto>(kafkaReceiverOptions); }
	 */
}

