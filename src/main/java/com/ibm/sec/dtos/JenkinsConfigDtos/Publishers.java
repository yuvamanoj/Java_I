package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Publishers {
    @XmlElement(name = "jenkins.plugins.publish__over__ssh.BapSshPublisher")
    private BapSshPublisher bapSshPublisher;
}
