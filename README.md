# DoriBottle SpringBoot API Server

You can find api documentation at click [here](https://api.doribottle-id.co.kr/swagger-ui/index.html).

## Running DoriBottle locally
DoriBottle is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Gradle](https://spring.io/guides/gs/gradle/). You can build a jar file and run it from the command line.

### Steps:
1) Download source code from github repository.
   ```
   git clone https://github.com/team-liah/dori-bottle-server.git
   
   cd dori-bottle-server
   ```
2) Create and Start Redis container.
   ```
   docker-compose -f docker/redis.yml up -d
   ```
3) Create and Start MariaDB container.
   ```
   docker-compose -f docker/mariadb.yml up -d 
   ```
4) Add environment variables.
5) Build jar file and run it.
   ```
   ./gradlew
   ./gradlew bootRun
   ```