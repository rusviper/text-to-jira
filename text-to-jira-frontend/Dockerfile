# Stage 0: Cache Gradle dependencies
FROM gradle:jdk21 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home

COPY build.gradle.* gradle.properties /home/gradle/app/
WORKDIR /home/gradle/app
RUN gradle clean build -i --stacktrace

# Этап 1: Сборка приложения
FROM gradle:jdk21 AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Копирование других необходимых модулей (если есть зависимости)
# COPY text-to-jira-api ./text-to-jira-api

# Сборка приложения
RUN gradle jsBrowserDevelopmentWebpack --no-daemon

# Этап 2: Настройка окружения для запуска
FROM node:14-alpine

WORKDIR /app

# Копирование собранных файлов из этапа сборки
COPY --from=build /home/gradle/src/build/distributions ./

# Установка http-server для обслуживания статических файлов
RUN npm install -g http-server

# Открытие порта
EXPOSE 8081

# Запуск сервера
CMD ["http-server", "-p", "8081"]