### Text to Jira
Приложение для перевода тестового логирования в Jira в качестве work log.
На входе принимаются данные в определённом текстовом виде, описывающие работы за день. [Пример файла](workLog.example.txt).
Эти работы парятся и отправляются в подключенную jira. 

Пока возможно только тестовое использование с помощью: `UsageTest.testWriteRows`


Запуск сервера (например): `gradle runFatJar`
Конфигурация: [application.conf](text-to-jira-backend/src/main/resources/application.conf)
Работает на JVM 21 (на 11 не работает)

Дальнейшие работы: [Backlog](Backlog.md))
