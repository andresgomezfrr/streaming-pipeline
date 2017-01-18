Execute Secor
```
java -Dsecor_group=secor_partition -Dlog4j.configuration=log4j.prod.properties -Dconfig=secor.prod.partition.properties -cp ./lib/*:secor-0.22-SNAPSHOT.jar com.pinterest.secor.main.ConsumerMain
```
