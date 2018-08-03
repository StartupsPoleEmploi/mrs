package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.recruteur.TypeRecruteur
import fr.poleemploi.perspectives.domain.{Metier, SecteurActivite}

sealed trait CandidatQuery extends Query

case class GetCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class GetCVByCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class RechercherCandidatsParDateInscriptionQuery(typeRecruteur: TypeRecruteur) extends CandidatQuery

case class RechercheCandidatsParSecteurQuery(secteur: SecteurActivite,
                                             typeRecruteur: TypeRecruteur) extends CandidatQuery

case class RechercherCandidatsParMetierQuery(metiers: Set[Metier],
                                             typeRecruteur: TypeRecruteur) extends CandidatQuery