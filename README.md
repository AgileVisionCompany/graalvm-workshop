# GraalVM Workshop
## Prepare development environment
### Local GraalVM
1. Install [SDKMAN](https://sdkman.io/install)
2. Install GraalVM 21.2 r11 `sdk install java 21.2.0.r11-grl`
3. Use GraalVm `sdk use java 21.2.0.r11-grl`
4. Install native-image extensions `gu install native-image`

### Docker Compose
Execute `docker-compose run --rm --service-ports graalvm` in the project root directory

### Docker
1. ```docker run -it --rm -v `pwd`:/root/project ghcr.io/graalvm/graalvm-ce:java11-21.2 bash```
2. `cd /root/project/`
3. `gu install native-image`
4. Install [Maven](https://maven.apache.org/install.html)

## "Vanilla" Java project
### Compile and run using JDK
1. `mvn clean package`
2. `time java -jar target/generate-html-email-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Compile and run native-image
1. `native-image -jar target/generate-html-email-1.0-SNAPSHOT-jar-with-dependencies.jar --no-fallback target/generate-html-email`
2. `java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/generate-html-email-1.0-SNAPSHOT-jar-with-dependencies.jar`
3. `time APP_LOGIN_EMAIL=email@ag.io ./target/generate-html-email`
4. Configure maven to generate native image 
```xml
<project>
  <properties>
    <native.maven.plugin.version>0.9.7.1</native.maven.plugin.version>
    <exe.file.name>generate-html-email</exe.file.name>
  </properties>

  <profiles>
    <profile>
      <id>java_agent</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>1.2</version>
            <configuration>
              <executable>java</executable>
              <arguments>
                <argument>
                  -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image/
                </argument>
                <argument>-classpath</argument>
                <classpath/>
                <argument>${app.main.class}</argument>
              </arguments>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
    <profile>
      <id>native</id>
      <build>
        <plugins>
          <!-- Native Image -->
          <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
            <version>${native.maven.plugin.version}</version>
            <extensions>true</extensions>
            <executions>
              <execution>
                <id>build-native</id>
                <goals>
                  <goal>build</goal>
                </goals>
                <phase>package</phase>
              </execution>
            </executions>
            <configuration>
              <!-- Set this to true if you need to switch this off -->
              <skip>false</skip>
              <imageName>${exe.file.name}</imageName>
              <mainClass>${app.main.class}</mainClass>
              <buildArgs>
                <buildArg>--no-fallback</buildArg>
                <buildArg>--report-unsupported-elements-at-runtime</buildArg>
                <buildArg>--allow-incomplete-classpath</buildArg>
              </buildArgs>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
</project>
```
5. `mvn clean package exec:exec -Pjava_agent`
6. `mvn clean package -Pnative`

## Spring Boot application
### Generate base spring boot project
1. Generate base project `spring-generate-html` using [Spring Initializr](https://start.spring.io/) or use intellij idea.
2. Select `spring-boot-starter-web` and `lombok` as dependency with spring boot `2.5.6`, java 11
3. Build java application `mvn clean package`
4. Run application `java -jar target/spring-generate-html-0.0.1-SNAPSHOT.jar`

### Build to native image with Native Build Tools
1. Add to pom.xnl:

```xml

<project>
  <properties>
    <spring.native>0.10.5</spring.native>
  </properties>

  <dependency>
    <groupId>org.springframework.experimental</groupId>
    <artifactId>spring-native</artifactId>
    <version>${spring.native}</version>
  </dependency>

  <profiles>
    <profile>
      <id>native</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.springframework.experimental</groupId>
            <artifactId>spring-aot-maven-plugin</artifactId>
            <version>${spring.native}</version>
            <executions>
              <execution>
                <id>test-generate</id>
                <goals>
                  <goal>test-generate</goal>
                </goals>
              </execution>
              <execution>
                <id>generate</id>
                <goals>
                  <goal>generate</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
            <version>0.9.4</version>
            <executions>
              <execution>
                <id>test-native</id>
                <goals>
                  <goal>test</goal>
                </goals>
                <phase>test</phase>
              </execution>
              <execution>
                <id>build-native</id>
                <goals>
                  <goal>build</goal>
                </goals>
                <phase>package</phase>
              </execution>
            </executions>
            <configuration>
              <!-- ... -->
            </configuration>
          </plugin>
          <!-- Avoid a clash between Spring Boot repackaging and native-maven-plugin -->
          <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
              <classifier>exec</classifier>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>

  <repositories>
    <repository>
      <id>spring-release</id>
      <name>Spring release</name>
      <url>https://repo.spring.io/release</url>
    </repository>
  </repositories>

  <pluginRepositories>
    <pluginRepository>
      <id>spring-release</id>
      <name>Spring release</name>
      <url>https://repo.spring.io/release</url>
    </pluginRepository>
  </pluginRepositories>
</project>
```
2. Build to native `mvn clean -Pnative -DskipTests package`
3. Run native application `./target/spring-generate-html`
4. Use NativeHint or `java -DspringAot=true -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/spring-generate-html-0.0.1-SNAPSHOT-exec.jar`

### Build to native image with Buildpacks
```xml
    <profile>
      <id>native-build-image</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.springframework.experimental</groupId>
            <artifactId>spring-aot-maven-plugin</artifactId>
            <version>${spring.native}</version>
            <executions>
              <execution>
                <id>test-generate</id>
                <goals>
                  <goal>test-generate</goal>
                </goals>
              </execution>
              <execution>
                <id>generate</id>
                <goals>
                  <goal>generate</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
              <image>
                <builder>paketobuildpacks/builder:tiny</builder>
                <env>
                  <BP_NATIVE_IMAGE>true</BP_NATIVE_IMAGE>
                </env>
                <buildpacks>
                  <buildpack>gcr.io/paketo-buildpacks/java-native-image:5.5.0</buildpack>
                </buildpacks>
              </image>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
```