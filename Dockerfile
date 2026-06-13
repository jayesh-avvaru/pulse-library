FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY backend/src ./src
COPY frontend ./frontend
RUN find src -name "*.java" > sources.txt && \
    javac -d out @sources.txt
EXPOSE 8080
CMD ["java", "-cp", "out", "com.pulselibrary.Main"]
