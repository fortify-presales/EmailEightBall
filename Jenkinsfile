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
        COMPONENT_VERSION = "1.0-SNAPSHOT"                  // Component version
        GIT_URL = scm.getUserRemoteConfigs()[0].getUrl()    // Git Repo
        JAVA_VERSION = 1.8                                  // Java version to compile as
        ISSUE_IDS = ""                                      // List of issues found from commit
        FOD_UPLOAD_DIR = 'fod'                              // Directory where FOD upload Zip is constructed

        // Credential references
        GIT_CREDS = credentials('eightball-git-creds-id')
       
        // The following are defaulted and can be overridden by creating a "Build parameter" of the same name
        FOD_RELEASE_ID = "${params.FOD_RELEASE_ID ?: '6678'}" // Fortify on Demand Release Id
	}

    tools {
        // None as of yet
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

                    // Run gradle to build JAR
                    if (isUnix()) {
                        sh 'gradlew clean build'
                    } else {
                        bat "gradlew clean build"
                    }
                }
            }

            post {
                success {
                    // Record the test results (success)
                    junit "**/build/test-results/TEST-*.xml"
                    // Archive the built file
                    archiveArtifacts "build/lib/${env.COMPONENT_NAME}-${env.COMPONENT_VERSION}.jar"
                    // Stash the deployable files
                    stash includes: "build/${env.COMPONENT_NAME}-${env.COMPONENT_VERSION}.jar", name: "${env.COMPONENT_NAME}_release"
                }
                failure {
                    // Record the test results (failures)
                    junit "**/build/test-results/TEST-*.xml"
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

                    // Run gradle build
                    if (isUnix()) {
                        sh "gradlew clean build writeClasspath"
                    } else {
                        sh "gradlew clean build writeClasspath"
                    }

                    // read contents of classpath file
                    def classpath = readFile "${env.WORKSPACE}/build/classpath.txt"
                    println "Using classpath: $classpath"

                    if (params.FOD_SAST) {
                        println "Using scancentral to package application"
                        if (isUnix()) {
                            sh "scancentral package -bt gradle -bf build.gradle -bc 'clean build writeClasspath -x test' -o fod.zip"
                        } else {
                            sh "scancentral package -bt gradle -bf build.gradle -bc 'clean build writeClasspath -x test' -o fod.zip"
                        }
                        unzip zipFile: fod.zip, dir: "${env.FOD_UPLOAD_DIR}"
                        println "Starting FOD SAST for Release: ${env.FOD_RELEASE_ID}"
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
            post {
                always {
                    dir("${env.FOD_UPLOAD_DIR}") {
                        deleteDir()
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
