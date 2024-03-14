FROM openjdk:18
EXPOSE 8080
COPY target/SEP-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/stockfish/stockfish_14_x64_avx2 /stockfish/stockfish_14_x64_avx2
RUN chmod +x /stockfish/stockfish_14_x64_avx2  #versichert, dass Engine ausgeführt werden darf
ENTRYPOINT ["java", "-jar", "/app.jar"]