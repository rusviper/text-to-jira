
Запуск сервера (например): `gradle :text-to-jira-backend:runFatJar`

Конфигурация: [application.conf](src/main/resources/application.conf)

В конфигурации нужно настроить параметры:
- порт, чтобы связать с фронтом
- логин и пароль для jira
Параметры можно задать через env, см. [application.conf](src/main/resources/application.conf)

Работает на JVM 21 (на 11 не работает)