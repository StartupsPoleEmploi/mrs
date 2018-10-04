package fr.poleemploi.perspectives.candidat

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, CVService}
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Candidat(override val id: CandidatId,
               override val version: Int,
               events: List[Event]) extends Aggregate {

  override type Id = CandidatId

  private val state: CandidatState =
    events.foldLeft(CandidatState())((s, e) => s.apply(e))

  // DECIDE
  // TODO : Behavior pour éviter les trop gros aggrégats
  def inscrire(command: InscrireCandidatCommand): List[Event] = {
    if (state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} est déjà inscrit")
    }

    val candidatInscritEvent = Some(
      CandidatInscritEvent(
        candidatId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      )
    )
    val adresseModifieeEvent = command.adresse.map(adresse =>
      AdresseModifieeEvent(
        candidatId = command.id,
        adresse = adresse
      ))
    val statutDemandeurEmploiModifieEvent = command.statutDemandeurEmploi.map(statutDemandeurEmploi =>
      StatutDemandeurEmploiModifieEvent(
        candidatId = command.id,
        statutDemandeurEmploi = statutDemandeurEmploi
      )
    )

    List(candidatInscritEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent).flatten
  }

  // FIXME : vérification de l'existence des codes ROME dans le référentiel
  def modifierCriteres(command: ModifierCriteresRechercheCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    val criteresRechercheModifiesEvent =
      if (!state.rechercheMetierEvalue.contains(command.rechercheMetierEvalue) ||
        !state.rechercheAutreMetier.contains(command.rechercheAutreMetier) ||
        !state.metiersRecherches.forall(command.metiersRecherches.contains) ||
        !command.metiersRecherches.forall(state.metiersRecherches.contains) ||
        !state.etreContacteParAgenceInterim.contains(command.etreContacteParAgenceInterim) ||
        !state.etreContacteParOrganismeFormation.contains(command.etreContacteParOrganismeFormation) ||
        !state.rayonRecherche.contains(command.rayonRecherche)) {
        Some(CriteresRechercheModifiesEvent(
          candidatId = command.id,
          rechercheMetierEvalue = command.rechercheMetierEvalue,
          rechercheAutreMetier = command.rechercheAutreMetier,
          metiersRecherches = command.metiersRecherches,
          etreContacteParAgenceInterim = command.etreContacteParAgenceInterim,
          etreContacteParOrganismeFormation = command.etreContacteParOrganismeFormation,
          rayonRecherche = command.rayonRecherche
        ))
      } else None

    val numeroTelephoneModifieEvent =
      if (!state.numeroTelephone.contains(command.numeroTelephone)) {
        Some(NumeroTelephoneModifieEvent(
          candidatId = command.id,
          numeroTelephone = command.numeroTelephone
        ))
      } else None

    List(criteresRechercheModifiesEvent, numeroTelephoneModifieEvent).flatten
  }

  def connecter(command: ConnecterCandidatCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }

    val candidatConnecteEvent = Some(CandidatConnecteEvent(command.id))

    val profilCandidatModifieEvent =
      if (!state.nom.contains(command.nom) ||
        !state.prenom.contains(command.prenom) ||
        !state.email.contains(command.email) ||
        !state.genre.contains(command.genre)) {
        Some(ProfilCandidatModifieEvent(
          candidatId = command.id,
          nom = command.nom,
          prenom = command.prenom,
          email = command.email,
          genre = command.genre
        ))
      } else None

    val adresseModifieeEvent = command.adresse.flatMap(adresse =>
      if (command.adresse.isDefined && !state.adresse.contains(command.adresse.get)) {
        Some(AdresseModifieeEvent(
          candidatId = command.id,
          adresse = command.adresse.get
        ))
      } else None
    )

    val statutDemandeurEmploiModifieEvent = command.statutDemandeurEmploi.flatMap(statutDemandeurEmploi =>
      if (!state.statutDemandeurEmploi.contains(statutDemandeurEmploi)) {
        Some(StatutDemandeurEmploiModifieEvent(
          candidatId = command.id,
          statutDemandeurEmploi = statutDemandeurEmploi
        ))
      } else None
    )

    List(candidatConnecteEvent, profilCandidatModifieEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent).flatten
  }

  def ajouterCV(command: AjouterCVCommand,
                cvService: CVService): Future[List[Event]] = {
    if (!state.estInscrit) {
      return Future.failed(new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit"))
    }
    if (state.cvId.isDefined) {
      return Future.failed(new RuntimeException(s"Impossible d'ajouter un CV au candidat ${id.value}, il existe déjà"))
    }

    val cvId = cvService.nextIdentity
    cvService.save(
      cvId = cvId,
      candidatId = command.id,
      nomFichier = command.nomFichier,
      typeMedia = command.typeMedia,
      path = command.path
    ).map(_ => List(
      CVAjouteEvent(
        candidatId = command.id,
        cvId = cvId,
        typeMedia = command.typeMedia
      )
    ))
  }

  def remplacerCV(command: RemplacerCVCommand,
                  cvService: CVService): Future[List[Event]] = {
    if (!state.estInscrit) {
      return Future.failed(new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit"))
    } else if (state.cvId.isEmpty) {
      return Future.failed(new RuntimeException(s"Impossible de remplacer le CV inexistant du candidat ${id.value}"))
    }

    cvService.update(
      cvId = command.cvId,
      nomFichier = command.nomFichier,
      typeMedia = command.typeMedia,
      path = command.path
    ).map(_ => List(
      CVRemplaceEvent(
        candidatId = command.id,
        cvId = command.cvId,
        typeMedia = command.typeMedia
      )
    ))
  }

  def ajouterMRSValidee(command: AjouterMRSValideesCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }
    val mrsDejaValidees = state.mrsValidees.intersect(command.mrsValidees)
    if (mrsDejaValidees.nonEmpty) {
      throw new RuntimeException(
        s"Le candidat ${id.value} a déjà validé les MRS suivantes : ${mrsDejaValidees.foldLeft("")((s, mrs) => s + '\n' + s"${mrs.codeROME} le ${mrs.dateEvaluation}")}"
      )
    }

    command.mrsValidees.map(m => MRSAjouteeEvent(
      candidatId = command.id,
      metier = m.codeROME,
      dateEvaluation = m.dateEvaluation
    ))
  }

  def declarerRepriseEmploiParConseiller(command: DeclarerRepriseEmploiParConseillerCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas encore inscrit")
    }
    if (state.rechercheEmploi.contains(false)) {
      throw new RuntimeException(s"Le candidat ${id.value} n'est pas en recherche d'emploi")
    }

    List(RepriseEmploiDeclareeParConseillerEvent(
      candidatId = command.id,
      conseillerId = command.conseillerId
    ))
  }
}

// APPLY
private[candidat] case class CandidatState(estInscrit: Boolean = false,
                                           nom: Option[String] = None,
                                           prenom: Option[String] = None,
                                           email: Option[Email] = None,
                                           genre: Option[Genre] = None,
                                           adresse: Option[Adresse] = None,
                                           statutDemandeurEmploi: Option[StatutDemandeurEmploi] = None,
                                           rechercheMetierEvalue: Option[Boolean] = None,
                                           rechercheAutreMetier: Option[Boolean] = None,
                                           mrsValidees: List[MRSValidee] = Nil,
                                           metiersRecherches: Set[CodeROME] = Set.empty,
                                           etreContacteParAgenceInterim: Option[Boolean] = None,
                                           etreContacteParOrganismeFormation: Option[Boolean] = None,
                                           rayonRecherche: Option[RayonRecherche] = None,
                                           numeroTelephone: Option[NumeroTelephone] = None,
                                           cvId: Option[CVId] = None,
                                           rechercheEmploi: Option[Boolean] = None) {

  def apply(event: Event): CandidatState = event match {
    case e: CandidatInscritEvent =>
      copy(
        estInscrit = true,
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre),
        // Par défaut un candidat qui s'inscrit est disponible tant qu'il n'a pas renseigné ces critères, pour qu'on puisse déclarer sa reprise d'emploi s'il a été suivi manuellement
        rechercheEmploi = Some(true)
      )
    case e: ProfilCandidatModifieEvent =>
      copy(
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre)
      )
    case e: CriteresRechercheModifiesEvent =>
      copy(
        rechercheMetierEvalue = Some(e.rechercheMetierEvalue),
        rechercheAutreMetier = Some(e.rechercheAutreMetier),
        metiersRecherches = e.metiersRecherches,
        etreContacteParAgenceInterim = Some(e.etreContacteParAgenceInterim),
        etreContacteParOrganismeFormation = Some(e.etreContacteParOrganismeFormation),
        rayonRecherche = Some(e.rayonRecherche),
        rechercheEmploi = Some(e.rechercheMetierEvalue || e.rechercheAutreMetier)
      )
    case e: NumeroTelephoneModifieEvent =>
      copy(numeroTelephone = Some(e.numeroTelephone))
    case e: AdresseModifieeEvent =>
      copy(adresse = Some(e.adresse))
    case e: StatutDemandeurEmploiModifieEvent =>
      copy(statutDemandeurEmploi = Some(e.statutDemandeurEmploi))
    case e: CVAjouteEvent =>
      copy(cvId = Some(e.cvId))
    case e: CVRemplaceEvent =>
      copy(cvId = Some(e.cvId))
    case e: MRSAjouteeEvent =>
      copy(
        mrsValidees = MRSValidee(
          codeROME = e.metier,
          dateEvaluation = e.dateEvaluation) :: this.mrsValidees
      )
    case e: RepriseEmploiDeclareeParConseillerEvent =>
      copy(rechercheEmploi = Some(false))
    case _ => this
  }
}
