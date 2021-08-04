package com.ibm.sec.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "customer_view")
    private String customerView;

    public enum Tasks{
        PERSIST_NEWUSERDETAILS,
        CREATE_REPOINSTALLATION,
        CREATE_REPOPOLICY,
        CREATE_JENKINSJOB_FOR_INSTALLATION,
        CREATE_JENKINSJOB_FOR_UNINSTALLATION,
        CREATE_JENKINSJOB_FOR_PRISMA_DASHBOARD_INSTALLATION,
        CREATE_JENKINSJOB_FOR_POLICY,
        BUILD_JENKINSJOB_FOR_INSTALLATION,
        BUILD_JENKINSJOB_FOR_POLICY,
        PRISMA_DASHBOARD_INSTALLATION,
        BUILD_JENKINSJOB_FOR_UNINSTALLATION,
        DELETE_JENKINSJOB_FOR_PRISMA_DASHBOARD,
        DELETE_JENKINSJOB_FOR_POLICY,
        DELETE_JENKINSJOB_FOR_INSTALLATION,
        DELETE_JENKINSJOB_FOR_UNINSTALLATION,
        DELETE_GITHUB_FOR_POLICY,
        DELETE_GITHUB_FOR_INSTALLATION,
        PURGE_CUSTOMER_DB_ENTRY
    }

    public enum StatusResponseTasks{
        PERSIST_NEWUSERDETAILS,
        CREATE_REPOINSTALLATION,
        CREATE_REPOPOLICY,
        CREATE_JENKINSJOB_FOR_INSTALLATION,
        CREATE_JENKINSJOB_FOR_UNINSTALLATION,
        CREATE_JENKINSJOB_FOR_PRISMA_DASHBOARD_INSTALLATION,
        CREATE_JENKINSJOB_FOR_POLICY,
        BUILD_JENKINSJOB_FOR_INSTALLATION,
        BUILD_JENKINSJOB_FOR_POLICY,
        PRISMA_DASHBOARD_INSTALLATION
    }
}
