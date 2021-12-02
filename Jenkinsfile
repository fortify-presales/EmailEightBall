#!/usr/bin/env groovy

pipeline {
    agent any

	//
    // The following parameters can be selected when the pipeline is executed manually to execute
    // different capabilities in the pipeline or configure the servers that are used.

    // Note: the pipeline needs to be executed at least once for the parameters to be available
    //
    parameters {         
    booleanParam(name: 'SCA_LOCAL',       	defaultValue: params.SCA_LOCAL ?: true,
                description: 'Use (local) Fortify SCA for Static Application Security Testing')
        booleanParam(name: 'SCA_OSS',           defaultValue: params.SCA_OSS ?: false,
                description: 'Use Fortify SCA with Sonatype Nexus IQ for Open Source Susceptibility Analysis')
        booleanParam(name: 'SCANCENTRAL_SAST', 	defaultValue: params.SCANCENTRAL_SAST ?: false,
                description: 'Run a remote scan using Scan Central SAST (SCA) for Static Application Security Testing')
        booleanParam(name: 'UPLOAD_TO_SSC',		defaultValue: params.UPLOAD_TO_SSC ?: false,
                description: 'Enable upload of scan results to Fortify Software Security Center')
        booleanParam(name: 'FOD_SAST',       	defaultValue: params.FOD_SAST ?: false,
                description: 'Use Fortify on Demand for Static Application Security Testing')       
        gitParameter(branchFilter: 'origin/(.*)', defaultValue: 'main', name: 'BRANCH', type: 'PT_BRANCH')
    }

    environment {
        // Application settings
        APP_NAME = "EmailEightBall"                         // Application name
        APP_VER = "main"                                    // Application release - GitHub master branch
        COMPONENT_NAME = "EmailEightBall"                   // Component name
        GIT_URL = scm.getUserRemoteConfigs()[0].getUrl()    // Git Repo
        JAVA_VERSION = 1.8                                  // Java version to compile as
        ISSUE_IDS = ""                                      // List of issues found from commit
        FOD_UPLOAD_DIR = 'fod'                              // Directory where FOD upload Zip is constructed

        // Credential references
        GIT_CREDS = credentials('eightball-git-creds-id')
        SSC_AUTH_TOKEN = credentials('eightball-ssc-ci-token-id')
        NEXUS_IQ_AUTH_TOKEN = credentials('eightball-nexus-iq-token-id')
       
        // The following are defaulted and can be overridden by creating a "Build parameter" of the same name
        SSC_URL = "${params.SSC_URL ?: 'http://localhost:8080/ssc'}" // URL of Fortify Software Security Center
        SSC_APP_VERSION_ID = "${params.SSC_APP_VERSION_ID ?: '10000'}" // Id of Application in SSC to upload results to
        SSC_NOTIFY_EMAIL = "${params.SSC_NOTIFY_EMAIL ?: 'security@ftfydemo.com'}" // User to notify with SSC/ScanCentral information
        SSC_SENSOR_POOL_UUID = "${params.SSC_SENSOR_POOL_UUID ?: '00000000-0000-0000-0000-000000000002'}" // UUID of Scan Central Sensor Pool to use - leave for Default Pool
        FOD_RELEASE_ID = "${params.FOD_RELEASE_ID ?: '6446'}" // Fortify on Demand Release Id
        NEXUS_IQ_URL = "${params.NEXUS_IQ_URL ?: 'http://localhost:8070'}" // Sonatype Nexus IQ URL
	}

    tools {
        gradle "gradle-7.2"
    }

    stages {
        stage('Build') {
            // Run on "master" node
            agent { label 'master' }
            steps {
                // Get some code from a GitHub repository
                git credentialsId: 'eightball-git-creds-id', url: "${env.GIT_URL}",  branch: "${params.BRANCH}"

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
                        sh 'gradle clean build'
                    } else {
                        bat "gradle clean build"
                    }
                }
            }

            post {
                success {
                    // Record the test results (success)
                    junit "**/build/test-results/test/TEST-*.xml"
                    // Archive the built file
                    archiveArtifacts "build/libs/${env.COMPONENT_NAME}.jar"
                    // Stash the deployable files
                    stash includes: "build/libs/${env.COMPONENT_NAME}.jar", name: "${env.COMPONENT_NAME}_release"
                }
                failure {
                    // Record the test results (failures)
                    junit "**/build/test-results/test/TEST-*.xml"
                }
            }
        }

        stage('SAST') {
            when {
            	beforeAgent true
            	anyOf {
                    expression { params.SCA_LOCAL == true }
                    expression { params.SCANCENTRAL_SAST == true }
                    expression { params.FOD_SAST == true }        	    }
            }
            // Run on an Agent with "fortify" label applied
            agent {label "fortify"}
            steps {
                script {
                    // Get code from Git repository so we can recompile it
                	git credentialsId: 'eightball-git-creds-id', url: "${env.GIT_URL}",  branch: "${params.BRANCH}"

                    // Run gradle build
                    if (isUnix()) {
                        sh "gradle clean build writeClasspath"
                    } else {
                        bat "gradle clean build writeClasspath -x test"
                    }

                    // read contents of classpath file
                    def classpath = readFile "${env.WORKSPACE}/build/classpath.txt"
                    println "Using classpath: $classpath"

                    if (params.FOD_SAST) {
                        println "Using scancentral to package application"
                        if (isUnix()) {
                            sh "scancentral package -bt gradle -bf build.gradle -bc 'clean build writeClasspath -x test' -o fod.zip"
                        } else {
                            bat "scancentral package -bt gradle -bf build.gradle -bc 'clean build writeClasspath -x test' -o fod.zip"
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
 					} else if (params.SCANCENTRAL_SAST) {

                        // set any standard remote translation/scan options
                        fortifyRemoteArguments transOptions: '',
                                scanOptions: ''

                        if (params.UPLOAD_TO_SSC) {                                                                    
                            // Remote analysis (using Scan Central) and upload to SSC
                            fortifyRemoteAnalysis remoteAnalysisProjectType: fortifyGradle(buildFile: 'build.gradle'),
                                    remoteOptionalConfig: [
                                            customRulepacks: '',
                                            filterFile: "etc\\sca-filter.txt",
                                            notifyEmail: "${env.SSC_NOTIFY_EMAIL}",
                                            sensorPoolUUID: "${env.SSC_SENSOR_POOL_UUID}"
                                    ],
                                    uploadSSC: [appName: "${env.APP_NAME}", appVersion: "${env.APP_VER}"]

                        } else {
                            // Remote analysis (using Scan Central)
                            fortifyRemoteAnalysis remoteAnalysisProjectType: fortifyGradle(buildFile: 'build.gradle'),
                                    remoteOptionalConfig: [
                                            customRulepacks: '',
                                            filterFile: "etc\\sca-filter.txt",
                                            notifyEmail: "${env.SSC_NOTIFY_EMAIL}",
                                            sensorPoolUUID: "${env.SSC_SENSOR_POOL_UUID}"
                                    ]
                        }
                    } else if (params.SCA_LOCAL) {
                        // optional: update scan rules
                        //fortifyUpdate updateServerURL: 'https://update.fortify.com'

                        // Clean project and scan results from previous run
                        fortifyClean buildID: "${env.COMPONENT_NAME}",
                                logFile: "${env.COMPONENT_NAME}-clean.log"

						env.WORKSPACE = pwd()
						def ClassPath = readFile "${env.WORKSPACE}/build/classpath.txt"
						println "Classpath is $ClassPath"
						
                        // Translate source files
                        fortifyTranslate buildID: "${env.COMPONENT_NAME}",
                                projectScanType: fortifyJava(
                                	javaAddOptions: '', 
                                	javaClasspath: "$ClassPath", 
                                	javaSrcFiles: '"src/main/java/**/*" "src/main/resources/**/*" "Dockerfile" "Dockerfile.win"', 
                                	javaVersion: '8'
                                ),
                                addJVMOptions: '',
                                logFile: "${env.COMPONENT_NAME}-translate.log"

                        // Scan source files
                        fortifyScan buildID: "${env.COMPONENT_NAME}",
                                addOptions: '"-filter" "etc\\sca-filter.txt"',
                                resultsFile: "${env.COMPONENT_NAME}.fpr",
                                addJVMOptions: '',
                                logFile: "${env.COMPONENT_NAME}-scan.log"

                        if (params.UPLOAD_TO_SSC) {
                            // Upload to SSC
                            fortifyUpload appName: "${env.APP_NAME}",
                                    appVersion: "${env.APP_VER}",
                                    resultsFile: "${env.COMPONENT_NAME}.fpr"
                        }
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
        
        stage('SCA OSS') {
            when {
                beforeAgent true
                anyOf {
                    expression { params.SCA_OSS == true }
                }
            }
            // Run on an Agent with "fortify" label applied
            agent {label "fortify"}
            steps {
                script {

                    // nexusPolicyEvaluation advancedProperties: '',
                    //      enableDebugLogging: false,
                    //      failBuildOnNetworkError: true,
                    //      iqApplication: selectedApplication('EmailEightBall'),
                    //      iqModuleExcludes: [[moduleExclude: 'build/**/*test*.*']],
                    //      iqScanPatterns: [[scanPattern: 'build/**/*.jar']],
                    //      iqStage: 'develop',
                    //      jobCredentialsId: ''

                    // run sourceandlibscanner powershell script
                    def stdout = powershell(returnStdout: true, script: ".\\bin\\fortify-sourceandlibscanner.ps1 -NexusIQUrl ${env.NEXUS_IQ_URL} -NexusIQAuth ${env.NEXUS_IQ_AUTH_TOKEN} -NexusIQAppId ${env.APP_NAME} -SSCURL ${env.SSC_URL} -SSCAuthToken ${env.SSC_AUTH_TOKEN} -SSCAppVersionId ${env.APP_VER}")
                    println stdout
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
