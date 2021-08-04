package com.ibm.sec.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.sec.entities.TermAndCondition;
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
class TermAndConditionEntityTest {
	@Autowired 
	private TermsAndConditionRepository repository;
	private TermAndCondition termAndCondition;


	@Test
	void should_not_allow_null_name() {
		termAndCondition = new TermAndCondition();
		termAndCondition.setId(1L);
		termAndCondition.setTerms("test");
		termAndCondition.setVersion(1);
		this.repository.save(termAndCondition);
		Optional<TermAndCondition> condition=this.repository.findById(1L);
		assertEquals(false,condition.isEmpty() );
		assertEquals(1L,condition.get().getId());
		assertEquals(1,condition.get().getVersion() );
		assertEquals("test",condition.get().getTerms() );
	}
}
