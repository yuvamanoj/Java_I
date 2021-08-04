package com.ibm.sec.dtos.JenkinsConfigDtos;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class BapSshTransfer {

    @XmlElement(name = "execCommand")
    private String execCommand;

    @XmlElement(name = "useSftpForExec")
    private String useSftpForExec;

    @XmlElement(name = "remoteDirectorySDF")
    private String remoteDirectorySDF;

    @XmlElement(name = "excludes")
    private String excludes;

    @XmlElement(name = "usePty")
    private String usePty;

    @XmlElement(name = "flatten")
    private String flatten;

    @XmlElement(name = "useAgentForwarding")
    private String useAgentForwarding;

    @XmlElement(name = "sourceFiles")
    private String sourceFiles;

    @XmlElement(name = "execTimeout")
    private String execTimeout;

    @XmlElement(name = "remoteDirectory")
    private String remoteDirectory;

    @XmlElement(name = "cleanRemote")
    private String cleanRemote;

    @XmlElement(name = "makeEmptyDirs")
    private String makeEmptyDirs;

    @XmlElement(name = "removePrefix")
    private String removePrefix;

    @XmlElement(name = "noDefaultExcludes")
    private String noDefaultExcludes;

    @XmlElement(name = "patternSeparator")
    private String patternSeparator;
}
