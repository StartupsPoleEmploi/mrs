package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.projections.metier.MetierDTO
import play.api.libs.json.{Json, Writes}

case class CandidatMetiersValidesQuery(candidatId: CandidatId) extends Query[CandidatMetiersValidesQueryResult]

case class CandidatMetiersValidesQueryResult(metiersValides: Set[MetierDTO]) extends QueryResult

object CandidatMetiersValidesQueryResult {

  implicit val writes: Writes[CandidatMetiersValidesQueryResult] = Json.writes[CandidatMetiersValidesQueryResult]
}
