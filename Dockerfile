# Stage 1: Build ClojureScript SPA (needs Node.js + Java for shadow-cljs)
FROM eclipse-temurin:21-jdk AS spa-builder
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
WORKDIR /app/vetsoft-spa
COPY vetsoft-spa/package.json vetsoft-spa/package-lock.json* ./
RUN npm ci
COPY vetsoft-spa/ ./
RUN npx shadow-cljs release app

# Stage 2: Build Clojure uberjar
FROM clojure:temurin-21-tools-deps AS api-builder
WORKDIR /app/vetsoft-api
COPY vetsoft-api/deps.edn vetsoft-api/build.clj ./
RUN clojure -P
COPY vetsoft-api/src/ ./src/
COPY vetsoft-api/resources/ ./resources/
# Copy compiled SPA into backend resources
COPY --from=spa-builder /app/vetsoft-spa/public/ ./resources/public/
RUN clojure -T:build uber

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=api-builder /app/vetsoft-api/target/vetsoft.jar ./vetsoft.jar
EXPOSE 8080
CMD ["java", "-jar", "vetsoft.jar"]
