package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Scm {
    @XmlElement(name = "extensions")
    private String extensions;

    @XmlAttribute(name = "class")
    private String aClass;

    @XmlAttribute(name = "plugin")
    private String plugin;

    @XmlElement(name = "configVersion")
    private String configVersion;

    @XmlElement(name = "submoduleCfg")
    private SubmoduleCfg submoduleCfg;

    @XmlElement(name = "userRemoteConfigs")
    private UserRemoteConfigs userRemoteConfigs;

    @XmlElement(name = "doGenerateSubmoduleConfigurations")
    private String doGenerateSubmoduleConfigurations;

    @XmlElement(name = "branches")
    private Branches branches;

    @XmlAttribute(name = "class")
    private String _class;
}