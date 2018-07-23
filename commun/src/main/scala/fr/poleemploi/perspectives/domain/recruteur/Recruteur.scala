package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}

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

    List(RecruteurInscrisEvent(
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = command.genre.code
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
        raisonSociale = command.raisonSociale,
        numeroSiret = command.numeroSiret.value,
        typeRecruteur = command.typeRecruteur.code,
        contactParCandidats = command.contactParCandidats,
        numeroTelephone = command.numeroTelephone.value
      ))
    } else Nil
  }

  def modifierProfilPEConnect(command: ModifierProfilPEConnectCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }

    if (!state.nom.contains(command.nom) ||
      !state.prenom.contains(command.prenom) ||
      !state.email.contains(command.email) ||
      !state.genre.contains(command.genre)) {
      List(ProfilRecruteurModifiePEConnectEvent(
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre.code
      ))
    } else Nil
  }
}

private[recruteur] case class RecruteurState(estInscrit: Boolean = false,
                                             nom: Option[String] = None,
                                             prenom: Option[String] = None,
                                             email: Option[String] = None,
                                             genre: Option[Genre] = None,
                                             raisonSociale: Option[String] = None,
                                             numeroSiret: Option[NumeroSiret] = None,
                                             typeRecruteur: Option[TypeRecruteur] = None,
                                             contactParCandidats: Option[Boolean] = None,
                                             numeroTelephone: Option[NumeroTelephone] = None) {

  def apply(event: Event): RecruteurState = event match {
    case e: RecruteurInscrisEvent =>
      copy(
        estInscrit = true,
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Genre.from(e.genre)
      )
    case e: ProfilModifieEvent =>
      copy(
        raisonSociale = Some(e.raisonSociale),
        numeroSiret = NumeroSiret.from(e.numeroSiret),
        typeRecruteur = TypeRecruteur.from(e.typeRecruteur),
        contactParCandidats = Some(e.contactParCandidats),
        numeroTelephone = NumeroTelephone.from(e.numeroTelephone)
      )
    case e: ProfilRecruteurModifiePEConnectEvent =>
      copy(
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Genre.from(e.genre)
      )
    case _ => this
  }
}