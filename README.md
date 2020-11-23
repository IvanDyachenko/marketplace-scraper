# Mar🐈place crawler ![Scala CI/CD](https://github.com/IvanDyachenko/marketplace-crawler/workflows/Scala%20CI/CD/badge.svg)

### How to

#### Docker

Pull an image by `tag` (which equals to the corresponding `GITHUB_SHA`):
```bash
docker pull ivandyachenko/marketplace-crawler:<GITHUB_SHA>
```

Run the image via bare Docker:
```bash
docker run                         \
  --network="host"                 \
  --memory 200m                    \
  --env PROXY_HOST=$MITMPROXY_HOST \
  --env PROXY_PORT=$MITMPROXY_PORT \
  --name crawler ivandyachenko/marketplace-crawler:<GITHUB_SHA>
```

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
