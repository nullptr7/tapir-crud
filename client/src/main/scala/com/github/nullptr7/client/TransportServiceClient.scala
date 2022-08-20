package com.github.nullptr7
package client

import org.http4s.client.Client

import configurations.types.TransportApiClientDetails

final class TransportServiceClient[F[_]](override val client: Client[F], override val clientDetails: TransportApiClientDetails)
  extends ServiceClient[F]
