package com.github.nullptr7
package protocol

import cats.effect.IO

trait ServiceLogicTestHelper{

  protected lazy val serviceLogic: ServiceLogic[IO] = new ServiceLogic[IO]

}
