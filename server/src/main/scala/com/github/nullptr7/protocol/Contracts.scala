package com.github.nullptr7
package protocol

import sttp.tapir.server.ServerEndpoint
import sttp.tapir._

trait Contracts[F[_]] {

  protected[protocol] lazy val base =
    infallibleEndpoint
      .in("employees")

  protected[protocol] val make: List[ServerEndpoint[Any, F]]
}
