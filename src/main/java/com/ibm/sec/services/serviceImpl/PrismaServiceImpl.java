package com.ibm.sec.services.serviceImpl;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.ibm.sec.configurations.ExternalCallConstants;
import com.ibm.sec.exceptions.ApiError;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.NoResponseException;
import com.ibm.sec.services.PrismaService;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Component
public class PrismaServiceImpl implements PrismaService {
	private static final Logger logger = LoggerFactory.getLogger(PrismaServiceImpl.class);

	@Autowired
    private ErrorMessages errorMessages;

	@Override
	public boolean refreshAPI(String prismaUrl, String prismaUsername, String prismaPassword) {
		logger.info(" Executing refreshAPI inside PrismaServiceImpl");
		refreshVulnerability(prismaUrl,prismaUsername,prismaPassword);
		refreshCompliance(prismaUrl,prismaUsername, prismaPassword);
		return true;
	}

	private void refreshVulnerability(String prismaUrl, String prismaUsername, String prismaPassword) {
		logger.info(" Executing refresh Vulnerability api");
		try {
			getClient(prismaUrl,prismaUsername,prismaPassword)
			.post()
			.uri(ExternalCallConstants.vulnerabilityRefreshURL)
			.retrieve()
			.bodyToFlux(Void.class)
			.blockFirst();
		}catch(Exception e) {
			logger.error(e.getMessage());
			 throw new NoResponseException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getNETWORK_ISSUE(), new RuntimeException()));
		}
	}
	private void refreshCompliance(String prismaUrl, String prismaUsername, String prismaPassword) {
		logger.info(" Executing refresh compliance api");
		try {
			getClient(prismaUrl,prismaUsername,prismaPassword)
			.post()
			.uri(ExternalCallConstants.complianceRefreshURL)
			.retrieve()
			.bodyToFlux(Void.class)
			.blockFirst();
		}catch(Exception e) {
			logger.error(e.getMessage());
			 throw new NoResponseException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getNETWORK_ISSUE(), new RuntimeException()));
		}
	}
	private WebClient getClient(String prismaUrl, String prismaUsername, String prismaPassword) {
		logger.info(" Getting webclient for api call");
		WebClient client;
		TcpClient tcpClient = TcpClient.newConnection();
		try {
			SslContext context =
					SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.
							INSTANCE).build();

			HttpClient httpClient = HttpClient.from(tcpClient)
					.secure(t ->
					t.sslContext(context));
			client = WebClient.builder()
					.clientConnector(new ReactorClientHttpConnector(httpClient))
					.defaultHeaders(header -> header.setBasicAuth(prismaUsername, prismaPassword))
					.baseUrl(prismaUrl).build();
		} catch (SSLException e) {
			logger.error(e.getMessage());
			 throw new NoResponseException(new ApiError(HttpStatus.BAD_REQUEST, ApiError.ErrorMessages.NO_WEB_CLIENT_FOUND.getMessage(), new RuntimeException()));
		}
		return client;
	}
}
