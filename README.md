# Paymentology Technical Project #


## What is this repository for? ##

* This project has been developed using Spring Boot framework, which provides an easy-to-use platform for managing 
  and comparing two CSV files, and generating a report detailing the number of perfectly matched transactions 
  through RESTful APIs. With this user-friendly tool, you can effortlessly upload two CSV files 
  and obtain a comprehensive comparison summary.
  
## Prerequisites ##
* Java 17
* Maven 3 or above
* Spring Boot 3


## Building Project ##

* On the terminal run the below command to build project.
  
``` mvn clean install ```

## Running Project ##
* To start the application on localhost:8080, run the command below on the terminal.
  #### localhost:8080 ####

``` mvn spring-boot:run ```

## Running Tests ##
* To run test by using below command
``` 
 mvn clean test 
```

## Endpoints ###

| Command |HTTP Method| Endpoint |
| --- | --- | --- |
| upload | POST |localhost:8080/api/v1/upload |
