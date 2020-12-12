# currency-app
Web-app created for viewing currency ratios to EUR, currency history and currency calculations..

## Project status:
Paused

## Technologies
Project has to be compiled on java 11.
Made with spring boot & thymeleaf.

## Database
H2 used for database management.
Access point for console view:
http://localhost:8089/h2-console

Details can be viewed in src/main/resources/application.properties

## Launching
Project needs to be built using maven commands:
"mvn jaxb2:generate" to generate classes for the project
or "mvn install"

Running spring boot server
In home folder open windows terminal:
mvnw spring-boot:run

Access in browser:
http://localhost:8089/
