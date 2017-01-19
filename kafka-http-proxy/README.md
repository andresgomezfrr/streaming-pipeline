
## Kafka-pixy

```
curl -L https://github.com/mailgun/kafka-pixy/releases/download/v0.11.1/kafka-pixy-v0.11.1-linux-amd64.tar.gz | tar xz
```

```
cd kafka-pixy-v0.11.1-linux-amd64
./kafka-pixy --kafkaPeers "<host1>:9092,...,<hostN>:9092" --zookeeperPeers "<host1>:2181,...,<hostM>:2181"
```

Repo: https://github.com/mailgun/kafka-pixy

## n2kafka

Install on CentOS

```
yum install -y epel-release
```

```
rpm -ivh http://repo.redborder.com/redborder-repo-0.0.3-1.el7.rb.noarch.rpm
```

```
yum install -y expat ncurses libpsl libev zlib libmicrohttpd libcurl librd0 librdkafka1 jansson yajl
```


kafka-http-proxy repo: https://github.com/redBorder/n2kafka
