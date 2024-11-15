# Stage 1: Cache Gradle dependencies
FROM gradle:latest AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle.* gradle.properties /home/gradle/app/
WORKDIR /home/gradle/app
RUN gradle clean build -i --stacktrace

# Stage 2: Build Application
FROM gradle:latest AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR, Gradle also supports shadow
# and boot JAR by default.
# todo переключить на прод в далёком будущем
#RUN gradle jsBrowserProductionWebpack --no-daemon
RUN gradle jsBrowserDevelopmentWebpack --no-daemon


# Stage 3: Create the Runtime Image
FROM node:alpine AS runtime
EXPOSE 8081:8081
RUN mkdir /app
WORKDIR /app
#COPY ./ ./
COPY --from=build /home/gradle/src/build/developmentExecutable/* /app

RUN npm install
ENTRYPOINT ["npm", "start"]

#WORKDIR /usr/app/front
#EXPOSE 3000
#COPY ./ ./
#RUN npm install
#CMD ["npm", "start"]