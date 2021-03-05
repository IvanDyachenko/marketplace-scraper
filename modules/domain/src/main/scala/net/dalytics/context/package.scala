package net.dalytics

import tofu.env.Env

package object context {
  type AppF[+A] = Env[CommandContext, A]

//type HasCE[F[_]] = F WithContext ConcurrentEffect[F]
//type HasConfig[F[_]] = F WithContext Config
}
