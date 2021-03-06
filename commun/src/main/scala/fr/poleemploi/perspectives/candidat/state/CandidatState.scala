package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.prospect.domain.ReferentielProspectCandidat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CandidatState {

  def inscrire(context: CandidatContext, command: InscrireCandidatCommand, referentielProspectCandidat: ReferentielProspectCandidat): Future[List[Event]] =
    Future(default(context, command))

  def connecter(context: CandidatContext, command: ConnecterCandidatCommand): List[Event] =
    default(context, command)

  def autologger(context: CandidatContext, command: AutologgerCandidatCommand): List[Event] =
    default(context, command)

  def modifierCriteresRecherche(context: CandidatContext, command: ModifierCriteresRechercheCommand): List[Event] =
    default(context, command)

  def modifierDisponibilites(context: CandidatContext, command: ModifierDisponibilitesCommand): List[Event] =
    default(context, command)

  def modifierProfil(context: CandidatContext, command: ModifierProfilCandidatCommand, localisationService: LocalisationService): Future[List[Event]] =
    Future(default(context, command))

  def ajouterMRSValidee(context: CandidatContext, command: AjouterMRSValideesCommand, referentielHabiletesMRS: ReferentielHabiletesMRS): Future[List[Event]] =
    Future(default(context, command))

  def declarerRepriseEmploiParConseiller(context: CandidatContext, command: DeclarerRepriseEmploiParConseillerCommand): List[Event] =
    default(context, command)

  private def default(context: CandidatContext, command: Command[Candidat]) =
    throw new IllegalStateException(s"Le candidat ${command.id.value} avec le statut ${context.statut.value} ne peut pas gérer la commande ${command.getClass.getSimpleName}")
}
