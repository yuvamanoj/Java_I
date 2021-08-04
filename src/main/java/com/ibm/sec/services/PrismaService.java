package com.ibm.sec.services;

import org.springframework.stereotype.Service;

@Service
public interface PrismaService {

boolean refreshAPI(String prismaUrl, String prismaUsername, String prismaPassword);
}
