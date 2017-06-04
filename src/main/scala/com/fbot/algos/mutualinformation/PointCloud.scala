package com.fbot.algos.mutualinformation

import TupleOps._
import com.fbot.common.immutable.{ArrayIndex, ImmutableArray}

/**
  * Copyright (C) 5/30/2017 - REstore NV
  *
  *
  * note: use classes to render data type safe, do everything else functional
  */
case class PointCloud(points: ImmutableArray[Tuple], space: HyperSpace) {

  def nearestBruteForce(k: Int, currentTupleIndex: ArrayIndex): (Int, ImmutableArray[ArrayIndex]) = {
    val currentTuple = points(currentTupleIndex)
    val otherTuplesSortedByDistance = points.indexRange
      .filterNot(_ == currentTupleIndex)
      .map(index => (index, distance(points(index), currentTuple)))
      .sortBy(_._2)


    var i = k
    while (i < otherTuplesSortedByDistance.length && otherTuplesSortedByDistance(ArrayIndex(k - 1))._2 ==  otherTuplesSortedByDistance(ArrayIndex(i))._2) {
      i += 1
    }

    (i, otherTuplesSortedByDistance.take(k).map(_._1))
  }

  /*
   New algo:
   1. OK def tuple => hyperCubeBin & store in map
   2. OK groupBy(tuple => hyperCubeBin)
   3. around center-point initialize with bigCube = UnitHyperCube, smallCube = noCube
   4. find all points .isIn(bigCube) and .isNotIn(smallCube)
      if (number of points in hyperCube > k) find brute force k-nearest on all points
   4. if distance from farthest of k-nearest to center-point < some edges of hyperCube (or number of points in hyperCube < k)
      extend smallHyperCube -> bigHyperCube in direction of edges which are too close and go back to 4.
   */

  lazy val binnedPoints: ImmutableArray[UnitHyperCube] = points.map(space.findEnclosingUnitHyperCube)
  def enclosingBin(tupleIndex: ArrayIndex): UnitHyperCube = binnedPoints(tupleIndex)

  val pointsByBin: Map[UnitHyperCube, ImmutableArray[ArrayIndex]] = points.indexRange.groupBy(enclosingBin)

  def kNearestBinned(k: Int, currentTupleIndex: ArrayIndex): Array[ArrayIndex] = {

    ???
  }

}

object PointCloud {

  def print[T](x: Array[T]): String = x.deep.mkString("Array(", ",", ")")

}
