package fr.poleemploi.perspectives.projections.recruteur.infra.sql

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

case class RecruteurRecord(recruteurId: RecruteurId,
                           nom: String,
                           prenom: String,
                           email: Email,
                           genre: Genre,
                           typeRecruteur: Option[TypeRecruteur],
                           raisonSociale: Option[String],
                           numeroSiret: Option[NumeroSiret],
                           numeroTelephone: Option[NumeroTelephone],
                           contactParCandidats: Option[Boolean],
                           dateInscription: ZonedDateTime,
                           dateDerniereConnexion: ZonedDateTime)
