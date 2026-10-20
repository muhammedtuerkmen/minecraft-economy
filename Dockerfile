# SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
# Copyright (c) 2026 Muha
# Commercial use only with prior permission. See LICENSE.md.

# ---- Build ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Erst nur die Gradle-Dateien kopieren: Dependencies werden gecacht, solange sich diese nicht ändern
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon -q > /dev/null

COPY src src
RUN ./gradlew bootJar --no-daemon -q \
 && java -Djarmode=tools -jar build/libs/*-SNAPSHOT.jar extract --layers --launcher --destination extracted

# ---- Runtime ----
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring
USER spring

# Layers getrennt kopieren: bei Code-Änderungen wird nur der letzte Layer neu gebaut
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
