package com.github.nullptr7
package exceptions

import sttp.model.StatusCode._

sealed trait ServiceException {
  val statusCode: Int
  val msg:        String
}

final object InvalidAuthMode extends ServiceException {
  override val statusCode: Int    = BadRequest.code
  override val msg:        String = "Authentication mode is invalid"

}

final object UnauthorizedAuth extends ServiceException {
  override val statusCode: Int    = Unauthorized.code
  override val msg:        String = "User is not authorized"

}
