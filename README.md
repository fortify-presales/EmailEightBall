# Email version of Fortify EightBall demo app

To develop/test (as Spring Boot application):

```aidl
mvn spring-boot:run -Dspring-boot.run.arguments=--input=1

```
To build:

```aidl
mvn package
```

To run:

```aidl
java -jar target\EmailEightBall.jar 2

```

Note: for working email you will need to enter details of a working gmail account in src\main\resources\email.properties.


