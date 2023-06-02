# DoriBottle SpringBoot API Server

## Running DoriBottle locally
DoriBottle is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Gradle](https://spring.io/guides/gs/gradle/). You can build a jar file and run it from the command line.

### Steps:
1) Install H2 Database and start.
   ```
   brew install h2
   h2
   ```
2) Modify Ip 218.38.137.27 to localhost.
   ```
   http://localhost:8082/?key=value
   ```
3) Modify JDBC URL and connect database.
   ```
   jdbc:h2:~/doribottle-h2-local
   ```
4) Verify .mv.db file is created in the home directory.
5) After disconnecting from the h2 database, modify the JDBC URL.
   ```
   jdbc:h2:tcp://localhost/~/doribottle-h2-local
   ```
6) Download source code from github repository.
   ```
   git clone https://github.com/team-liah/dori-bottle-server.git
   ```
7) Build jar file and run it.
   ```
   cd dori-bottle-server
   
   ./gradlew
   ./gradlew bootRun
   ```