package com.github.nullptr7
package client
package exception

sealed abstract class ClientException(msg: String) extends Exception {
  override def getMessage: String = msg
}

case class ServiceClientException(msg: String) extends ClientException(msg)
