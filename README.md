# Public-Enemy-Back-Office
Back-office services for Public-Enemy
REST API for communication between: 
- Public-Enemy frontend
- Pogues Backoffice
- Eno
- Stromae backoffice

## Requirements
For building and running the application you need:
- [JDK 17](https://jdk.java.net/archive/)
- Maven 3

## Install and excute unit tests
Use the maven clean and maven install
```shell
mvn clean install
```  

## Running the application locally
Use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:
```shell
mvn spring-boot:run
```

## Application Accesses locally
To access to swagger-ui, use this url : [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Deploy application on Tomcat server
### 1. Package the application
Use the [Spring Boot Maven plugin]  (https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:
```shell
mvn clean package
```  
The jar will be generate in `/target` repository

### 2. Launch

```shell
java -jar public-enemy-api-XXX.jar
```  


#### Properties to edit
Create an application profile file (application-dev.yml for example, don't forget to add profile when using spring-boot:run) and override necessary properties from application.yaml
```shell  
spring:
  # public-enemy db
  datasource:
    url: jdbc:postgresql://localhost:5432/public-enemy-db
    username: public-enemy
    password: public-enemyPassword
    hikari.maximumPoolSize: 2
  # where logs are stored
logging:
  file:
    path: /var/log
application:
  # if you need proxy
  proxy:
    enable: true
    url: proxy.xxx.com
    port: 80
  # allowed origin: url of your public-enemu frontend
  cors:
    allowed-origins: http://localhost:3000
  # pogues backoffice url
  pogues:
    url: https://pogues-back-office.xxx.com
  # eno ws url
  eno:
    url: https://eno.xxx.com
  # queen backoffice for stromae
  queen:
    url: https://stromae-api.xxx.com
```

### 5. Application Access
To access to swagger-ui, use this url : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)  

## Before you commit
Before committing code please ensure,  
1 - README.md is updated  
2 - A successful build is run and all tests are sucessful  
3 - All newly implemented APIs are documented  
4 - All newly added properties are documented

## Global project concept

Public-Enemy Backoffice (& frontend) is a tool used to test questionnaires with data. Questionnaire designer create questionnaire in pogues but can't test them with predefined data directly. Public-Enemy gives to the designer the ability to inject a questionnaire in orchestrators (stromae, queen soon), and add predefined survey units data in the orchestrators for this questionnaire.

Public-Enemy's global functionalities for a designer:

- create a questionnaire in public-enemy
    - retrieve the questionnaire from pogues
    - create the questionnaire in public-enemy
    - inject the questionnaire and survey units data (given by the designer when creating questionnaire in public-enemy) in the orchestrators
- update a questionnaire in public-enemy
    - synchronise the questionnaire with the orchestrators (when pogues questionnaire has been edited for example, or to change survey units data)
- delete a questionnaire in public-enemy
    - delete questionnaire in orchestrators
- access to questionnaire in orchestrators
    - designer can access to a questionnaire in orchestrators for a specific survey unit
    - designer can reset a specific survey unit data
  
## End-Points in /api
- Modes
    - `GET /modes` : get the insee questionnaire modes

- Contexts
    - `GET /contexts` : get the insee questionnaire contexts

- Questionnaires
  - `POST /questionnaires/{questionnaireId}` : update an existing questionnaire and synchronize it with orchestrators
  - `POST /questionnaires/add` : create a new questionnaire and inject it to orchestrators (with survey units external data as csv file)
  - `GET  /questionnaires` : get list of questionnaires 
  - `GET  /questionnaires/{poguesId}/db` : get questionnaire from pogues questionnaire id
  - `GET  /questionnaires/{id}` : get questionnaire
  - `GET  /questionnaires/{id}/data` : get survey units data (as csv file)
  - `GET  /questionnaires/pogues/{poguesId}` : get questionnaire informations coming from pogues
  - `DELETE /questionnaires/{id}/delete` : delete a questionnaire

- Survey Units
  - `PUT /survey-units/{surveyUnitId}/reset` : reset collected data from a survey unit (keep external data)
  - `POST /questionnaires/{poguesId}/checkdata` : check csv data file against variables definition from a questionnaire 
  - `GET /questionnaires/{questionnaireId}/modes/{modeName}/survey-units` : get survey units for a questionnaire
  - `GET /questionnaires/{poguesId}/csv` : get CSV schema from a questionnaire variables definitions 

## Libraries used
- spring-boot-jpa
- spring-boot-security
- spring-boot-web
- spring-boot-tomcat
- spring-boot-test
- postgresql
- junit
- springdoc
- spring-boot-actuator
- liquibase

## License
Please check [LICENSE](https://github.com/InseeFr/Public-Enemy-Back-Office/blob/main/LICENSE) file

