package com.github.nullptr7
package optics

import java.util.UUID

import monocle.Iso

trait IsUUID[A] {
  def _uuid: Iso[UUID, A]
}

object IsUUID {

  def apply[A: IsUUID]: IsUUID[A] = implicitly

}
