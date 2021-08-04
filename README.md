# cerberus-tab-generator-customer-config

[![Quality gate](https://sonar.sec.ibm.com/api/project_badges/quality_gate?project=managed-security%3Acerberus-tab-generator-customer-config)](https://sonar.sec.ibm.com/dashboard?id=managed-security%3Acerberus-tab-generator-customer-config)

## How to work with this project scaffold
In this readme you will find instructions specific to working with this Java Microservice scaffold based on Spring Boot. 

### File Structure
```
├── .gitignore - Common git ignore patterns.
├── chart/ - The Helm chart used to deploy the application to Kubernetes or OpenShift.
│   ├── values.yaml - The main K8s configuration file
├── Dockerfile - Used by Docker to build the app container image.
├── Jenkinsfile - Used by Jenkins to execute the build pipeline.
├── pom.xml - Maven Project definition file.
└── src/
    ├── main/
    │   ├── java/com/ibm/sec
    │   │   └── Application.java - Simple Spring Boot application.  
    │   ├── /resources/
    │       ├── application.yaml     - Used by Spring Boot to configure default application
    │       │                          configuration.  This is initialized with core
    │       │                          application capabilities such as metrics and health
    │       │                          via Spring Actuator.
    │       └── logback.xml      - Default logback logging configuration.
    └── test/java/com/ibm/sec
        ├── java/com/ibm/sec
            └── Application/test.java - Simple test class

```

### Instructions

Follow the [DevSecOps Pipeline Service Gen 5](https://pages.github.ibm.com/managed-security/dept-it/#/services/devsecops_sre/devsecops_pipeline/5/) documentation to learn how to work with this project scaffold and deliver an application into OpenShift.