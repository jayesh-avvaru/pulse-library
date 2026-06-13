FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy source files
COPY src ./src

# Find all .java files and compile them
RUN find src -name "*.java" > sources.txt && \
    javac -d out @sources.txt

EXPOSE 8080
CMD ["java", "-cp", "out", "com.pulselibrary.Main"]