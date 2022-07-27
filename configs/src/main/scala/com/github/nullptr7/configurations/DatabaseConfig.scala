package com.github.nullptr7
package configurations

import types._

final case class DatabaseConfig(
  val host:     Hostname,
  val port:     Port,
  val user:     String,
  val database: String,
  val password: Sensitive
)
