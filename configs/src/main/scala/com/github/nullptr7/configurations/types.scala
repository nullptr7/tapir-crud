package com.github.nullptr7
package configurations

import scala.concurrent.duration.Duration

import java.net.URI

object types {

  final case class Sensitive(value: String) extends AnyVal {
    override def toString: String = "MASKED"
  }

  final case class Hostname(value: String) extends AnyVal
  final case class Port(value: Int) extends AnyVal

  final case class Namespace(value: String) extends AnyVal

  sealed trait ConfigType {
    val namespace: Namespace
  }

  object ConfigType {

    implicit object Server extends ConfigType {
      override val namespace: Namespace = Namespace("server")
    }

    implicit object Postgres extends ConfigType {
      override val namespace: Namespace = Namespace("db")
    }

    implicit object Client extends ConfigType {
      override val namespace: Namespace = Namespace("client")
    }

  }

  final case class ClientConfig(
    timeout:   Duration,
    transport: TransportApiClientDetails
  )

  final case class ServerConfig(host: Hostname, port: Port)

  sealed trait ClientDetails {
    val url:      URI
    val username: String
    val password: Sensitive
  }

  final case class TransportApiClientDetails(
    override val url:      URI,
    override val username: String,
    override val password: Sensitive
  ) extends ClientDetails

  final case class DatabaseConfig(
    host:     Hostname,
    port:     Port,
    user:     String,
    database: String,
    password: Sensitive
  )

}
