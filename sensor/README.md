Build the sensor docker:

```
docker build -t sensor:latest .
```

Execute the sensor docker:

```
docker run -it -e HTTP_ENDPOINT=localhost:8088 -e SENSOR_NAME=my-sensor-1 sensor:latest -e SENSOR_IP=127.0.0.1
```

Sensor repo: https://github.com/redBorder/rb_monitor


Load Balancer: kschool-LB-2118706800.us-east-1.elb.amazonaws.com
