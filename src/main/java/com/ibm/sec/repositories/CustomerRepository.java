package com.ibm.sec.repositories;

import com.ibm.sec.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
   public Optional<Customer> findByClusterId(String clusterId);
    @Query("select cus from Customer cus where cus.createdDateTime <= :createdDateTime")
    List<Customer> findAllWithcreatedDateTimeBefore(@Param("createdDateTime") Date createdDateTime);

    List<Customer> findByIbmId(String ibmId);
}
