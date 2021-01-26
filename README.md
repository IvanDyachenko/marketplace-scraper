# Mar🐈place crawler [![Scala CI/CD](https://github.com/IvanDyachenko/marketplace-crawler/workflows/Scala%20CI/CD/badge.svg)](https://github.com/IvanDyachenko/marketplace-crawler/actions?query=workflow%3A%22Scala+CI%2FCD%22) [![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org) [![time tracker](https://wakatime.com/badge/github/IvanDyachenko/marketplace-crawler.svg)](https://wakatime.com/badge/github/IvanDyachenko/marketplace-crawler)

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
