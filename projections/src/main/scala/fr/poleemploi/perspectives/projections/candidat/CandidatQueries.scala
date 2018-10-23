package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

sealed trait CandidatQuery extends Query

case class CandidatSaisieCriteresRechercheQuery(candidatId: CandidatId) extends CandidatQuery

case class CVCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class CVCandidatPourRecruteurQuery(candidatId: CandidatId,
                                        recruteurId: RecruteurId) extends CandidatQuery

case class CandidatsPourConseillerQuery(nbCandidatsParPage: Int,
                                        nbPagesACharger: Int,
                                        avantDateInscription: ZonedDateTime) extends CandidatQuery

case class CandidatsPourConseillerQueryResult(candidats: List[CandidatPourConseillerDto],
                                              pages: List[ZonedDateTime],
                                              derniereDateInscription: Option[ZonedDateTime])

case class RechercherCandidatsParDepartementQuery(typeRecruteur: TypeRecruteur,
                                                  codeDepartement: CodeDepartement) extends CandidatQuery

case class RechercherCandidatsParSecteurQuery(typeRecruteur: TypeRecruteur,
                                              codeSecteurActivite: CodeSecteurActivite,
                                              codeDepartement: Option[CodeDepartement]) extends CandidatQuery

case class RechercherCandidatsParMetierQuery(typeRecruteur: TypeRecruteur,
                                             codeROME: CodeROME,
                                             codeDepartement: Option[CodeDepartement]) extends CandidatQuery