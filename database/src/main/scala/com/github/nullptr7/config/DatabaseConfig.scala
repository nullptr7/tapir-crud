package com.github.nullptr7
package config

final case class DatabaseConfig(
  val host:     String,
  val port:     Int,
  val user:     String,
  val database: String,
  val password: String
)