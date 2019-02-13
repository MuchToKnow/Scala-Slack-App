package com.example

object EloUtil {
  final val eloScale = 40
  final val startingElo = 1200

  /**
    * Calculates new elo of winner and loser, returning a tuple of the new Elos.
    * @param winner
    * @param loser
    * @returns (Double, Double)
    */
  def calculateElo(winner: Double, loser: Double) = {
    //Estimated probability that the winner would win
    val pWinner = 1/(1 + scala.math.pow(10, ((loser - winner)/400)))
    //Same for the loser
    val pLoser = 1 - pWinner

    val winnerNewElo = winner + (eloScale * (1 - pWinner))
    val loserNewElo = loser + (eloScale * (0 - pLoser))
    (winnerNewElo, loserNewElo)
  }
}
