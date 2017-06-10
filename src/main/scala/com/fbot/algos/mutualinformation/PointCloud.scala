package com.fbot.algos.mutualinformation

import com.fbot.common.hyperspace.TupleOps._
import com.fbot.common.fastcollections.{FastMap, ImmutableArray}
import com.fbot.common.fastcollections.index.ArrayIndex
import com.fbot.common.fastcollections.math.FastArrayLongMath
import com.fbot.common.hyperspace.{HyperCube, HyperSpace, Tuple, UnitHyperCube}

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * Copyright (C) 5/30/2017 - REstore NV
  *
  *
  * note: use classes to render data type safe, do everything else functional
  */
case class PointCloud(points: ImmutableArray[Tuple], space: HyperSpace) {

  def kNearestBruteForce(pointIndices: ImmutableArray[ArrayIndex])(k: Int, currentTuple: Tuple): ImmutableArray[(ArrayIndex, Double)] = {
    val otherTuplesSortedByDistance = pointIndices
      .map(index => (index, distance(points(index), currentTuple)))
      .partialSort(k, (el1, el2) => el1._2 < el2._2)

    otherTuplesSortedByDistance
  }

  /*
   New algo:
   1. OK def tuple => hyperCubeBin & store in map
   2. OK groupBy(tuple => hyperCubeBin)
   3. around center-point initialize with cube = UnitHyperCube
   4. find all points .isIn(bigCube) and not already visited
      if (number of points in hyperCube > k) find brute force k-nearest on all points
   4. if distance from farthest of k-nearest to center-point < some edges of hyperCube (or number of points in hyperCube < k)
      extend smallHyperCube -> bigHyperCube in direction of edges which are too close and go back to 4.
   */

  lazy val binnedPoints: ImmutableArray[UnitHyperCube] = points.map(space.findEnclosingUnitHyperCube)

  lazy val pointsByBin: FastMap[UnitHyperCube, ImmutableArray[ArrayIndex]] = points.indexRange.unzippedGroupBy(binnedPoints(_))

  def kNearest(k: Int, currentTupleIndex: ArrayIndex): ImmutableArray[ArrayIndex] = {

    val currentTuple = points(currentTupleIndex)

    @tailrec
    def kNearestInCube(kNearestCandidates: ImmutableArray[ArrayIndex],
                       remainingPointsByBin: FastMap[UnitHyperCube, ImmutableArray[ArrayIndex]],
                       cube: HyperCube): ImmutableArray[(ArrayIndex, Double)] = {

      val newCandidateBins = remainingPointsByBin.filterKeys(_ isIn cube)  // time-expensive line

      val newCandidatePoints = if (kNearestCandidates.isEmpty) {
        newCandidateBins.values.flatten.filterNot(_ == currentTupleIndex)
      } else {
        kNearestCandidates ++ newCandidateBins.values.flatten
      }

      //println(f"$cube%60s: #candidates = ${kNearestCandidates.length }%3d + ${newCandidateBins.values.flatten.length }%3d")

      if (newCandidatePoints.length >= k) {
        val kNearestWithDistance = kNearestBruteForce(newCandidatePoints)(k, currentTuple)
        val epsilon = kNearestWithDistance.map(_._2).last

        val growLeft = space.axes.map(axis => if ((currentTuple(axis) - cube.left.repr(axis) * space.unitCubeSize(axis)) < epsilon) -1L else 0L)
        val growRight = space.axes.map(axis => if ((cube.right.repr(axis) * space.unitCubeSize(axis) - currentTuple(axis)) < epsilon) 1L else 0L)

        if (growLeft.forall(_ == 0L) && growRight.forall(_ == 0L)) {
          kNearestWithDistance
        } else {
          val newCube = cube.grow(growLeft, growRight)
          kNearestInCube(kNearestWithDistance.map(_._1), remainingPointsByBin.filterOut(newCandidateBins.filter), newCube)
        }

      } else {
        val newCube = cube.grow(Array.fill[Long](space.dim)(-1L), Array.fill[Long](space.dim)(1L))

        kNearestInCube(newCandidatePoints, remainingPointsByBin.filterOut(newCandidateBins.filter), newCube)
      }
    }

    // initialize
    val cube = space.unitCube(points(currentTupleIndex))
    val kNearestCandidates = ImmutableArray.empty[ArrayIndex]
    kNearestInCube(kNearestCandidates, pointsByBin, cube).map(_._1)
  }


}

object PointCloud {

  def print[T](x: Array[T]): String = x.deep.mkString("Array(", ",", ")")

}

