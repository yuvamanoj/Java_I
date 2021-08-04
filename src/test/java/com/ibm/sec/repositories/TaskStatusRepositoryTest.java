package com.ibm.sec.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import com.ibm.sec.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ibm.sec.entities.Customer;
import com.ibm.sec.entities.TaskStatus;
import com.ibm.sec.entities.TermAndCondition;



@ExtendWith(SpringExtension.class)
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DataJpaTest
class TaskStatusRepositoryTest {
	@Autowired
	private TaskStatusRepository repository;
	@Autowired
	private CustomerRepository cusrepository;

	@Autowired
	private UserRepository userRepository;
	@BeforeEach
	void initUseCase() {
		User user = userRepository.save(new User("0000XXXX","test@Ibm.com"));
		Customer customer=new Customer();
		customer.setId("user1");
		customer.setFirstName("test"); customer.setLastName("test");
		customer.setEmail("test"); customer.setTncCheck(true); customer.setTncId(1L);
		TermAndCondition termAndCondition = new TermAndCondition();
		termAndCondition.setId(1L);
		customer.setCreatedDateTime(new Date());
		customer.setIbmId(user.getIbmId());
		cusrepository.save(customer);
		TaskStatus status1= new TaskStatus();
		status1.setId(1L);
		status1.setStatusName("test1");
		status1.setUserId("user1");
		status1.setTaskId(1L);
		status1.setInitiatedDateTime(new Date());
		status1.setCreatedDateTime(new Date());
		status1.setUpdatedDateTime(new Date());
		status1.setStatusId(1L);
		status1.setTaskName("test");
		TaskStatus status2= new TaskStatus("user1", 1L, "task1", new Date(), new Date(), new Date(), 1L, "test", 0, null);
		status2.setId(2L);
		repository.saveAll(Arrays.asList(status1,status2));
	}


	@AfterEach public void destroyAll(){
		repository.deleteAll();
		cusrepository.deleteAll();
		}

	@Test
	void deleteByUserId_success() {
;
		Optional<TaskStatus> optional1=repository.findByUserIdAndTaskName("user1", "task1");
		assertEquals("user1", optional1.get().getUserId());
		assertEquals(1L, optional1.get().getTaskId());
		assertEquals("task1", optional1.get().getTaskName());

		  assertEquals(null, optional1.get().getError());
		  assertEquals(0,optional1.get().getRetry());
		  assertEquals("test", optional1.get().getStatusName());
		  assertEquals(1L, optional1.get().getStatusId());
		//  assertEquals(Optional.empty(), optional);

		repository.deleteByUserId("user1");
		Optional<TaskStatus> optional = repository.findById(1L);
		assertEquals(Optional.empty(), optional);
	}


}
