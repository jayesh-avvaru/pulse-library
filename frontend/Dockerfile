FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy both backend and frontend
COPY backend/src ./src
COPY frontend ./frontend

# Compile all Java files
RUN find src -name "*.java" > sources.txt && \
    javac -d out @sources.txt

EXPOSE 8080
CMD ["java", "-cp", "out", "com.pulselibrary.Main"]