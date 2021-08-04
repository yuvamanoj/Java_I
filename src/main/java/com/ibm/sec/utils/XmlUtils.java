package com.ibm.sec.utils;

import com.ibm.sec.dtos.JenkinsConfigDtos.Project;
import com.ibm.sec.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static com.ibm.sec.configurations.ExternalCallConstants.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;

public class XmlUtils {

    private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    public static String updateCustomerDetailsInXml(String customerId, String xmlContent, CustomerService.GithubRepositoryType githubRepositoryType){
        logger.debug("Entering update customer details in Jenkins config json ");
        String repoExtension;
        String configFileName;
        if (githubRepositoryType == INSTALLATION){
            repoExtension = customerInstallationGithubRepoExtension;
            configFileName = installationExecCommandFileName;
        }else if(githubRepositoryType == UNINSTALLATION){
            repoExtension = customerInstallationGithubRepoExtension;
            configFileName = unInstallationExecCommandFileName;
        }else if(githubRepositoryType == DASHBOARD_INSTALLATION){
            repoExtension = customerInstallationGithubRepoExtension;
            configFileName = pccDashboardInstallationFileName;
        } else {
            repoExtension = customerPolicyGithubRepoExtension;
            configFileName = policyExecCommandFileName;
        }
        String newUrl = gitConnectionInitial+customerId+repoExtension+gitExtension;
        String newRemoteDirectory = slash+customerId+repoExtension;
        String newExecCommand = execCommandInitial+customerId+repoExtension+slash+configFileName+execCommandFinal;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Project.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            StringReader reader = new StringReader(xmlContent);
            Project project = (Project) unmarshaller.unmarshal(reader);

            project.getScm().getUserRemoteConfigs().getUserRemoteConfig().setUrl(newUrl);
            project.getBuilders().getBapSshBuilderPlugin().getDelegate().getDelegate().getPublishers().getBapSshPublisher().getTransfers().getBapSshTransfer().setRemoteDirectory(newRemoteDirectory);
            project.getBuilders().getBapSshBuilderPlugin().getDelegate().getDelegate().getPublishers().getBapSshPublisher().getTransfers().getBapSshTransfer().setExecCommand(newExecCommand);
            
            if(githubRepositoryType == UNINSTALLATION || githubRepositoryType == DASHBOARD_INSTALLATION) {
                project.getBuilders().getBapSshBuilderPlugin().getDelegate().getDelegate().getPublishers().getBapSshPublisher().getTransfers().getBapSshTransfer().setSourceFiles("");
            }

            String execCommand = project.getBuilders().getBapSshBuilderPlugin().getDelegate().getDelegate().getPublishers().getBapSshPublisher().getTransfers().getBapSshTransfer().getExecCommand();
            String updatedExecCommand = updateExecCommand(execCommand,customerId, repoExtension);
            project.getBuilders().getBapSshBuilderPlugin().getDelegate().getDelegate().getPublishers().getBapSshPublisher().getTransfers().getBapSshTransfer().setExecCommand(updatedExecCommand);

            StringWriter sw = new StringWriter();
            JAXB.marshal(project, sw);
            logger.debug("Exiting update customer details in Jenkins config json ");
            return sw.toString();
        } catch (JAXBException e) {
            //e.printStackTrace();
            logger.error(e.getMessage());
            throw new ClassCastException("Received invalid XML String, marshall/unmarshall to object failed.");
        }
    }

    private static String updateExecCommand(String execCommand, String customerId, String repoExtension) {
        String[] commands = execCommand.split("-e ");
        String values = commands[1].replaceAll("&apos;", "").replaceAll("'", "");
        values += " custId=" + customerId + " projPath=" + customerId + repoExtension;
        values = "'"+values+"'";
        return commands[0]+ "-e "+ values;
    }
}
