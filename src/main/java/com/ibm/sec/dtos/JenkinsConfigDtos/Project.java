package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Project {
    @XmlElement(name = "keepDependencies")
    private String keepDependencies;

    @XmlElement(name = "buildWrappers")
    private String buildWrappers;

    @XmlElement(name = "builders")
    private Builders builders;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "triggers")
    private String triggers;

    @XmlElement(name = "concurrentBuild")
    private String concurrentBuild;

    @XmlElement(name = "blockBuildWhenUpstreamBuilding")
    private Boolean blockBuildWhenUpstreamBuilding;

    @XmlElement(name = "blockBuildWhenDownstreamBuilding")
    private Boolean blockBuildWhenDownstreamBuilding;

    @XmlElement(name = "publishers")
    private String publishers;

    @XmlElement(name = "disabled")
    private Boolean disabled;

    @XmlElement(name = "scm")
    private Scm scm;

    @XmlElement(name = "actions")
    private String actions;

    @XmlElement(name = "canRoam")
    private Boolean canRoam;

    @XmlElement(name = "properties")
    private Properties properties;
}
