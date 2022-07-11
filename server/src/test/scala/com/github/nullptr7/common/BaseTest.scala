package com.github.nullptr7
package common

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Inside

trait BaseTest extends AsyncFlatSpec with Matchers with Inside
