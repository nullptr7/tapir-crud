package com.github.nullptr7
package common

import org.scalatest.Inside
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AsyncFlatSpec with Matchers with Inside
