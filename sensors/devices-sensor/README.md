Build the sensor docker:

```
docker build -t devices-sensor:latest .
```

Execute the sensor docker:

```
docker run -it -e ENDPOINT=http://192.168.1.113:8888/ -e MSG_PERSEC=10 -e NUM_THREADS=1 devices-sensor:latest
```

