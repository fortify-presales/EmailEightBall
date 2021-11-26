# Email version of Fortify EightBall demo app

This is a simple email based version of the Fortify [Magic EightBall](https://en.wikipedia.org/wiki/Magic_8-Ball) demo app that has a few security issues
that can be found using [Fortify Static Code Analyzer](https://www.microfocus.com/en-us/cyberres/application-security/static-code-analyzer)
and [Fortify Software Composition Analysis](https://www.microfocus.com/en-us/cyberres/application-security/software-composition-analysis). 

To build/run the application you will need to have a Java JDK (1.11 or later) and the [Gradle Build Tool](https://gradle.org/)
installed.

To build:

```aidl
gradlew clean build
```

To run, first start the included "fake" [email server](https://github.com/gessnerfl/fake-smtp-server):

```aidl
java -Dspring.config.location=.\etc\fake-smtp-server.properties -jar .\lib\fake-smtp-server-1.8.1.jar
```

and then in different console to run (with defaults):

```aidl
java -jar .\build\libs\EmailEightBall-1.0-SNAPSHOT.jar
```

You can specify different options on the command line to ask different questions and/or send
the results to a different email address, for example:

```aidl
java -jar .\build\libs\EmailEightBall-1.0-SNAPSHOT.jar "Does my bum look big in this?" biggie@localhost.com
```

You can see the resultant emails on the console or on the [email server UI](http://localhost:5080/).

To run a Fortify SCA scan on the source code you can use the provided PowerShell script [fortify-sca.ps1](bin\fortify-sca.ps1).
In order for the script to work you will need to create a ```.env``` file in the project root
directory with contents similar to the following:

```aidl
# The URL of Software Security Center
SSC_URL=http://localhost:8080/ssc
SSC_USERNAME=username
SSC_PASSWORD=passwod
# SSC Authentication Token (recommended to use CIToken)
SSC_AUTH_TOKEN=xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
# Name of the application in SSC
SSC_APP_NAME=EmailEightBall
# Name of the application version in SSC
SSC_APP_VER_NAME=1.0
SCANCENTRAL_CTRL_URL=http://localhost:8080/scancentral-ctrl
SCANCENTRAL_CTRL_TOKEN=xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
SCANCENTRAL_POOL_ID=00000000-0000-0000-0000-000000000002
SCANCENTRAL_EMAIL=<your email address>
NEXUS_IQ_URL=http://localhost:8070
NEXUS_IQ_AUTH=XXX:YYY
NEXUS_IQ_APP_ID=EmailEightBall
FOD_API_URL=https://api.emea.fortify.com
FOD_API_KEY=xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
FOD_API_SECRET=xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
```

Note: this file should NOT be added to source control.
