# Mar🐈place crawler ![Scala CI/CD](https://github.com/IvanDyachenko/marketplace-crawler/workflows/Scala%20CI/CD/badge.svg)

### How to

#### Mitmproxy

Start [mitmproxy in regular mode](https://docs.mitmproxy.org/stable/concepts-modes/#regular-proxy):
```bash
mitmproxy --verbose --server --mode regular --listen-port 8888 --allow-hosts 'mobile.market.yandex.net:443' --no-http2
```

#### ClickHouse

Run containers in the background:
```bash
docker-compose up --detach
```

Connect to ClickHouse server using native command-line client:
```bash
docker-compose exec ch-client sh
# clickhouse-client --host=ch-server
```
