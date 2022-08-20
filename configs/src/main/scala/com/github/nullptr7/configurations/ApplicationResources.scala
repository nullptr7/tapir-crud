package com.github.nullptr7
package configurations

import types.{ClientConfig, DatabaseConfig, ServerConfig}

final case class ApplicationResources(
  databaseConfig: DatabaseConfig,
  serverConfig:   ServerConfig,
  clientConfig:   ClientConfig
)
