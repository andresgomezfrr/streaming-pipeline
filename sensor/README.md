Build the sensor docker:

```
docker build -t sensor:latest .
```

Execute the sensor docker:

```
docker run -it -e HTTP_ENDPOINT=localhost:8088 -e SENSOR_NAME=my-sensor-1 sensor:latest
```
