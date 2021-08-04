# SOURCE: https://github.ibm.com/managed-security/msscertimg-openjdk-11/blob/master/Dockerfile
FROM mss-docker.artifactory.swg-devops.com/msscertimg-openjdk-11:latest

ADD target/*.jar /opt/app
