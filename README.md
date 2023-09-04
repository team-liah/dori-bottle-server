# DoriBottle SpringBoot API Server

You can find api documentation at click [here](https://api.doribottle-id.co.kr/swagger-ui/index.html).

## Running DoriBottle locally
DoriBottle is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Gradle](https://spring.io/guides/gs/gradle/). You can build a jar file and run it from the command line.

### Steps:
1) Install Redis and start.
   ```
   brew install redis
   redis-server
   ```
2) Install H2 Database and start.
   ```
   brew install h2
   h2
   ```
3) Modify Ip 218.38.137.27 to localhost.
   ```
   http://localhost:8082/?key=value
   ```
4) Modify JDBC URL and connect database.
   ```
   jdbc:h2:~/doribottle-h2-local
   ```
5) Verify .mv.db file is created in the home directory.
6) After disconnecting from the h2 database, modify the JDBC URL and re-connect database.
   ```
   jdbc:h2:tcp://localhost/~/doribottle-h2-local
   ```
7) Download source code from github repository.
   ```
   git clone https://github.com/team-liah/dori-bottle-server.git
   ```
8) Add environment variables.
9) Build jar file and run it.
   ```
   cd dori-bottle-server
   
   ./gradlew
   ./gradlew bootRun
   ```