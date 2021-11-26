Write-Host "Starting email server..."
& java -Dspring.config.location=.\etc\fake-smtp-server.properties -jar .\lib\fake-smtp-server-1.8.1.jar
