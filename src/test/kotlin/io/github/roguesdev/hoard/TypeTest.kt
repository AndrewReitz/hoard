/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.roguesdev.hoard

import java.lang.reflect.Type
import java.util.ArrayList
import org.junit.Test

import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.junit.Assert.fail
import java.lang.reflect.ParameterizedType

class TypeTest {

  private lateinit var mapOfStringInteger: Map<String, Int>
  private lateinit var arrayListOfMapOfStringInteger: ArrayList<Map<String, Int>>

  @Test
  fun newParameterizedType() {
    // List<A>. List is a top-level class.
    var type: Type = newParameterizedType(List::class.java, A::class.java)
    (type as ParameterizedType).actualTypeArguments[0] shouldEqual A::class.java

    // A<B>. A is a static inner class.
    type = newParameterizedTypeWithOwner(TypeTest::class.java, A::class.java, B::class.java)
    type.actualTypeArguments[0] shouldEqual B::class.java
  }

  @Test
  fun parameterizedTypeWithRequiredOwnerMissing() {
    try {
      newParameterizedType(A::class.java, B::class.java)
      fail()
    } catch (expected: IllegalArgumentException) {
      expected.message shouldEqual "unexpected owner type for ${A::class.java}: null"
    }
  }

  @Test
  fun parameterizedTypeWithUnnecessaryOwnerProvided() {
    try {
      newParameterizedTypeWithOwner(A::class.java, List::class.java, B::class.java)
      fail()
    } catch (expected: IllegalArgumentException) {
      expected.message shouldEqual "unexpected owner type for ${List::class.java}: ${A::class.java}"
    }
  }

  @Test
  fun arrayOf() {
    arrayTypeOf(Int::class.javaPrimitiveType!!).toString() shouldEqual "int[]"
    arrayTypeOf(List::class.java).toString() shouldEqual "java.util.List[]"
    arrayTypeOf(Array<String>::class.java).toString() shouldEqual "java.lang.String[][]"
  }

  @Test
  fun subtypeOf() {
    subtypeOf(CharSequence::class.java).toString() shouldEqual "? extends java.lang.CharSequence"
  }

  @Test
  fun supertypeOf() {
    supertypeOf(String::class.java).toString() shouldEqual "? super java.lang.String"
  }

  @Test
  fun newParameterizedTypeObjectMethods() {
    val mapOfStringIntegerType = TypeTest::class.java.getDeclaredField("mapOfStringInteger").genericType
    val newMapType = newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
    newMapType shouldEqual mapOfStringIntegerType
    newMapType.hashCode() shouldEqual mapOfStringIntegerType.hashCode()
    newMapType.toString() shouldEqual mapOfStringIntegerType.toString()

    val arrayListOfMapOfStringIntegerType = TypeTest::class.java.getDeclaredField("arrayListOfMapOfStringInteger").genericType
    val newListType = newParameterizedType(ArrayList::class.java, newMapType)
    newListType shouldEqual arrayListOfMapOfStringIntegerType
    newListType.hashCode() shouldEqual arrayListOfMapOfStringIntegerType.hashCode()
    newListType.toString() shouldEqual arrayListOfMapOfStringIntegerType.toString()
  }

  private class A
  private class B

  @Test fun arrayEqualsGenericTypeArray() {
    equals(IntArray::class.java, arrayTypeOf(Int::class.javaPrimitiveType!!)).shouldBeTrue()
    equals(arrayTypeOf(Int::class.javaPrimitiveType!!), IntArray::class.java).shouldBeTrue()
    equals(Array<String>::class.java, arrayTypeOf(String::class.java)).shouldBeTrue()
    equals(arrayTypeOf(String::class.java), Array<String>::class.java).shouldBeTrue()
  }

  @Test
  fun parameterizedAndWildcardTypesCannotHavePrimitiveArguments() {
    try {
      newParameterizedType(List::class.java, Int::class.javaPrimitiveType!!)
      fail()
    } catch (expected: IllegalArgumentException) {
      expected.message shouldEqual "Unexpected primitive int. Use the boxed type."
    }

    try {
      subtypeOf(Byte::class.javaPrimitiveType!!)
      fail()
    } catch (expected: IllegalArgumentException) {
      expected.message shouldEqual "Unexpected primitive byte. Use the boxed type."
    }

    try {
      subtypeOf(Boolean::class.javaPrimitiveType!!)
      fail()
    } catch (expected: IllegalArgumentException) {
      expected.message shouldEqual "Unexpected primitive boolean. Use the boxed type."
    }
  }
}
