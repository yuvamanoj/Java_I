package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class BapSshPublisher {

    @XmlElement(name = "configName")
    private String configName;

    @XmlElement(name = "useWorkspaceInPromotion")
    private String useWorkspaceInPromotion;

    @XmlAttribute(name = "plugin")
    private String plugin;

    @XmlElement(name = "transfers")
    private Transfers transfers;

    @XmlElement(name = "usePromotionTimestamp")
    private String usePromotionTimestamp;

    @XmlElement(name = "verbose")
    private String verbose;
}
