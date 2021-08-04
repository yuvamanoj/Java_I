package com.ibm.sec.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ibm.sec.entities.Customer;
import com.ibm.sec.handlers.CSSSchedulerHandler;
import com.ibm.sec.repositories.CustomerRepository;

@Component
public class CSSScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CSSScheduler.class);
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CSSSchedulerHandler schedulerHandler;

    private final String schedule="0 0 */24 * * ?"; 
	//scheduling daily basis(after each 24 hour ) on server's time zone.
	//@Scheduled(cron = "10 * * * * ?", zone="IST")
    @Scheduled(cron = schedule)
	public void scheduleToRefreshPrisma() {
	    logger.info("Current time is :: " + Calendar.getInstance().getTime());
	    customerRepository.findAll().forEach(customer -> schedulerHandler.refreshAPICall(customer));
	}
	//@Scheduled(cron = "0 */1 * * * ?", zone="IST")
	@Scheduled(cron = schedule)
	public void scheduleToUninstall() {
	      LocalDate currentDateMinus30Days = LocalDate.now().minusDays(30);
	      logger.info("currentDateMinus30Days : " + currentDateMinus30Days);
	      List<Customer>cusList=  customerRepository.findAllWithcreatedDateTimeBefore(Date.from(currentDateMinus30Days.atStartOfDay(ZoneId.systemDefault()).toInstant()));
	      if(cusList.size()>0) {
	    	  cusList.forEach(customer->schedulerHandler.applyUnInstallationSteps(customer.getId()));
	      }
	}
}
