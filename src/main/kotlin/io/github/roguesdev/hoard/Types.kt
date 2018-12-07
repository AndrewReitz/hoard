/*
 * Copyright (C) 2008 Google Inc.
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

/**
 * Factory methods for types.
 *
 * @since 1.0.0
 */
@file:JvmName("Types")
@file:Suppress("unused")

package io.github.roguesdev.hoard

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.Arrays

private val EMPTY_TYPE_ARRAY = arrayOf<Type>()

/**
 * Returns a new parameterized type, applying `typeArguments` to `rawType`. Use this
 * method if `rawType` is not enclosed in another type.
 */
fun newParameterizedType(rawType: Type, vararg typeArguments: Type): ParameterizedType {
  return ParameterizedTypeImpl(null, rawType, *typeArguments)
}

/**
 * Returns a new parameterized type, applying `typeArguments` to `rawType`. Use this
 * method if `rawType` is enclosed in `ownerType`.
 */
fun newParameterizedTypeWithOwner(
  ownerType: Type,
  rawType: Type,
  vararg typeArguments: Type
): ParameterizedType {
  return ParameterizedTypeImpl(ownerType, rawType, *typeArguments)
}

/** Returns an array type whose elements are all instances of `componentType`.  */
fun arrayOf(componentType: Type): GenericArrayType {
  return GenericArrayTypeImpl(componentType)
}

/**
 * Returns a type that represents an unknown type that extends `bound`. For example, if
 * `bound` is `CharSequence.class`, this returns `? extends CharSequence`. If
 * `bound` is `Object.class`, this returns `?`, which is shorthand for `? extends Object`.
 */
fun subtypeOf(bound: Type): WildcardType {
  return WildcardTypeImpl(arrayOf<Type>(bound), EMPTY_TYPE_ARRAY)
}

/**
 * Returns a type that represents an unknown supertype of `bound`. For example, if `bound` is `String.class`, this returns `? super String`.
 */
fun supertypeOf(bound: Type): WildcardType {
  return WildcardTypeImpl(arrayOf<Type>(Any::class.java), arrayOf<Type>(bound))
}

/**
 * Returns a type that is functionally equal but not necessarily equal according to [ ][Object.equals].
 */
private fun canonicalize(type: Type): Type {
  return if (type is Class<*>) {
    if (type.isArray) GenericArrayTypeImpl(canonicalize(type.componentType)) else type
  } else if (type is ParameterizedType) {
    type as? ParameterizedTypeImpl ?: ParameterizedTypeImpl(type.ownerType,
      type.rawType, *type.actualTypeArguments)
  } else if (type is GenericArrayType) {
    type as? GenericArrayTypeImpl ?: GenericArrayTypeImpl(type.genericComponentType)
  } else if (type is WildcardType) {
    type as? WildcardTypeImpl ?: WildcardTypeImpl(type.upperBounds, type.lowerBounds)
  } else {
    type // This type is unsupported!
  }
}

private fun equal(a: Any?, b: Any): Boolean {
  @Suppress("SuspiciousEqualsCombination")
  return a === b || a != null && a == b
}

/** Returns true if `a` and `b` are equal.  */
private fun equals(a: Type, b: Type): Boolean {
  when {
    a === b -> return true // Also handles (a == null && b == null).
    a is Class<*> -> return a == b // Class already specifies equals().
    a is ParameterizedType -> {
      if (b !is ParameterizedType) return false
      val aTypeArguments = (a as? ParameterizedTypeImpl)?.typeArguments ?: a.actualTypeArguments
      val bTypeArguments = (b as? ParameterizedTypeImpl)?.typeArguments ?: b.actualTypeArguments
      return (equal(a.ownerType, b.ownerType) &&
        a.rawType == b.rawType &&
        Arrays.equals(aTypeArguments, bTypeArguments))
    }
    else -> return if (a is GenericArrayType) {
      if (b !is GenericArrayType) false else equals(a.genericComponentType, b.genericComponentType)
    } else if (a is WildcardType) {
      if (b !is WildcardType) false else Arrays.equals(a.upperBounds, b.upperBounds) && Arrays.equals(a.lowerBounds, b.lowerBounds)
    } else if (a is TypeVariable<*>) {
      if (b !is TypeVariable<*>) false else a.genericDeclaration === b.genericDeclaration && a.name == b.name
    } else {
      // This isn't a supported type.
      false
    }
  }
}

private fun hashCodeOrZero(o: Any?): Int {
  return o?.hashCode() ?: 0
}

private fun typeToString(type: Type): String {
  return if (type is Class<*>) type.name else type.toString()
}

private fun checkNotPrimitive(type: Type) {
  if (type is Class<*> && type.isPrimitive) {
    throw IllegalArgumentException()
  }
}

private class ParameterizedTypeImpl internal constructor(ownerType: Type?, rawType: Type, vararg typeArguments: Type) : ParameterizedType {
  private val ownerType: Type?
  private val rawType: Type
  internal val typeArguments: Array<Type>

  init {
    // Require an owner type if the raw type needs it.
    if (rawType is Class<*> && ownerType == null != (rawType.enclosingClass == null)) {
      throw IllegalArgumentException(
        "unexpected owner type for $rawType: $ownerType")
    }

    this.ownerType = if (ownerType == null) null else canonicalize(ownerType)
    this.rawType = canonicalize(rawType)
    this.typeArguments = arrayOf(*typeArguments)
    for (t in this.typeArguments.indices) {
      checkNotPrimitive(this.typeArguments[t])
      this.typeArguments[t] = canonicalize(this.typeArguments[t])
    }
  }

  override fun getActualTypeArguments(): Array<Type> {
    return typeArguments.clone()
  }

  override fun getRawType(): Type {
    return rawType
  }

  override fun getOwnerType(): Type? {
    return ownerType
  }

  override fun equals(other: Any?): Boolean {
    return other is ParameterizedType && equals(this, other)
  }

  override fun hashCode(): Int {
    return (Arrays.hashCode(typeArguments)
      xor rawType.hashCode()
      xor hashCodeOrZero(ownerType))
  }

  override fun toString(): String {
    val result = StringBuilder(30 * (typeArguments.size + 1))
    result.append(typeToString(rawType))

    if (typeArguments.isEmpty()) {
      return result.toString()
    }

    result.append("<").append(typeToString(typeArguments[0]))
    for (i in 1 until typeArguments.size) {
      result.append(", ").append(typeToString(typeArguments[i]))
    }
    return result.append(">").toString()
  }
}

private class GenericArrayTypeImpl(componentType: Type) : GenericArrayType {
  private val componentType: Type = canonicalize(componentType)

  override fun getGenericComponentType(): Type {
    return componentType
  }

  override fun equals(other: Any?): Boolean {
    return other is GenericArrayType && equals(this, other)
  }

  override fun hashCode(): Int {
    return componentType.hashCode()
  }

  override fun toString(): String {
    return typeToString(componentType) + "[]"
  }
}

/**
 * The WildcardType interface supports multiple upper bounds and multiple lower bounds. We only
 * support what the Java 6 language needs - at most one bound. If a lower bound is set, the upper
 * bound must be Object.class.
 */
private class WildcardTypeImpl(upperBounds: Array<Type>, lowerBounds: Array<Type>) : WildcardType {
  private val upperBound: Type
  private val lowerBound: Type?

  init {
    if (lowerBounds.size > 1) throw IllegalArgumentException()
    if (upperBounds.size != 1) throw IllegalArgumentException()

    if (lowerBounds.size == 1) {
      checkNotPrimitive(lowerBounds[0])
      if (upperBounds[0] !== Any::class.java) throw IllegalArgumentException()
      this.lowerBound = canonicalize(lowerBounds[0])
      this.upperBound = Any::class.java
    } else {
      checkNotPrimitive(upperBounds[0])
      this.lowerBound = null
      this.upperBound = canonicalize(upperBounds[0])
    }
  }

  override fun getUpperBounds(): Array<Type> {
    return arrayOf<Type>(upperBound)
  }

  override fun getLowerBounds(): Array<Type> {
    return if (lowerBound != null) arrayOf<Type>(lowerBound) else EMPTY_TYPE_ARRAY
  }

  override fun equals(other: Any?): Boolean {
    return other is WildcardType && equals(this, other)
  }

  override fun hashCode(): Int {
    // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
    return (if (lowerBound != null) 31 + lowerBound.hashCode() else 1) xor 31 + upperBound.hashCode()
  }

  override fun toString(): String = when {
      lowerBound != null -> "? super " + typeToString(lowerBound)
      upperBound === Any::class.java -> "?"
      else -> "? extends " + typeToString(upperBound)
  }
}
