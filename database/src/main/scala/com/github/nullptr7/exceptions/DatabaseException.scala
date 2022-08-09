package com.github.nullptr7
package exceptions

case class DatabaseException(t: Throwable) extends Exception {

  override def getCause():   Throwable = t
  override def getMessage(): String    = "Exception occured while connecting to database. Please check the logs..."
}
