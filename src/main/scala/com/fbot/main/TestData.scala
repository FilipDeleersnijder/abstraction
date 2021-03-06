package com.fbot.main

import breeze.linalg.{DenseMatrix, DenseVector}
import breeze.stats.distributions.{Gaussian, MultivariateGaussian}
import com.fbot.common.data.BigData
import com.fbot.common.fastcollections.ImmutableArray
import com.fbot.common.hyperspace.Tuple
import org.apache.spark.SparkContext

import scala.util.Random

/**
  *
  */
trait TestData {

  def data: BigData

}


case class RndDataXd(dim: Int, N: Int)(implicit sc: SparkContext) extends TestData {

  def data: BigData = {
    def randomDouble = {
      Random.nextDouble() * 1000d
    }

    val dataX = ImmutableArray.fill[Tuple](N)(Tuple.fill(dim)(randomDouble))
    val dataY = ImmutableArray.fill[Tuple](N)(Tuple.fill(dim)(randomDouble))

    // 0 - 1,000   of 1,000,000 = 10^6
    // volume total space = 2000^8 = 2^8 10^(3*8)   = 256   10^24  --> 10^6  points
    // volume unit cube   =  500^8 = 1/2^8 10^(3*8) = 1/256 10^24  --> 15.25 points
    BigData(dataX, dataY)
  }

}


case class FxDataXd(dim: Int, N: Int)(implicit sc: SparkContext) extends TestData {

  def data: BigData = {
    def randomDouble = {
      Random.nextDouble() * 1000d
    }

    def f(tuple: Tuple): Tuple = {
      Tuple(tuple.repr.map(x => if (x >= 500d) Random.nextDouble() * 1000d else 0d))
    }

    val dataX = ImmutableArray(Array.fill[Tuple](N)(Tuple(Array.fill(dim)(randomDouble))))
    val dataY = dataX.map(f)

    // 0 - 1,000   of 1,000,000 = 10^6
    // volume total space = 2000^8 = 2^8 10^(3*8)   = 256   10^24  --> 10^6  points
    // volume unit cube   =  500^8 = 1/2^8 10^(3*8) = 1/256 10^24  --> 15.25 points
    BigData(dataX, dataY)
  }

}

case class GaussianData2d(N: Int, rho: Double,
                          sigmaX: Double = 1d, sigmaY: Double = 1d,
                          muX: Double = 0d, muY: Double = 0d)(implicit sc: SparkContext) extends TestData {

  def data: BigData = {
    val mu = DenseVector(muX, muY)
    val sigma = DenseMatrix((sigmaX*sigmaX, rho*sigmaX*sigmaY),
                            (rho*sigmaX*sigmaY, sigmaY*sigmaY))
    val gaussian = MultivariateGaussian(mu, sigma)

    val sample = ImmutableArray.fill[Tuple](N)(Tuple(gaussian.draw().toArray))
    val dataX = sample.map(tuple => Tuple(tuple(0)))
    val dataY = sample.map(tuple => Tuple(tuple(1)))

    // 0 - 1,000   of 1,000,000 = 10^6
    // volume total space = 2000^8 = 2^8 10^(3*8)   = 256   10^24  --> 10^6  points
    // volume unit cube   =  500^8 = 1/2^8 10^(3*8) = 1/256 10^24  --> 15.25 points
    BigData(dataX, dataY, dataX, dataY)
  }

}


case class ConstGaussianData2d(N: Int, const: Double, sigma: Double = 1d, mu: Double = 0d)(implicit sc: SparkContext) extends TestData {

  def data: BigData = {
    val gaussian = Gaussian(mu, sigma)

    val sample = ImmutableArray.fill[Tuple](N)(Tuple(const, const))
    val dataX = sample.map(tuple => Tuple(tuple(0)))
    val dataY = sample.map(tuple => Tuple(tuple(1)))

    // 0 - 1,000   of 1,000,000 = 10^6
    // volume total space = 2000^8 = 2^8 10^(3*8)   = 256   10^24  --> 10^6  points
    // volume unit cube   =  500^8 = 1/2^8 10^(3*8) = 1/256 10^24  --> 15.25 points
    BigData(dataX, dataY)
  }

}

case class KraskovData(implicit sc: SparkContext) extends TestData {

  def data: BigData = {
    val dataX = ImmutableArray(39, 65, 101, 169, 171, 205, 232, 243, 258, 277, 302, 355, 381).map(Tuple(_))
    val dataY = ImmutableArray(358, 205, 126, 150, 350, 227, 390, 94, 268, 41, 328, 365, 119).map(Tuple(_))

    BigData(dataX, dataY)
  }

}

case class Data2d(implicit sc: SparkContext) extends TestData {

  def data: BigData = {
    //  5432101234567
    //
    //              4 6
    //  0     6   93  5
    //                4
    //                3
    //      3         2
    //  7     2   5   1
    //       01       0
    //                1
    //                2
    //     4          3
    //                4
    //  1     8   2   5
    //
    val data = ImmutableArray(
      Tuple(0, 0),
      Tuple(1, 0), Tuple(1, 1), Tuple(-1, 2), Tuple(-2, -3),
      Tuple(5, 1), Tuple(1, 5), Tuple(-5, 1), Tuple(1, -5),
      Tuple(5, 5), Tuple(-5, 5), Tuple(-5, -5), Tuple(5, -5),
      Tuple(6, 5), Tuple(7, 6))

    BigData(data, data)
  }
}