package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delegate", namespace = "com.ibm.sec.dtos.JenkinsConfigDtos.Delegate")
public class Delegate {
    @XmlAttribute(name = "plugin")
    private String plugin;

    @XmlElement(name = "hostConfigurationAccess")
    private HostConfigurationAccess hostConfigurationAccess;

    @XmlElement(name = "publishers")
    private Publishers publishers;

    @XmlElement(name = "alwaysPublishFromMaster")
    private String alwaysPublishFromMaster;

    @XmlElement(name = "failOnError")
    private String failOnError;

    @XmlElement(name = "continueOnError")
    private String continueOnError;
}