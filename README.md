# Mar🐈place scraper [![Scala CI/CD](https://github.com/IvanDyachenko/marketplace-crawler/workflows/Scala%20CI/CD/badge.svg)](https://github.com/IvanDyachenko/marketplace-crawler/actions?query=workflow%3A%22Scala+CI%2FCD%22) [![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

## Scheduler

#### Run the docker image in the background

```bash
docker run --detach \
  --restart=always \
  --sysctl net.core.somaxconn=65536 \
  --sysctl net.core.netdev_max_backlog=65536 \
  --sysctl net.core.wmem_max=16777216 \
  --sysctl net.core.rmem_max=16777216 \
  --sysctl net.ipv4.ip_local_port_range="1024 65000" \
  --sysctl net.ipv4.tcp_mem="524288 1048576 4194304" \
  --sysctl net.ipv4.tcp_wmem="4096 87380 16777216" \
  --sysctl net.ipv4.tcp_rmem="4096 131072 16777216" \
  --sysctl net.ipv4.tcp_tw_reuse=1 \
  --sysctl net.ipv4.tcp_fin_timeout=10 \
  --sysctl net.ipv4.tcp_max_tw_buckets=65536 \
  --sysctl net.ipv4.tcp_max_syn_backlog=65536 \
  --memory=<number>[<unit>] \
  --env KAFKA_BOOTSTRAP_SERVERS=<host>:<port> \
  --env SCHEMA_REGISTRY_BASE_URL=http://<host>:<port> \
  --add-host api.ozon.ru:<ip> \
  --name marketplace-scheduler ivandyachenko/marketplace-scheduler:<tag>
```

## Handler

#### Run the docker image in the background

```bash
docker run --detach \
  --restart=always \
  --sysctl net.core.somaxconn=65536 \
  --sysctl net.core.netdev_max_backlog=65536 \
  --sysctl net.core.wmem_max=16777216 \
  --sysctl net.core.rmem_max=16777216 \
  --sysctl net.ipv4.ip_local_port_range="1024 65000" \
  --sysctl net.ipv4.tcp_mem="524288 1048576 4194304" \
  --sysctl net.ipv4.tcp_wmem="4096 87380 16777216" \
  --sysctl net.ipv4.tcp_rmem="4096 131072 16777216" \
  --sysctl net.ipv4.tcp_tw_reuse=1 \
  --sysctl net.ipv4.tcp_fin_timeout=10 \
  --sysctl net.ipv4.tcp_max_tw_buckets=65536 \
  --sysctl net.ipv4.tcp_max_syn_backlog=65536 \
  --memory=<number>[<unit>] \
  --env KAFKA_BOOTSTRAP_SERVERS=<host>:<port> \
  --env SCHEMA_REGISTRY_BASE_URL=http://<host>:<port> \
  --add-host api.ozon.ru:<ip> \
  --name marketplace-handler ivandyachenko/marketplace-handler:<tag>
```
