package marketplace.db

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import marketplace.config.ClickhouseConfig

object ClickhouseXa {

  def make[F[_]: Async: ContextShift](config: ClickhouseConfig): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](config.threadPoolSize)
      be <- Blocker[F]
      xa <- HikariTransactor.newHikariTransactor[F](
              driverClassName = "ru.yandex.clickhouse.ClickHouseDriver",
              url = config.url,
              user = config.user,
              pass = config.pass,
              connectEC = ce,
              blocker = be
            )
    } yield xa
}
