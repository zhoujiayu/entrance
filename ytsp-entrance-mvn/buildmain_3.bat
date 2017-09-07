cd /d %~dp0
echo master=3 > src\main\resources\master.properties
start mvn clean package -o -Pmain -Dmaven.test.skip=true