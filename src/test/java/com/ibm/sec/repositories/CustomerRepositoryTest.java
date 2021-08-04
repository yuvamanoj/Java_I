package com.ibm.sec.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.ibm.sec.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.entities.Customer;
import com.ibm.sec.entities.TaskStatus;
import com.ibm.sec.entities.TermAndCondition;



@ExtendWith(SpringExtension.class)
@DataJpaTest
class CustomerRepositoryTest {
	@Autowired
	private CustomerRepository repository;

	@Autowired
	UserRepository userRepository;

	@BeforeEach
	void initUseCase() {

		User user = userRepository.save(new User("0000XXXX","test@Ibm.com"));

		Customer customer=new Customer();
		customer.setId("1");
		customer.setFirstName("test");
		customer.setLastName("test");
		customer.setEmail("test");
		customer.setTncCheck(true);
		customer.setCreatedDateTime(new Date());
		customer.setTncId(1L);
		customer.setIbmId(user.getIbmId());

		/*
		 * TermAndCondition termAndCondition = new TermAndCondition();
		 * termAndCondition.setId(1L);
		 */

		CustomerRegistrationDto customerRegistrationDto =new CustomerRegistrationDto();
		customerRegistrationDto.setApiKey("test");
		customerRegistrationDto.setClusterId("test");
		customerRegistrationDto.setCompanyName("test");
		customerRegistrationDto.setCreatedBy("test");
		customerRegistrationDto.setCreatedDateTime(new Date());
		customerRegistrationDto.setEmail("test");
		customerRegistrationDto.setFirstName("test");
		customerRegistrationDto.setFullVersion(false);
		customerRegistrationDto.setId("2");
		customerRegistrationDto.setLastName("test");
		customerRegistrationDto.setLicenseKey("test");
		customerRegistrationDto.setPhoneNumber(999L);
		customerRegistrationDto.setPlatform("test");
		customerRegistrationDto.setPolicy("test");
		customerRegistrationDto.setRegion("test");
		customerRegistrationDto.setResourceGroup("test");
		customerRegistrationDto.setTncCheck(false);
		customerRegistrationDto.setTncId(1L);
		customerRegistrationDto.setToolset("test");
		customerRegistrationDto.setTrialVersion(false);
		customerRegistrationDto.setUpdatedBy("test");
		customerRegistrationDto.setUpdatedDateTime(new Date());
		customerRegistrationDto.setIbmId(user.getIbmId());
		customerRegistrationDto.setIbmEmailId("test@Ibm.com");
		Customer cus1=new Customer(customerRegistrationDto);
		List<Customer> customerList = Arrays.asList(customer,cus1);

		repository.saveAll(customerList);
	}
	@AfterEach
	public void destroyAll(){
		repository.deleteAll();
		userRepository.deleteAll();
	}
	@Test
	void findAllWithcreatedDateTimeBefore_success() {
		repository.findAllWithcreatedDateTimeBefore(new Date());
		Optional<Customer> optional = repository.findById("2");
		assertEquals("test", optional.get().getFirstName());
		assertEquals("test", optional.get().getLastName());
		assertEquals("test", optional.get().getLicenseKey());
		assertEquals("test", optional.get().getCompanyName());
		assertEquals("test", optional.get().getClusterId());
		assertEquals("test", optional.get().getEmail());
		assertEquals("test", optional.get().getPlatform());
		assertEquals("test", optional.get().getPolicy());
		assertEquals("test", optional.get().getRegion());
		assertEquals("test", optional.get().getResourceGroup());
		assertEquals("test", optional.get().getApiKey());
		assertEquals("test", optional.get().getToolset());
		assertEquals(1L, optional.get().getTncId());
		assertEquals(null, optional.get().getPrismaPassword());
		assertEquals(null, optional.get().getPrismaUsername());
		assertEquals(null, optional.get().getPrismaUrl());
		assertEquals(false, optional.get().isFullVersion());
		assertEquals(false, optional.get().isTncCheck());
		assertEquals(false, optional.get().isTrialVersion());
		assertEquals(999L, optional.get().getPhoneNumber());
		assertEquals(-1, optional.get().getCreatedDateTime().compareTo(new Date()));
		assertEquals(-1, optional.get().getUpdatedDateTime().compareTo(new Date()));
		assertEquals("test", optional.get().getCreatedBy());
		assertEquals("test", optional.get().getUpdatedBy());
	}


}