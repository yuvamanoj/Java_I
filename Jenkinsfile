// Generated via https://github.ibm.com/managed-security/generator-devsecops/blob/master/generators/categories/scaffold/core/java-microservice/index.js

def image
def imageTag
def version
def fullVersion
def chartVersion
pipeline {
  agent any

  options {
    buildDiscarder(logRotator(daysToKeepStr: '7', artifactDaysToKeepStr: '7'))
  }




  environment {

    REPOSITORY_NAME = "${env.GIT_URL.tokenize('/')[3].split('\\.')[0]}"
    REPOSITORY_OWNER = "${env.GIT_URL.tokenize('/')[2]}"
    GIT_SHORT_REVISION = "${env.GIT_COMMIT[0..7]}"
    DOCKER_SERVER = "${env.MSS_DOCKER_SERVER_PRD}"
    DOCKER_CREDENTIALS = 'mss-docker-registry-prd-credentials'
    NODEJS_HOME = tool('nodejs-12.16.3')
    HELM_HOME = tool name: 'helm3', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
    COMMON_PATH = "$HELM_HOME/linux-amd64:$NODEJS_HOME/bin"


    SNAPSHOT_REPO = "${env.MSS_MAVEN_SNAPSHOTS_REPO}"
    RELEASE_REPO = "${env.MSS_MAVEN_RELEASES_REPO}"
    JAVA_HOME = tool('openjdk-11')
    PATH = "$JAVA_HOME/bin:$COMMON_PATH:$PATH"  
  }

  stages {

    stage('Checkout') {
      steps {
        deleteDir()
        checkout scm
      }
    }



    stage('Generator Version Update Check') {
      steps {
        script {
          warnError('Generator Last Run Version is Outdated') {
            withCredentials([usernamePassword(credentialsId: 'mss-artifactory-credentials', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD')]) {
              sh '''#!/bin/bash
                mkdir -p yo_update
                cd yo_update
                npm init --force > /dev/null
                npm config set @mss:registry https://na.artifactory.swg-devops.com/artifactory/api/npm/mss-npm/  --userconfig ./.npmrc
                npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/mss-npm/:_password="$(printf ${ARTIFACTORY_PASSWORD} | base64)"  --userconfig ./.npmrc
                npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/mss-npm/:username="${ARTIFACTORY_USERNAME}"  --userconfig ./.npmrc
                npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/mss-npm/:email="${ARTIFACTORY_USERNAME}"  --userconfig ./.npmrc
                npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/mss-npm/:always-auth=true  --userconfig ./.npmrc
                npm install yo @mss/generator-devsecops > /dev/null
                # Workaround: https://github.com/yeoman/yo/issues/348#issuecomment-477856306
                sed -i -e '/rootCheck/d' node_modules/yo/lib/cli.js
                node_modules/yo/lib/cli.js @mss/devsecops --ci
                RV=$?
                cd ..
                rm -rf yo_update
                exit $RV
              '''
            }
          }
        }
      }
    }


    stage('Set Full Version') {
      steps {
        script {
          withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
            version = sh(script: 'mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout | tail -n1 | sed "s/[^0-9.]*\\([0-9.]*\\).*/\\1/"', returnStdout: true).trim()

            if (env.TAG_NAME) {
              if (!env.TAG_NAME.contains(version)) {
                error "Git tag '${env.TAG_NAME}' does not match build version '${version}'"
              }

              // Format: <major>.<minor>.<patch>
              fullVersion = version
              chartVersion = version
            } else {
              // Format: <major>.<minor>.<patch>-<branch>.<commit hash>
              def branch_short = env.GIT_BRANCH.replaceAll("[^a-zA-Z0-9 ]+","").toLowerCase().take(64)
              fullVersion = "${version}-${branch_short}.${env.GIT_COMMIT[0..7]}"
              chartVersion = "${version}-${branch_short}";
            }


            sh "mvn versions:set -DnewVersion=${fullVersion}"
            echo "Updated pom.xml with new version:"
            sh 'cat pom.xml'
          }
        }
      }
    }

    stage('Compile') {
      steps {
        withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
          sh "mvn clean compile"
        }
      }
    }

    stage('Test') {
      steps {
        script {
          try {
            withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
              sh "mvn test"
            } 
          } catch (exc) {
            echo 'Tests failed'
            currentBuild.result = 'FAILURE'
          }
        }
      }
    }

    stage('Build Container Image') {
      steps {
        script {
          withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
            sh '''#!/bin/bash
            mvn package dockerfile:build -Ddocker.registry=$MSS_DOCKER_SERVER_PRD
            mvn dockerfile:tag@tag-version
            '''
            if (env.TAG_NAME ) {
             sh  "mvn dockerfile:tag@tag-latest"
            }
          }
        }
      }
    }


    stage("Scan Prep") {
      steps {
        withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
          sh '''#!/bin/bash
          mkdir -p scan_dependencies

          # Copy dependencies so OWASP and WICKED can scan them
          mvn -DincludeScope=runtime dependency:copy-dependencies -DoutputDirectory=scan_dependencies

          # Add internal libraries to the Wicked Suppression List
          mvn dependency:list | egrep '  net.iss.mss|  com.ibm' | awk -F':' '{printf "%s:%s:\n",$2,$4}' | sort | uniq >> wicked-suppression.txt
          '''
        }
      }
    }

    stage('Scans') {
      parallel {   

        stage('Dependency Check (SAST)') {
          steps {
            dependencyCheck additionalArguments: "--cveValidForHours 48 --prettyPrint --scan scan_dependencies --format ALL ${fileExists('owasp-suppression.xml') ? "--suppression owasp-suppression.xml" : ""} --disableAssembly", odcInstallation: 'dependency-check-5.3.2'
            dependencyCheckPublisher pattern: '', unstableTotalHigh: 1, unstableTotalCritical: 1
            archiveArtifacts allowEmptyArchive: true, artifacts: '**/dependency-check-report.html', onlyIfSuccessful: false            
          }
        }


        stage ('Wicked Scan (OSS)') {
          steps {
            script {
              withCredentials([usernamePassword(credentialsId: 'mss-artifactory-credentials', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD'), usernamePassword(credentialsId: 'ibm-github-credentials-managed-security', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD')]) {
                env.ENCODED_GITHUB_USERNAME = java.net.URLEncoder.encode(GITHUB_USERNAME)
                env.ENCODED_GITHUB_PASSWORD = java.net.URLEncoder.encode(GITHUB_PASSWORD)

                warnError('Wicked Scan Failed') {
                  try {
                    sh '''#!/bin/bash
                      set -o pipefail

                      USERNAME=`echo "${ARTIFACTORY_USERNAME}" | tr '[:upper:]' '[:lower:]'`
                      # Convert their password to base64
                      BASE64_PASSWORD=`printf ${ARTIFACTORY_PASSWORD} | base64`
                      npm config set @wicked:registry https://na.artifactory.swg-devops.com/artifactory/api/npm/wicked-npm-local/
                      npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/wicked-npm-local/:_password="${BASE64_PASSWORD}"
                      npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/wicked-npm-local/:username="${ARTIFACTORY_USERNAME}"
                      npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/wicked-npm-local/:email="${ARTIFACTORY_USERNAME}"
                      npm config set //na.artifactory.swg-devops.com/artifactory/api/npm/wicked-npm-local/:always-auth=true
                      # Verify their configuration
                      printf "\nTesting your configuration ..."
                      npm cache clean --force > /dev/null 2>&1
                      if npm view @wicked/cli > /dev/null 2>&1;
                        then
                          echo "\rSUCCESS! NPM is now configured to use the WICKED Artifactory repository for @wicked packages.\n"
                        else
                          echo "\rFAILED! Authentication was not successful. Please double check your credentials and try again.\n"
                      fi
                      npm install @wicked/cli -g
                      export WICKED_DIR=$(sudo npm install @wicked/cli -g | grep -oh "> .*/node_modules" | grep -oh " .*/")
                      echo ${WICKED_DIR}
                      sed -i 's/pedigree-service.wdc1a.cirrus.ibm.com/wicked-cio-bluemix.sec.ibm.com/g' ${WICKED_DIR}node_modules/@wicked/cli/cli_functions.js
                      pip3 install virtualenv
                      virtualenv -p python3 venv
                      source venv/bin/activate
                      pip3 install csvkit

                      if [[ -e wicked-suppression.txt && $(wc -l < wicked-suppression.txt) -gt 0 ]]; then
                        echo "WARNING: Overwriting existing wicked-suppresion.txt"
                      fi

                      # Fetch updated central COO database and wicked suppressions
                      echo "Ignore warning about 'no common commits'"
                      git remote add wicked-data "https://${ENCODED_GITHUB_USERNAME}:${ENCODED_GITHUB_PASSWORD}@github.ibm.com/managed-security/wicked-data.git"
                      git fetch -q wicked-data
                      if [[ $? != 0 ]]; then
                        echo "ERROR: Failed to fetch wicked-data repo"
                        exit 1
                      fi
                      git cat-file blob wicked-data/master:master.txt > wicked-suppression.txt
                      git cat-file blob wicked-data/master:suppression.txt >> wicked-suppression.txt

                      wicked-cli --output . --scan ./scan_dependencies --project ${REPOSITORY_NAME} --silent
                      # Clean up CSV for processing, reduce down to only the failures, and eliminate known false positives
                      tail -n +8 **/Scan-Report.csv | tail -n +2 | csvgrep -c 3 -r "true|TRUE" --no-header-row | csvgrep -c 9 -i -m " " | csvgrep -c 1 -i -m "gradle-wrapper" | tail -n +2  >wicked.csv
                      notApproved=()
                      licenseApproved=()

                      while read -r finding; do
                        fields=`echo "$finding" | csvcut -c 1,2,4,8,9`
                        name=`echo "$fields" | awk -F',' '{print $1}'`
                        version=`echo "$fields" | awk -F',' '{print $2}'`
                        reason=`echo "$fields" | awk -F',' '{print $3}'`
                        status=`echo "$fields" | awk -F',' '{print $4}'`
                        path=`echo "$fields" | awk -F',' '{print $5}'`
                        if [[ "$REPOSITORY_NAME" != "$name" ]] && ! grep "^$name:$version" wicked-suppression.txt > /dev/null; then
                          if [[ $status == "Approved" ]]; then
                            licenseApproved+=("$name:$version ($reason)")
                          else
                            notApproved+=("$name:$version ($reason)")
                          fi
                        fi
                      done < <(cat wicked.csv)
                      
                      if [ ${#notApproved[@]} -eq 0 ]; then
                        if [ ${#licenseApproved[@]} -eq 0 ]; then
                          echo "Wicked scan success! All libraries have valid pedigree." > res.txt
                        else
                          # This should trigger a warning. not error. pipeline should continue.
                          echo -e "Warning. Wicked scan succeeded but some libraries have Approved license:\n" > res.txt
                          for license in "${licenseApproved[@]}"
                            do
                              echo "  $license" >> res.txt
                            done
                          echo -e "\nThe above lines can be copied into a wicked-suppression.txt file to suppress each finding." >> res.txt
                        fi
                      else
                        # This should trigger an error in pipeline and stop pipeline.
                        echo -e "Wicked scan failed. The following libraries do not have a valid pedigree:\n" > res.txt
                        for finding in "${notApproved[@]}"
                        do
                          echo "  $finding" >> res.txt
                        done
                        echo -e "\nThe above lines can be copied into a wicked-suppression.txt file to suppress each finding." >> res.txt
                      fi
                    '''
                  }
                  finally {
                    archiveArtifacts allowEmptyArchive: true, artifacts: '*_scan-results/Scan-Report.json,*_scan-results/Scan-Report.csv,*_scan-results/License-Report.html', onlyIfSuccessful: false
                  }
                }
                warnError('Wicked Scan Found Libraries With Disallowed Licenses') {
                  sh '''#!/bin/bash
                    if grep -m 1 "Wicked scan failed" res.txt > /dev/null;
                      then
                        cat res.txt
                        exit 1
                    fi
                  '''
                }
                warnError('Wicked Scan Found Libraries Requiring Pedigree') {
                  sh '''#!/bin/bash
                    if grep -m 1 "Warning. Wicked scan succeeded" res.txt > /dev/null;
                      then
                        cat res.txt
                        exit 1
                    fi
                  '''
                }
              }
            }
          }
        }


        stage('Clair Scan ') {
          steps {  
            script {
              warnError('Clair Scan Failure') {
                docker.withRegistry("https://${DOCKER_SERVER}", DOCKER_CREDENTIALS) {
                  try {
                    sh '''#!/bin/bash
                    IMAGENAME=${DOCKER_SERVER}/${REPOSITORY_NAME}:'''+fullVersion +'''
                    CLAIR_DB_ID=`docker ps -a -q --filter name=db --format="{{.ID}}"`
                    CLAIR_SERVER_ID=`docker ps -a -q --filter name=clair --format="{{.ID}}"`
                    if [ ! -n "$CLAIR_DB_ID" ] || [ ! -n "$CLAIR_SERVER_ID" ]
                    then 
                        echo "Clair db and server is not running"
                        echo "Ensure that clair service is up"
                        touch clair-scanning-report.json
                        exit 1
                    else 
                        echo "Clair db is up and clair server is running ....."
                    fi
                    echo "About to start clair scanner ........."
                    touch clair-whitelist.yml
                    own_ip=$(hostname -i)
                    wget https://github.com/arminc/clair-scanner/releases/download/v8/clair-scanner_linux_amd64
                    mv clair-scanner_linux_amd64 clair-scanner
                    chmod +x clair-scanner
                    ./clair-scanner --ip=$own_ip -l clair.log -r clair-scanning-report.json -w clair-whitelist.yml $IMAGENAME
                    cat clair-scanning-report.json
                    if test -f "clair-scanning-report.json"; then
                        input="clair-scanning-report.json"
                        while IFS= read -r line
                        do
                            if [[ ("$line" == *"[]"* ) && ("$line" == *"unapproved"*) ]]; then
                                echo "clair found 0 vulnerabilities"
                                exit 0           
                        fi
                        done < "$input" 
                        echo "clair found vulnerabilities"
                        exit 1
                    
                    else
                        exit 1
                    fi
                    '''
                  } finally {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'clair-scanning-report.json', onlyIfSuccessful: false
                  }
                }
              } 
            }
          }
        }

      }
    }

    stage('Sonar') {
      steps {
        withSonarQubeEnv('mss-sonar') {
          withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
            sh '''#!/bin/bash

            npm install -g sonarqube-scanner
            CURRENT_BRANCH=${GIT_BRANCH}
            if [[ "${GIT_BRANCH}" != "master" && "${GIT_BRANCH}" != "integration" ]]; then
              # Check to see if repo has an integration branch
              hasBranch=`git branch -a | grep "remotes/origin/integration"`
              if [ "${hasBranch}" != "" ]; then
                TARGET_BRANCH="integration"
              else
                TARGET_BRANCH="master"
              fi
            elif [[ "${GIT_BRANCH}" == "integration" ]]; then
              TARGET_BRANCH="master"
            elif [[ "${GIT_BRANCH}" == "master" ]]; then
              TARGET_BRANCH=""
              CURRENT_BRANCH=""
            fi


            mvn -Dsonar.projectKey="${REPOSITORY_OWNER}:${REPOSITORY_NAME}" \
                -Dmaven.test.failure.ignore=true \
                -Dsonar.dependencyCheck.reportPath=./dependency-check-report.xml \
                -Dsonar.dependencyCheck.htmlReportPath=./dependency-check-report.html \
                -Dsonar.branch.name=${CURRENT_BRANCH} \
                -Dsonar.branch.target=${TARGET_BRANCH} \
                -Dsonar.links.scm=${GIT_URL} \
                -Dsonar.links.homepage="" \
                -Dsonar.exclusions=venv/**,package-lock.json \
                compile sonar:sonar
            '''
          }
        }
      }
    }

    stage("Quality Gate") {
      steps {
        script {
        // Temporary solution to prevent pipeline stuck on waitForQualityGate
        sleep(30)
          def qg = waitForQualityGate()
          if (qg.status != 'OK') {
            def msg =  "Pipeline aborted due to quality gate failure: ${qg.status}"
            if (env.TAG_NAME) {
              error msg
            } else {
              unstable msg
            }
          }
        }
      }
    }


    stage('Publish') {
      parallel { 
        stage('Container Image') {
          steps {
            script {
              withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
                sh "mvn package dockerfile:push@push-version -Ddocker.registry=${env.MSS_DOCKER_SERVER_PRD}"
                if (env.TAG_NAME ) {
                  sh "mvn package dockerfile:push@push-latest -Ddocker.registry=${env.MSS_DOCKER_SERVER_PRD}"
                }
              }
            }
          }
        }

        stage('Helm Chart') {
          steps {
            script {
              withCredentials([usernamePassword(
                credentialsId: 'mss-artifactory-credentials',
                usernameVariable: 'ARTIFACTORY_USERNAME',
                passwordVariable: 'ARTIFACTORY_APIKEY'
              )]) {  
                script { 

                  def yamlfile = readYaml (file: "chart/values.yaml")
                  yamlfile.image.tag = "${fullVersion}".toString()
                  sh 'rm chart/values.yaml'
                  writeYaml file: "chart/values.yaml", data: yamlfile
                  sh 'cat chart/values.yaml'

                  yamlfile = readYaml (file: "chart/Chart.yaml")
                  yamlfile.version = "${chartVersion}"
                  sh 'rm chart/Chart.yaml'
                  writeYaml file: "chart/Chart.yaml", data: yamlfile
                  sh 'cat chart/Chart.yaml'


                  sh 'helm package chart'
                  def CHART_FILE_NAME = sh(script: "echo \"${env.REPOSITORY_NAME}-\$(helm show chart chart | grep version | awk '{print \$NF}').tgz\"", returnStdout: true).trim()
                  sh  "curl -H 'X-JFrog-Art-Api:'${ARTIFACTORY_APIKEY}" +
                    " -T \"${CHART_FILE_NAME}\"" +
                    " https://na.artifactory.swg-devops.com/artifactory/mss-helm-local/${CHART_FILE_NAME}"
                }
              }
            }
          }
        }


      }
    }

    stage('OSS COO Artifacts') {
     when {
        buildingTag()
      }
      steps {
        script {
          withMaven(maven: 'maven-3', globalMavenSettingsConfig: 'mss-mvn-global-settings', options: [ artifactsPublisher(disabled: true) ]) {
            // Create and push the source source -- Needed for OSS Clearance
            sh "mvn source:jar -Dmaven.test.skip=true deploy"
            sh '''#/bin/bash
              grp=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.groupId -B | egrep -v '^\\[|^Down' | tail -n1`
              art=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.artifactId -B | egrep -v '^\\[|^Down' | tail -n1`
              ver=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version -B | egrep -v '^\\[|^Down' | tail -n1`

              echo "tar -czf ${art}-${ver}.tar.gz target/${art}-${ver}.jar scan_dependencies/*.jar"
              tar -czf ${art}-${ver}.tar.gz target/${art}-${ver}.jar scan_dependencies/*.jar

              echo "mvn deploy:deploy-file -DgeneratePom=false -DgroupId=${grp} -DartifactId=${art} -Dversion=${ver} -Dpackaging=tar.gz -DrepositoryId=mss-artifactory -Durl=${RELEASE_REPO} -Dclassifier=release -Dfile=${art}-${ver}.tar.gz"
              mvn deploy:deploy-file -Dmaven.test.skip=true -DgeneratePom=false -DgroupId=${grp} -DartifactId=${art} -Dversion=${ver} -Dpackaging=tar.gz -DrepositoryId=mss-artifactory -Durl=${RELEASE_REPO} -Dclassifier=release -Dfile=${art}-${ver}.tar.gz
            '''
          }
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      jacoco execPattern: 'target/jacoco.exec'
    }

    success {

      script {
        if (fullVersion) {
          echo "Published Application / Container Version: ${fullVersion}"
        }
        if (chartVersion) {
          echo "Published Chart Version: ${chartVersion}"
        }
      }

    }
    unstable {

      script {
        if (fullVersion) {
          echo "Published Application / Container Version: ${fullVersion}"
        }
        if (chartVersion) {
          echo "Published Chart Version: ${chartVersion}"
        }
      }

    }
  }
}
