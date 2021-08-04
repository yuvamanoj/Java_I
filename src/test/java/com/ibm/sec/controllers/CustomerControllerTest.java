package com.ibm.sec.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ibm.sec.producer.MessageKafkaProducer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sec.dtos.CustomerClusterDetailsDto;
import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.dtos.JobDetailsDto;
import com.ibm.sec.dtos.PrismaDetailsDto;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.services.CustomerService;
@RunWith(SpringRunner.class)
@WebMvcTest(value = CustomerController.class)
class CustomerControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	CustomerService customerService;

	@MockBean
	MessageKafkaProducer producer;

	@MockBean
	private ErrorMessages errorMessages;
	private CustomerRegistrationDto customerRegDetailsDto=null;
	private PrismaDetailsDto prismaDetailsDto=null;
	private CustomerClusterDetailsDto customerClusterDetailsDto=null;
	private static ObjectMapper mapper = new ObjectMapper();

	@BeforeEach	
	void setup() {
		customerClusterDetailsDto =new CustomerClusterDetailsDto();
		customerClusterDetailsDto.setApiKey("fdddg");
		customerClusterDetailsDto.setClusterId("dfdgdg");
		customerClusterDetailsDto.setLicenseKey("dssf");
		customerClusterDetailsDto.setRegion("cffdf");
		customerClusterDetailsDto.setResourceGroup("dsfsf");
		customerClusterDetailsDto.setClusterId("1");

		customerRegDetailsDto=new CustomerRegistrationDto();
		customerRegDetailsDto.setApiKey("fdddg");
		customerRegDetailsDto.setClusterId("dfdgdg");
		customerRegDetailsDto.setLicenseKey("dssf");
		customerRegDetailsDto.setRegion("cffdf");
		customerRegDetailsDto.setResourceGroup("dsfsf");
		customerRegDetailsDto.setToolset("jhj");
		customerRegDetailsDto.setCompanyName("test");
		customerRegDetailsDto.setEmail("gmg@ff.com");
		customerRegDetailsDto.setFirstName("hghjghjg");
		customerRegDetailsDto.setFullVersion(false);
		customerRegDetailsDto.setLastName("nh");
		customerRegDetailsDto.setPhoneNumber(578575757585L);
		customerRegDetailsDto.setPlatform("ghgj");
		customerRegDetailsDto.setPolicy("gjgj");
		customerRegDetailsDto.setTncCheck(true);
		customerRegDetailsDto.setTncId(55L);
		customerRegDetailsDto.setToolset("khkjhkh");
		customerRegDetailsDto.setTrialVersion(true);
		customerRegDetailsDto.setPrismaPassword("test");
		customerRegDetailsDto.setPrismaUrl("test.com");
		customerRegDetailsDto.setPrismaUsername("test");  
		customerRegDetailsDto.setId("1");
		customerRegDetailsDto.setIbmId("0000XXXX");
		customerRegDetailsDto.setIbmEmailId("test@ibm.com");

		prismaDetailsDto =new PrismaDetailsDto();
		prismaDetailsDto.setPrismaPassword("test");
		prismaDetailsDto.setPrismaUrl("test.com");
		prismaDetailsDto.setPrismaUsername("test");
	}

	@Test
	void registerNewUserTest() throws Exception {
		String json = mapper.writeValueAsString(customerRegDetailsDto);
		mockMvc.perform(post("/customer/register").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content(json).accept(MediaType.APPLICATION_JSON)).andExpect(status().isAccepted());
	}

	@Test
	void updateUserPrismaDetailsTest() throws Exception {
		Mockito.when(customerService.updateCustomerPrismaDetails("1", prismaDetailsDto)).thenReturn(customerRegDetailsDto);
		String json = mapper.writeValueAsString(customerRegDetailsDto);
		mockMvc.perform(put("/customer/{customerId}/prismaDetails/update","1").contentType(MediaType.APPLICATION_JSON).characterEncoding("utf-8")
				.content(json).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		//.andExpect(jsonPath("$.prismaUsername", Matchers.equalTo("test")));

	}

	@Test
	void getPrismaDetailsForCustomerTest() throws Exception {
		Mockito.when(customerService.getCustomerPrismaDetails(ArgumentMatchers.any())).thenReturn(prismaDetailsDto);
		mockMvc.perform(get("/customer/{customerId}/prismaDetails","1")).andExpect(status().isOk())
		.andExpect(jsonPath("$.prismaUsername", Matchers.equalTo("test")));
	}
	@Test
	void getClusterDetailsByCustomerIdTest() throws Exception {
		Mockito.when(customerService.getClusterDetailsByCustomerId(ArgumentMatchers.any())).thenReturn(customerClusterDetailsDto);
		mockMvc.perform(get("/customer/{customerId}/clusterDetails","1")).andExpect(status().isOk())
		.andExpect(jsonPath("$.region", Matchers.equalTo("cffdf")));
	}
	@Test
	void updateJobStatusTest() throws Exception {
		JobDetailsDto jobDetailsDto=new JobDetailsDto();
		jobDetailsDto.setJobName("INSTALLATION");
		jobDetailsDto.setStatus(true);
		String json = mapper.writeValueAsString(jobDetailsDto);
		Mockito.when(customerService.updateJobStatusForCustomer("1", jobDetailsDto)).thenReturn(json);
		mockMvc.perform(put("/customer/{customerId}/job","1").contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8").content(json).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());

	}
}
