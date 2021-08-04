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

import com.ibm.sec.entities.Task;
import com.ibm.sec.entities.Task.Tasks;

@ExtendWith(SpringExtension.class)
@DataJpaTest
 class TaskRepositoryTest {
	@Autowired
	private TaskRepository repository;
	@BeforeEach
	   void initUseCase() {
	        Task task1= new Task();
	        task1.setId(1L);
	        task1.setName("test1");
	        Task task2= new Task();
	        task2.setId(2L);
	        task2.setName(Tasks.BUILD_JENKINSJOB_FOR_UNINSTALLATION.name());
	        List<Task> tasks = Arrays.asList(task1, task2);
	        repository.saveAll(tasks);
	    }
    @AfterEach
    public void destroyAll(){
    	repository.deleteAll();
    }
    @Test
    void findByName_success() {
        Task task = repository.findByName("test1");
        assertEquals(1L, task.getId());
        assertEquals("test1", task.getName());
    }

}
