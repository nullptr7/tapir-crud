package com.github.nullptr7
package client

case class ApiClients[F[_]](
  transportServiceClient: TransportServiceClient[F]
)
