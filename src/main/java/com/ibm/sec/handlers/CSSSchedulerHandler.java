package com.ibm.sec.handlers;

import com.ibm.sec.services.JenkinsService.BuildInitializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import com.ibm.sec.entities.Customer;

import com.ibm.sec.repositories.CustomerRepository;
import com.ibm.sec.repositories.TaskStatusRepository;
import com.ibm.sec.services.CustomerService.GithubRepositoryType;
import com.ibm.sec.services.PrismaService;

@Component
public class CSSSchedulerHandler {
	private static final Logger logger = LoggerFactory.getLogger(CSSSchedulerHandler.class);
	@Autowired
	private PrismaService prismaService;
	@Autowired
	private JenkinsInteractionHandler jenkinsHandler;
	@Autowired
	private GithubInteractionHandler githubInteractionHandler;
	
	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	TaskStatusRepository taskStatusRepository;

	public void refreshAPICall(Customer customer) {
		if (!StringUtils.isEmpty(customer.getPrismaUrl()) && !StringUtils.isEmpty(customer.getPrismaUsername()) && !StringUtils.isEmpty(customer.getPrismaPassword())) {
			String prismaUrl = new String(Base64Utils.decodeFromString(customer.getPrismaUrl()));
			String prismaUsername = new String(Base64Utils.decodeFromString(customer.getPrismaUsername()));
			String prismaPassword = new String(Base64Utils.decodeFromString(customer.getPrismaPassword()));
			prismaService.refreshAPI(prismaUrl,prismaUsername,prismaPassword);	
		}
	}

	public void applyUnInstallationSteps(String custId) {
		logger.info("inside CSSSChedulerHandler.applyUnInstallationSteps() {}",custId);
		//trigger uninstallation job
		jenkinsHandler.initiateJenkinsJobBuild(custId,GithubRepositoryType.UNINSTALLATION, BuildInitializerType.INITIAL);
		//remove jenkins pcc dashboard installation job
		jenkinsHandler.deleteJenkinsJob(custId, GithubRepositoryType.DASHBOARD_INSTALLATION, BuildInitializerType.INITIAL);
		//remove jenkins policy job
		jenkinsHandler.deleteJenkinsJob(custId, GithubRepositoryType.POLICY, BuildInitializerType.INITIAL);
		//remove jenkins job for pcc-console installation
		jenkinsHandler.deleteJenkinsJob(custId, GithubRepositoryType.INSTALLATION,BuildInitializerType.INITIAL);		
		//remove github policy repo
		githubInteractionHandler.deleteGithubRepositoryForCustomer(custId, GithubRepositoryType.POLICY);
		
		githubInteractionHandler.deleteGithubRepositoryForCustomer(custId, GithubRepositoryType.INSTALLATION);
		jenkinsHandler.deleteJenkinsJob(custId, GithubRepositoryType.UNINSTALLATION,BuildInitializerType.INITIAL);
		//clean up database
		taskStatusRepository.deleteByUserId(custId);
		customerRepository.deleteById(custId);
		//trigger email	
	}

}
