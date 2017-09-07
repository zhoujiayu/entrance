cd /d %~dp0
start mvn clean package -o -Pmain -Dmaven.test.skip=true