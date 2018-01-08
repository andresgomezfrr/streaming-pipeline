Build the sensor docker:

```
docker build -t iot-sensor:latest .
```

Execute the sensor docker:

${NUM_MESSAGES} ${INTERVAL_MS} ${NUM_THREADS}

```
docker run -it -e MQTT_BROKER_HOST=broker.hivemq.com -e MQTT_BROKER_PORT=1883 -e MQTT_BROKER_TOPIC=streaming-platform/sensor/data -e NUM_MESSAGES=10000 -e INTERVAL_MS=1000 -e NUM_THREADS=1 iot-sensor:latest
```

