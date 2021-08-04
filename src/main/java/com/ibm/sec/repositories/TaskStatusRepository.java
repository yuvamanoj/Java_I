package com.ibm.sec.repositories;

import com.ibm.sec.entities.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    public Optional<TaskStatus> findByUserIdAndTaskName(String userId, String taskName);

	public void deleteByUserId(String userId);

    List<TaskStatus> findByUserId(String customerId);
}
