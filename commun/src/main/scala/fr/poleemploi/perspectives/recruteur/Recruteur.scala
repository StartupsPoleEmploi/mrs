package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}

class Recruteur(override val id: RecruteurId,
                override val version: Int,
                events: List[Event]) extends Aggregate {

  override type Id = RecruteurId

  private val state: RecruteurState =
    events.foldLeft(RecruteurState())((s, e) => s.apply(e))

  def inscrire(command: InscrireRecruteurCommand): List[Event] = {
    if (state.estInscrit) {
      throw new RuntimeException(s"Le recruteur ${id.value} est déjà inscrit")
    }

    List(RecruteurInscritEvent(
      recruteurId = command.id,
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = command.genre
    ))
  }

  def modifierProfil(command: ModifierProfilCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }

    if (!state.raisonSociale.contains(command.raisonSociale) ||
      !state.numeroSiret.contains(command.numeroSiret) ||
      !state.typeRecruteur.contains(command.typeRecruteur) ||
      !state.contactParCandidats.contains(command.contactParCandidats) ||
      !state.numeroTelephone.contains(command.numeroTelephone)) {
      List(ProfilModifieEvent(
        recruteurId = command.id,
        raisonSociale = command.raisonSociale,
        numeroSiret = command.numeroSiret,
        typeRecruteur = command.typeRecruteur,
        contactParCandidats = command.contactParCandidats,
        numeroTelephone = command.numeroTelephone
      ))
    } else Nil
  }

  def modifierProfilGerant(command: ModifierProfilGerantCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }

    if (!state.nom.contains(command.nom) ||
      !state.prenom.contains(command.prenom) ||
      !state.email.contains(command.email) ||
      !state.genre.contains(command.genre)) {
      List(ProfilGerantModifieEvent(
        recruteurId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      ))
    } else Nil
  }
}

private[recruteur] case class RecruteurState(estInscrit: Boolean = false,
                                             nom: Option[String] = None,
                                             prenom: Option[String] = None,
                                             email: Option[Email] = None,
                                             genre: Option[Genre] = None,
                                             raisonSociale: Option[String] = None,
                                             numeroSiret: Option[NumeroSiret] = None,
                                             typeRecruteur: Option[TypeRecruteur] = None,
                                             contactParCandidats: Option[Boolean] = None,
                                             numeroTelephone: Option[NumeroTelephone] = None) {

  def apply(event: Event): RecruteurState = event match {
    case e: RecruteurInscritEvent =>
      copy(
        estInscrit = true,
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre)
      )
    case e: ProfilModifieEvent =>
      copy(
        raisonSociale = Some(e.raisonSociale),
        numeroSiret = Some(e.numeroSiret),
        typeRecruteur = Some(e.typeRecruteur),
        contactParCandidats = Some(e.contactParCandidats),
        numeroTelephone = Some(e.numeroTelephone)
      )
    case e: ProfilGerantModifieEvent =>
      copy(
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre)
      )
    case _ => this
  }
}
