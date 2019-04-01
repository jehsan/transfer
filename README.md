# transfer
a simple money transfer api

# for packaging the project skip tests
mvn -Dmaven.test.skip=true package

# for running project use command below. If port is not specified, 8080 will be set as default
java -jar [PATH-TO-TARGET]/transfer.jar [PORT]
