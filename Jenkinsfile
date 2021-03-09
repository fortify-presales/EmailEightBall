#!/usr/bin/env groovy

pipeline {
    agent any

	//
    // The following parameters can be selected when the pipeline is executed manually to execute
    // different capabilities in the pipeline or configure the servers that are used.

    // Note: the pipeline needs to be executed at least once for the parameters to be available
    //
    parameters {         
        booleanParam(name: 'FOD_SAST',       	defaultValue: params.FOD_SAST ?: true,
                description: 'Use Fortify on Demand for Static Application Security Testing')
        booleanParam(name: 'FOD_DAST',       	defaultValue: params.FOD_DAST ?: false,
                description: 'Use Fortify on Demand for Dynamic Application Security Testing')        
    }

    environment {
        // Application settings
        APP_NAME = "EmailEightBall"                         // Application name
        APP_VER = "master"                                  // Application release - GitHub master branch
        COMPONENT_NAME = "EmailEightBall"                   // Component name
        GIT_URL = scm.getUserRemoteConfigs()[0].getUrl()    // Git Repo
        JAVA_VERSION = 1.8                                  // Java version to compile as
        ISSUE_IDS = ""                                      // List of issues found from commit
        FOD_UPLOAD_DIR = 'fod'                              // Directory where FOD upload Zip is constructed

        // Credential references
        GIT_CREDS = credentials('eightball-git-creds-id')
       
        // The following are defaulted and can be overriden by creating a "Build parameter" of the same name
        FOD_RELEASE_ID = "${params.FOD_RELEASE_ID ?: '6645'}" // Fortify on Demand Release Id
	}

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven 'M3'
    }

    stages {
        stage('Build') {
            // Run on "master" node
            agent { label 'master' }
            steps {
                // Get some code from a GitHub repository
                git credentialsId: 'eightball-git-creds-id', url: "${env.GIT_URL}"

                // Get Git commit details
                script {
                    if (isUnix()) {
                        sh 'git rev-parse HEAD > .git/commit-id'
                    } else {
                        bat(/git rev-parse HEAD > .git\\commit-id/)
                    }
                    //bat(/git log --format="%ae" | head -1 > .git\commit-author/)
                    env.GIT_COMMIT_ID = readFile('.git/commit-id').trim()
                    //env.GIT_COMMIT_AUTHOR = readFile('.git/commit-author').trim()

                    println "Git commit id: ${env.GIT_COMMIT_ID}"
                    //println "Git commit author: ${env.GIT_COMMIT_AUTHOR}"

                    // Run maven to build WAR/JAR application
                    if (isUnix()) {
                        sh 'mvn clean package'
                    } else {
                        bat "mvn clean package"
                    }
                }
            }

            post {
                success {
                    // Record the test results (success)
                    junit "**/target/surefire-reports/TEST-*.xml"
                    // Archive the built file
                    archiveArtifacts "target/${env.COMPONENT_NAME}.jar"
                    // Stash the deployable files
                    stash includes: "target/${env.COMPONENT_NAME}.jar", name: "${env.COMPONENT_NAME}_release"
                }
                failure {
                    // Record the test results (failures)
                    junit "**/target/surefire-reports/TEST-*.xml"
                }
            }
        }

        stage('SAST') {
            when {
            	beforeAgent true
            	anyOf {
            	    expression { params.FOD_SAST == true }
        	    }
            }
            // Run on an Agent with "fortify" label applied
            agent {label "fortify"}
            steps {
                script {
                    // Get code from Git repository so we can recompile it
                	git credentialsId: 'eightball-git-creds-id', url: "${env.GIT_URL}"

                    // Run Maven debug compile, download dependencies (if required) and package up for FOD
                    if (isUnix()) {
                        sh "mvn -Dmaven.compiler.debuglevel=lines,vars,source -DskipTests -P fortify clean verify"
                        sh "mvn dependency:build-classpath -Dmdep.regenerateFile=true -Dmdep.outputFile=${env.WORKSPACE}/cp.txt"
                    } else {
                        bat "mvn -Dmaven.compiler.debuglevel=lines,vars,source -DskipTests -P fortify clean verify"
                        bat "mvn dependency:build-classpath -Dmdep.regenerateFile=true -Dmdep.outputFile=${env.WORKSPACE}/cp.txt"
                    }

                    // read contents of classpath file
                    def classpath = readFile "${env.WORKSPACE}/cp.txt"
                    println "Using classpath: $classpath"

                    if (params.FOD_SAST) {
                        println "Starting FOD SAST for Release: ${env.FOD_RELEASE_ID}"
                        println "Uploading from ${env.FOD_UPLOAD_DIR}"
                        // Upload built application to Fortify on Demand and carry out Static Assessment
                        fodStaticAssessment entitlementPreference: 'SubscriptionOnly',
                                inProgressBuildResultType: 'WarnBuild',
                                inProgressScanActionType: 'Queue',
                                releaseId: "${env.FOD_RELEASE_ID}",
                                remediationScanPreferenceType: 'NonRemediationScanOnly',
                                srcLocation: "${env.FOD_UPLOAD_DIR}"
                        // optional: wait for FOD assessment to complete
                        fodPollResults releaseId: "${env.FOD_RELEASE_ID}",
                                policyFailureBuildResultPreference: 1,
                                pollingInterval: 5
                    } else {
                        println "No Static Application Security Testing (SAST) to do."
                    }
                }
            }
        }

        stage('Deploy') {
            // Run on "master" node
            agent { label 'master' }
            steps {
                script {
                    // unstash the built files
                    unstash name: "${env.COMPONENT_NAME}_release"
                    println "Deploying application ..."
                }
            }
        }

        stage('DAST') {
            when {
            	beforeAgent true
            	anyOf {
            	    expression { params.FOD_DAST == true }
        	    }
            }
            // Run on an Agent with "fortify" label applied
            agent {label "fortify"}
            steps {
                script {                    
                    if (params.FOD_DAST) {
                        println "DAST via FOD is not yet implemented."
                    } else {
                        println "No Dynamic Application Security Testing (DAST) to do."
                    }
                }
            }
        }
        
		// An example manual release checkpoint
        stage('Stage') {
        	agent { label 'master' }
        	steps {
                println "Skipping manual approval..."
            	//input id: 'Release',
            	//	message: 'Ready to Release?',
            	//	ok: 'Yes, let\'s go',
            	//	submitter: 'admin',
            	//	submitterParameter: 'approver'
        	}
        }

        stage('Release') {
            agent { label 'master' }
            steps {
                script {
                    println "Releasing application ..."
                }
            }
        }

    }

}
