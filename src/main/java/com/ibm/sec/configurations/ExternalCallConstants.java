package com.ibm.sec.configurations;

public class ExternalCallConstants {

    //Github Urls

    //Github Template Repos
    public static String templatePolicyRepo = "Configuration";
    public static String templateInstallationRepo = "PCC-OC-Chart";

    //Jenkins Urls

    //miscellaneous
    public static String customerIdDelimiter = "_";
    public static String repositoryGeneratorHeader = "application/vnd.github.baptiste-preview+json";
    public static String githubApiHeader = "application/vnd.github.v3+json";
    public static String customerPolicyGithubRepoExtension = "_policy_prisma";
    public static String customerInstallationGithubRepoExtension = "_installation_prisma";
    public static String fileUpdateMessage = "Customer config file updated with customer id";
    public static String jsonContentField = "content";
    public static String jsonShaField = "sha";
    public static int waitTime = 3000;
    public static String installationExecCommandFileName = "ansible_install.yml";
    public static String unInstallationExecCommandFileName = "ansible_uninstall.yml";
    public static String policyExecCommandFileName = "ansible_addpolicy.yml";
    public static String pccDashboardInstallationFileName = "ansible_pccdashboard.yml";
    public static int maxAllowedRetryCount = 3;
    public static String gitConnectionInitial = "git@github.ibm.com:Cloud-Security/";
    public static String gitExtension = ".git";
    public static String slash = "/";
    public static String underscore = "_";
    public static String execCommandInitial = "ansible-playbook ./";
    public static String execCommandFinal = " -e 'ansible_python_interpreter=/usr/bin/python3'";
    public static String callTypeRecursive = "Recursive";

    // For Prisma
    public static String vulnerabilityRefreshURL = "/api/v1/stats/vulnerabilities/refresh"; 
    public static String complianceRefreshURL = "/api/v1/stats/compliance/refresh";

    //For Kafka
    public static String CSS_INSTALL_CONSUMER="css-installation";
    public static String NEW_USER_CONSUMER="new-user";
    public static String CSS_INSTALL_SUCCESS_CONSUMER="installation-success";
    public static String UPDATE_DB_CONSUMER="update-dbtaskstatus";
}
