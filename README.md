# Mar🐈place crawler ![Scala CI/CD](https://github.com/IvanDyachenko/marketplace-crawler/workflows/Scala%20CI/CD/badge.svg) [![time tracker](https://wakatime.com/badge/github/IvanDyachenko/marketplace-crawler.svg)](https://wakatime.com/badge/github/IvanDyachenko/marketplace-crawler)

### Docker
Run containers in the background:
```bash
docker-compose up --remove-orphans --detach
```

#### ClickHouse
Connect to ClickHouse server using native command-line client:
```bash
docker-compose exec ch-client sh
# clickhouse-client --host=ch-server
```

#### Schema registry
Get list of all subjects:
```bash
docker-compose exec schema-registry sh
# curl -X GET http://schema-registry:8081/subjects
```

### Mitmproxy
Install the mitmproxy CA certificate manually by using `keytool`:
```bash
sudo $JAVA_HOME/bin/keytool -noprompt -importcert -trustcacerts \
  -keystore $JAVA_HOME/jre/lib/security/cacerts \
  -storepass changeit \
  -alias mitmproxycert \
  -file ~/.mitmproxy/mitmproxy-ca-cert.pem
```

Delete the mitmproxy CA certificate manually by using `keytool`:
```bash
sudo $JAVA_HOME/bin/keytool -noprompt -delete \
  -keystore $JAVA_HOME/jre/lib/security/cacerts \
  -storepass changeit \
  -alias mitmproxycert 
```


Start [mitmproxy in regular mode](https://docs.mitmproxy.org/stable/concepts-modes/#regular-proxy):
```bash
mitmproxy --verbose --server --mode regular --listen-port 8888 --allow-hosts 'mobile.market.yandex.net:443' --no-http2
```
