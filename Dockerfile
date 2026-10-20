ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy AS builder
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY domain/pom.xml domain/pom.xml
COPY application/pom.xml application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
RUN ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY domain/src/ domain/src/
COPY application/src/ application/src/
COPY infrastructure/src/ infrastructure/src/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests clean package
RUN java -Djarmode=tools \
    -jar infrastructure/target/infrastructure-0.1.0-SNAPSHOT.jar \
    extract --layers --destination /workspace/extracted

FROM eclipse-temurin:${JAVA_VERSION}-jre-jammy AS runtime
WORKDIR /application

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/application/ ./

ENTRYPOINT ["java", "-jar", "infrastructure-0.1.0-SNAPSHOT.jar"]
