# DoriBottle SpringBoot API Server

You can find api documentation at click [here](https://api.doribottle-id.co.kr/swagger-ui/index.html).

## Running DoriBottle locally
DoriBottle is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Gradle](https://spring.io/guides/gs/gradle/). You can build a jar file and run it from the command line.

### Steps:
1) Download Redis image and start.
   ```
   docker pull redis:latest
   docker run -p 6379:6379 redis:latest
   ```
2) Download Mariadb image and start.
   ```
   docker pull mariadb:10.6
   docker run -e MARIADB_ROOT_PASSWORD=root -e MARIADB_DATABASE=doribottle_local -p 3306:3306 mariadb:10.6
   ```
3) Download source code from github repository.
   ```
   git clone https://github.com/team-liah/dori-bottle-server.git
   ```
4) Add environment variables.
5) Build jar file and run it.
   ```
   cd dori-bottle-server
   
   ./gradlew
   ./gradlew bootRun
   ```
