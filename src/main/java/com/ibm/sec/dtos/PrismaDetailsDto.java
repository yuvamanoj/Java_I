package com.ibm.sec.dtos;

import com.ibm.sec.entities.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Base64Utils;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@NoArgsConstructor
//@PrismaDetails
public class PrismaDetailsDto {

    @NotNull(message = "Prisma url is mandatory")
    private String prismaUrl;

    @NotNull(message = "Prisma username is mandatory")
    private String prismaUsername;

    @NotNull(message = "Prisma password is mandatory")
    private String prismaPassword;

    public PrismaDetailsDto(Customer customer){
        this.prismaUrl = new String(Base64Utils.decodeFromString(customer.getPrismaUrl()));
        this.prismaUsername = new String(Base64Utils.decodeFromString(customer.getPrismaUsername()));
        this.prismaPassword = new String(Base64Utils.decodeFromString(customer.getPrismaPassword()));
    }
}
