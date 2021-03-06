package com.fbot.common.fastcollections.math

import com.fbot.common.fastcollections.{FastTuple, ImmutableArray}

import scala.collection.mutable

/**
  *
  */
trait FastArrayLongMath[Self <: FastTuple[Long, Self]] extends Any with ElementWiseFastArrayOps[Long] with FastTuple[Long, Self] {

  def make(x: mutable.WrappedArray[Long]): Self

  private def make(x: Array[Long]): Self = {
    // avoid the match case, and go straight to Long
    make(new mutable.WrappedArray.ofLong(x).asInstanceOf[mutable.WrappedArray[Long]])
  }


  def +(rhs: Self): Self = {
    val res: Array[Long] = new Array[Long](this.length)
    make(elementWise(_ + _)(this.repr.toArray, rhs.repr.toArray)(res))
  }

  def -(rhs: Self): Self = {
    val res: Array[Long] = new Array[Long](this.length)
    make(elementWise(_ - _)(this.repr.toArray, rhs.repr.toArray)(res))
  }

  def *(rhs: Self): Self = {
    val res: Array[Long] = new Array[Long](this.length)
    make(elementWise(_ * _)(this.repr.toArray, rhs.repr.toArray)(res))
  }

  def unary_-(): Self = {
    make(this.repr.toArray.map(- _))
  }

}

object ArrayLongMath extends ElementWiseFastArrayOps[Long] {

  def plus(lhs: ImmutableArray[Long], rhs: ImmutableArray[Long]): ImmutableArray[Long] = {
    val res: Array[Long] = new Array[Long](lhs.length)
    ImmutableArray(elementWise(_ + _)(lhs.toArray, rhs.toArray)(res))
  }

  def minus(lhs: ImmutableArray[Long], rhs: ImmutableArray[Long]): ImmutableArray[Long] = {
    val res: Array[Long] = new Array[Long](lhs.length)
    ImmutableArray(elementWise(_ - _)(lhs.toArray, rhs.toArray)(res))
  }

  def times(lhs: ImmutableArray[Long], rhs: ImmutableArray[Long]): ImmutableArray[Long] = {
    val res: Array[Long] = new Array[Long](lhs.length)
    ImmutableArray(elementWise(_ * _)(lhs.toArray, rhs.toArray)(res))
  }

  def negate(lhs: ImmutableArray[Long]): ImmutableArray[Long] = {
    lhs.map(-_)
  }

}
