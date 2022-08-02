package com.github.nullptr7
package protocol

import sttp.tapir._
import sttp.tapir.server.ServerEndpoint

trait Contracts[F[_]] {

  final protected[protocol] type ServerEndpointF = ServerEndpoint[Any, F]

  protected[protocol] lazy val base =
    infallibleEndpoint
      .in("employees")
      .in(header[AuthMode]("X-AuthMode").default(MissingAuthMode))

  protected[protocol] val make: F[List[ServerEndpointF]]
}
