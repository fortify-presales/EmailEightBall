# Email version of Fortify EightBall demo app

To develop/test (as Spring Boot application):

```aidl
mvn spring-boot:run -Dspring-boot.run.arguments="--input=1"

```
To build:

```aidl
mvn package
```

To run:

```aidl
java -jar target/EmailEightBall.jar 2

```

Note: for working email you will need to enter the email address of a working user in `src/main/resources/application.yml`:

```aidl
app:
  name: Email Eight Ball
  version: 1.0
  email: user@localhost.com # <- REPLACE THIS ADDRESS
  operation: send
```
and you will also need to enter the details of a working SMTP server in `src/main/resources/email.properties`:

```aidl
email.server=smtp.sendgrid.net
email.port=587
email.username=apikey
email.password=#ENTER API SECRET HERE#
email.from=eightball@localhost.com
```

The default example use the "free" SMTP service from [SendGrid](https://sendgrid.com)


