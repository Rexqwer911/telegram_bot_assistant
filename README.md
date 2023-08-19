# Telegram Bot Assistant

An application for managing a telegram bot is designed to create and manage 
regular reminders, convenient communication with gpt 3 chat, as well as 
convenient entry of records of the work done during the day, 
and the formation of a monthly upload based on this data.

List of used technologies:
- JDK 17
- Spring Boot 2.7.5
- PostgreSQL
- Project Reactor
- GitHub Actions

___

## Application components:

### Message branch component

### Chat GPT component

### Voice recognition component

### Scheduled reminder component

___

## Prepairing the environment

First of all, we need to set up the environment:

Install JKD 17:
```
sudo apt-get install openjdk-17-jdk openjdk-17-jre
```

Install PostgreSQL:
```
sudo apt install postgresql
```

Initial scripts for PostgreSQL:
```
CREATE USER {{your_user}} with encrypted password '{{your_password}}';
CREATE DATABASE {{your_db}}
WITH OWNER = {{your_user}}
   ENCODING = 'UTF8'
   TABLESPACE = pg_default
   LC_COLLATE = 'en_US.UTF-8'
   LC_CTYPE = 'en_US.UTF-8'
   CONNECTION LIMIT = -1
   TEMPLATE template0;
```
Insert your values instead of {{your_db}}, {{your_user}} and {{your_password}}
___

## Application configuration file

We can build our application in 2 profiles - 'dev' and 'prod'


___

## Build the application

Development profile:

```
mvn clean install -Pdev
```

Production profile:

```
mvn clean install -Pprod
```

Build will be saved to the path: {project_folder}/target/tgapp.jar

___

## GitHub actions workflow file for CI/CD

So, we don't want to build our application manually, we can use 
Github Actions workflow.

GitHub Actions is a popular technology for implementing continuous integration 
and continuous deployment (CI/CD) workflows. It enables developers to automate 
various tasks and processes in their software development projects, 
such as building, testing, and deploying applications. With GitHub Actions, 
developers can define custom workflows using YAML files and leverage a vast 
ecosystem of pre-built actions to create efficient and automated CI/CD pipelines. 
It integrates seamlessly with GitHub, making it easy to trigger workflows based on 
events in repositories and enabling tight collaboration and automation within the 
development workflow.

Our yml file contains 5 steps:
1) Step 1 - Checkout main branch from Github
2) Step 2 - Set up JDK 17:

    GutHub actions runner prepairing environment and installing JDK 17.

3) Step 3 - Build with Maven

    Runner executes maven command to build the application jar.

4) Step 4 - SSH file transfer

   Sending the jar file to the server is done using the appleboy/ssh-action github action.
   
   We need to use the ssh key, that contains in our github repository secrets.

5) Step 5 - SSH execution restart scripts

   Now runner executes the application restart scripts. In my project the application stops, new jar 
   file replaces the old jar file, and finally the new jar file starts.

___