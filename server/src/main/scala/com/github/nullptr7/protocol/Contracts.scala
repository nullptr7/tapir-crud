package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.server.ServerEndpoint

trait Contracts[F[_]] {

  protected[protocol] final type ServerEndpointF = ServerEndpoint[Any, F]

  protected[protocol] lazy val base =
    infallibleEndpoint
      .in("employees")

  protected[protocol] val make: F[List[ServerEndpointF]]
}
