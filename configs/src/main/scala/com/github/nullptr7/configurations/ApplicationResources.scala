package com.github.nullptr7
package configurations

import types.{DatabaseConfig, ServerConfig}

final case class ApplicationResources(
  databaseConfig: DatabaseConfig,
  serverConfig:   ServerConfig
)
