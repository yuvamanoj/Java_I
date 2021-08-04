package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "delegate", namespace = "com.ibm.sec.dtos.JenkinsConfigDtos._Delegate")
public class _Delegate {
    @XmlElement(name = "consolePrefix")
    private String consolePrefix;

    @XmlElement(name = "delegate")
    private Delegate delegate;
}
