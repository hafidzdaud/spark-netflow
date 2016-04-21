/*
 * Copyright 2016 sadikovi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sadikovi.spark.netflow.sources

import java.util.{HashSet => JHashSet}
import com.github.sadikovi.testutil.UnitTestSpec

class StatisticsSuite extends UnitTestSpec {
  test("resolve statistics path without root") {
    val resolver = StatisticsPathResolver(None)
    val path = resolver.getStatisticsPath("file:/x/y/z/file")
    path should be ("file:/x/y/z/.statistics-file")
  }

  test("resolve statistics path with root") {
    val resolver = StatisticsPathResolver(Some("file:/a/b/c"))
    val path = resolver.getStatisticsPath("file:/x/y/z/file")
    path should be ("file:/a/b/c/x/y/z/.statistics-file")
  }

  test("fail if root path is null") {
    intercept[IllegalArgumentException] {
      StatisticsPathResolver(Some(null))
    }
  }

  test("fail if root path is empty") {
    intercept[IllegalArgumentException] {
      StatisticsPathResolver(Some(""))
    }
  }

  test("fail if file path is null") {
    val resolver = StatisticsPathResolver(None)
    intercept[IllegalArgumentException] {
      resolver.getStatisticsPath(null)
    }
  }

  test("fail if file path is empty") {
    val resolver = StatisticsPathResolver(None)
    intercept[IllegalArgumentException] {
      resolver.getStatisticsPath("")
    }
  }

  test("create and update attribute") {
    val attr = Attribute[Int]("a", _ < _, 7)
    for (i <- 1 to 3) {
      attr.addValue(i)
    }
    attr.getCount() should be (Some(3))
    attr.containsInRange(2) should be (Some(true))
    attr.containsInSet(2) should be (Some(true))
  }

  test("get value from attribute for range mode") {
    val attr = Attribute[Int]("a", _ < _, 2)
    attr.addValue(3)
    attr.addValue(5)
    attr.containsInRange(2) should be (Some(false))
    attr.containsInRange(3) should be (Some(true))
    attr.containsInRange(4) should be (Some(true))
    attr.containsInRange(5) should be (Some(true))
    attr.containsInRange(6) should be (Some(false))
  }

  test("get value from attribute for set mode") {
    val attr = Attribute[Int]("a", _ < _, 4)
    attr.addValue(3)
    attr.addValue(5)
    attr.containsInSet(2) should be (Some(false))
    attr.containsInSet(3) should be (Some(true))
    attr.containsInSet(4) should be (Some(false))
    attr.containsInSet(5) should be (Some(true))
    attr.containsInSet(6) should be (Some(false))
  }

  test("get value from attribute for count mode") {
    val attr = Attribute[Int]("a", _ < _, 1)
    attr.getCount() should be (Some(0))
    attr.addValue(3)
    attr.addValue(3)
    attr.getCount() should be (Some(2))
  }

  test("get value from attribute for incorrect mode") {
    val attr = Attribute[Int]("a", _ < _, 8)
    attr.getCount() should be (None)
    attr.containsInRange(1) should be (None)
    attr.containsInSet(1) should be (None)
  }

  test("update values of attribute directly") {
    val attr = Attribute[Int]("a", _ < _, 7)
    attr.setCount(10)
    attr.setMinMax(2, 5)
    val set = new JHashSet[Int]()
    set.add(2)
    set.add(5)
    attr.setSet(set)

    attr.getCount() should be (Some(10))
    attr.containsInRange(2) should be (Some(true))
    attr.containsInSet(2) should be (Some(true))
  }

  test("fail when updating attribute for unset mode") {
    val attr = Attribute[Int]("a", _ < _, 8)
    intercept[IllegalArgumentException] {
      attr.setCount(1)
    }

    intercept[IllegalArgumentException] {
      attr.setMinMax(1, 2)
    }

    intercept[IllegalArgumentException] {
      attr.setSet(new JHashSet[Int]())
    }
  }
}
