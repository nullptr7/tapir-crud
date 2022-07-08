package com.github.nullptr7
package protocol

import cats.effect.IO

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

trait ServiceLogicTestHelper extends AsyncFlatSpec with Matchers {

  protected lazy val serviceLogic: ServiceLogic[IO] = new ServiceLogic[IO]

}
