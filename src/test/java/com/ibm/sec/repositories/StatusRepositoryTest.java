package com.ibm.sec.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ibm.sec.entities.Status;
import com.ibm.sec.entities.Status.Statuses;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class StatusRepositoryTest {

	@Autowired
	private StatusRepository repository;
	@BeforeEach
	void initUseCase() {
		Status status1= new Status();
		status1.setId(1L);
		status1.setName(Statuses.SUCCESS.name());
		Status status2= new Status();
		status2.setId(2L);
		status2.setName("test2");
		List<Status> statusList = Arrays.asList(status1, status2);
		repository.saveAll(statusList);
	}
	@AfterEach
	public void destroyAll(){
		repository.deleteAll();
	}
	@Test
	void findByName_success() {
		Status status = repository.findByName("test2");
		assertEquals(2L, status.getId());
		assertEquals("test2", status.getName());
	}
}
