cd /d %~dp0
start mvn clean package -o -Dmaven.test.skip=true