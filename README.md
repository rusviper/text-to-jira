## Text to Jira
Приложение для перевода тестового логирования в Jira в качестве work log.
На входе принимаются данные в определённом текстовом виде, описывающие работы за день. [Пример файла](workLog.example.txt).
Эти работы парятся и отправляются в подключенную jira. 

### Быстрый запуск

1. Запуск приложения под Docker:

   `docker-compose up`

2. Приложение откроется по следующему адресу:

   `http://127.0.0.1:8081/`
### Тестовое использование

Возможно тестовое использование с помощью: `UsageTest.testWriteRows`

### Другое

Дальнейшие работы: [Backlog](Backlog.md))