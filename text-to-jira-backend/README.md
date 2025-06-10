## Быстрый старт

Из папки `text-to-jira`:
```text
docker-compose up backend
```

Проверить работоспособность:
```text
curl localhost:8082/text/ping
```

Проверить соединение с jira:
```text
curl localhost:8082/jira/check
```


## Разработка

Запуск сервера (например): `gradle :text-to-jira-backend:runFatJar`

Конфигурация: [application.conf](src/main/resources/application.conf)

В конфигурации нужно настроить параметры:
- порт, чтобы связать с фронтом
- логин и пароль для jira
Параметры можно задать через env, см. [application.conf](src/main/resources/application.conf)

Работает на JVM 21 (на 11 не работает)


