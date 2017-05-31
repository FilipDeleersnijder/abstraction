package com.fbot.algos.mutualinformation


/**
  * Copyright (C) 5/31/2017 - REstore NV
  *
  */
object Index {

  trait ExtraImplicits {
    /** These implicits create conversions from a value for which an implicit Numeric
      *  exists to the inner class which creates infix operations.  Once imported, you
      *  can write methods as follows:
      *  {{{
      *  def plus[T: Index](x: T, y: T) = x + y
      *  }}}
      */
    implicit def infixIndexOps[T](x: T)(implicit ind: Index[T]): Index[T]#Ops = new ind.Ops(x)
  }
  object Implicits extends ExtraImplicits { }


}


trait Index[T] extends Ordering[T] {
  def plus(i: T, j: T): T
  def minus(i: T, j: T): T
  def times(i: T, j: T): T
  def fromInt(i: Int): T

  class Ops(lhs: T) {
    def +(rhs: T): T = plus(lhs, rhs)
    def -(rhs: T): T = minus(lhs, rhs)
    def *(rhs: T): T = times(lhs, rhs)
  }
  implicit def mkIndexOps(lhs: T): Ops = new Ops(lhs)
}

