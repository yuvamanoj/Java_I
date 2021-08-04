package com.ibm.sec.repositories;

import com.ibm.sec.entities.TermAndCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAndConditionRepository extends JpaRepository<TermAndCondition, Long> {
}
