package com.example

//#user-registry-actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.StatusCodes
import EloUtil._

final case class User(id: String, elo: Double)
//Represents a channel of Users
final case class Team(id: String, users: Seq[User])

object SampleEloActor {
  final case class ReportGame(winner: String, loser: String, teamId: String)
  final case object GetLeaderboard
  def props: Props = Props[SampleEloActor]
}

class SampleEloActor extends Actor with ActorLogging {
  import SampleEloActor._

  //Will be in a DB eventually
  var teams = Seq.empty[Team]

  /**
    * Initializes a team with the two users that played the match and saves it to teams
    * @param winner
    * @param loser
    * @param teamId
    */
  def initTeam(winner: String, loser: String, teamId: String) = {
    teams = teams :+ Team(teamId, updateUsers(winner, startingElo, loser, startingElo))
  }

  /**
    * Returns a new list of users with new elos calculated.
    * @param winner
    * @param winnerElo
    * @param loser
    * @param loserElo
    * @return
    */
  def updateUsers(winner: String, winnerElo: Double, loser: String, loserElo: Double) = {
    val newElos = calculateElo(winnerElo, loserElo)
    Seq(User(winner, newElos._1), User(loser, newElos._2))
  }

  /**
    * Updates teams with a new team containing updated users.
    * @param winner
    * @param loser
    * @param team
    * @return
    */
  def handleFoundTeam(winner: String, loser: String, team: Team) = {
    val foundUsers = team.users.filter(user => user.id == winner || user.id == loser)
    val teamsStripped = teams.filter(t => t.id != team.id)
    foundUsers.length match {
        //Both users were found
      case 2 => {
        var winnerIdx = 0
        var loserIdx = 1
        if(foundUsers(1).id == winner) {
          winnerIdx = 1
          loserIdx = 0
        }
        val winnerElo = foundUsers(winnerIdx).elo
        val loserElo = foundUsers(loserIdx).elo
        teams = teamsStripped :+ Team(team.id, team.users ++ updateUsers(winner, winnerElo, loser, loserElo))
        StatusCodes.OK
      }
        //One of the users is new
      case 1 => {
        var isWinner = foundUsers(0).id == winner
        if (isWinner) {
          teams = teamsStripped :+ Team(team.id, team.users ++ updateUsers(winner, foundUsers(0).elo, loser, startingElo))
        }
        else {
          teams = teamsStripped :+ Team(team.id, team.users ++ updateUsers(winner, startingElo, loser, foundUsers(0).elo))
        }
        StatusCodes.OK
      }
        //Both users are new
      case 0 => {
        teams = teamsStripped :+ Team(team.id, team.users ++ updateUsers(winner, startingElo, loser, startingElo))
        StatusCodes.OK
      }
      case _ => {
        log.info(s"Invalid amount of users found")
        StatusCodes.InternalServerError
      }
    }
  }

  def receive: Receive = {
    case ReportGame(winner, loser, teamId) =>
      //Find the team
      val foundTeam = teams.filter(team => team.id == teamId)
      foundTeam.length match {
        case 1 => {
          sender() ! handleFoundTeam(winner, loser, foundTeam(0))
        }
        case 0 => {
          initTeam(winner, loser, teamId)
          sender() ! StatusCodes.OK
        }
        case _ => {
          log.info(s"Invalid amount of teams found for teamId: ${teamId}")
          sender() ! StatusCodes.InternalServerError
        }
      }
  }
}
