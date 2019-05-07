package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielHabiletesMRS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CandidatInscritState extends CandidatState {

  override def modifierCandidat(context: CandidatContext, command: ModifierCandidatCommand): List[Event] = {
    if (command.contactFormation && command.numeroTelephone.isEmpty) {
      throw new IllegalArgumentException("Le numéro de téléphone doit être renseigné lorsque le contactFormation est souhaité")
    }
    if (command.contactRecruteur && command.numeroTelephone.isEmpty) {
      throw new IllegalArgumentException("Le numéro de téléphone doit être renseigné lorsque le contactRecruteur est souhaité")
    }
    val codesROMEValides = context.mrsValidees.map(_.codeROME)
    if (command.codesROMEValidesRecherches.exists(c => !codesROMEValides.contains(c))) {
      throw new IllegalArgumentException("Un codeROME ne fait pas partie des codesROME validés par le candidat")
    }

    val visibiliteRecruteurModifieeEvent =
      if (!context.contactRecruteur.contains(command.contactRecruteur) ||
        !context.contactFormation.contains(command.contactFormation))
        Some(VisibiliteRecruteurModifieeEvent(
          candidatId = command.id,
          contactFormation = command.contactFormation,
          contactRecruteur = command.contactRecruteur,
        ))
      else None

    val numeroTelephoneModifieEvent = command.numeroTelephone.flatMap(n =>
      if (!context.numeroTelephone.contains(n)) {
        Some(NumeroTelephoneModifieEvent(
          candidatId = command.id,
          numeroTelephone = command.numeroTelephone.get
        ))
      } else None
    )

    val criteresRechercheModifiesEvent =
      if (!context.codesROMEValidesRecherches.forall(command.codesROMEValidesRecherches.contains) ||
        !context.codesROMERecherches.forall(command.codesROMERecherches.contains) ||
        !context.codesDomaineProfessionnelRecherches.forall(command.codesDomaineProfessionnelRecherches.contains) ||
        !context.localisationRecherche.contains(command.localisationRecherche)) {
        Some(CriteresRechercheModifiesEvent(
          candidatId = command.id,
          localisationRecherche = command.localisationRecherche,
          codesROMEValidesRecherches = command.codesROMEValidesRecherches,
          codesROMERecherches = command.codesROMERecherches,
          codesDomaineProfessionnelRecherches = command.codesDomaineProfessionnelRecherches
        ))
      } else None

    List(visibiliteRecruteurModifieeEvent, numeroTelephoneModifieEvent, criteresRechercheModifiesEvent).flatten
  }

  override def connecter(context: CandidatContext,
                         command: ConnecterCandidatCommand,
                         localisationService: LocalisationService): Future[List[Event]] = {
    val candidatConnecteEvent = Future.successful(Some(CandidatConnecteEvent(command.id)))

    val profilCandidatModifieEvent = Future.successful(
      if (!context.nom.contains(command.nom) ||
        !context.prenom.contains(command.prenom) ||
        !context.email.contains(command.email) ||
        !context.genre.contains(command.genre)) {
        Some(ProfilCandidatModifieEvent(
          candidatId = command.id,
          nom = command.nom,
          prenom = command.prenom,
          email = command.email,
          genre = command.genre
        ))
      } else None)

    val adresseModifieeEvent = command.adresse.map(adresse =>
      if (!context.adresse.contains(adresse)) {
        localisationService.localiser(adresse).map(optCoordonnees =>
          for {
            coordonnees <- optCoordonnees if !context.coordonnees.contains(coordonnees)
          } yield {
            AdresseModifieeEvent(
              candidatId = command.id,
              adresse = adresse,
              coordonnees = coordonnees
            )
          }
        ).recover {
          case _: Throwable => None
        }
      } else Future.successful(None)
    ).getOrElse(Future.successful(None))

    val statutDemandeurEmploiModifieEvent = Future.successful(command.statutDemandeurEmploi.flatMap(statutDemandeurEmploi =>
      if (!context.statutDemandeurEmploi.contains(statutDemandeurEmploi)) {
        Some(StatutDemandeurEmploiModifieEvent(
          candidatId = command.id,
          statutDemandeurEmploi = statutDemandeurEmploi
        ))
      } else None
    ))

    Future.sequence(List(candidatConnecteEvent, profilCandidatModifieEvent, adresseModifieeEvent, statutDemandeurEmploiModifieEvent))
      .map(_.flatten)
  }

  override def ajouterCV(context: CandidatContext, command: AjouterCVCommand, cvService: CVService): Future[List[Event]] = {
    if (context.cvId.isDefined) {
      return Future.failed(new IllegalArgumentException(s"Impossible d'ajouter un CV au candidat ${command.id.value}, il existe déjà"))
    }

    val cvId = cvService.nextIdentity
    cvService.save(
      cvId = cvId,
      candidatId = command.id,
      nomFichier = buildNomCV(context).getOrElse(throw new IllegalArgumentException("Erreur lors de la construction du nom du CV")),
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

  override def remplacerCV(context: CandidatContext, command: RemplacerCVCommand, cvService: CVService): Future[List[Event]] = {
    if (context.cvId.isEmpty) {
      return Future.failed(new IllegalArgumentException(s"Impossible de remplacer le CV inexistant du candidat ${command.id.value}"))
    }

    cvService.update(
      cvId = command.cvId,
      nomFichier = buildNomCV(context).getOrElse(throw new IllegalArgumentException("Erreur lors de la construction du nom du CV")),
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

  private def buildNomCV(context: CandidatContext): Option[String] =
    for {
      nom <- context.nom
      prenom <- context.prenom
    } yield s"${prenom.value} ${nom.value}"

  override def ajouterMRSValidee(context: CandidatContext,
                                 command: AjouterMRSValideesCommand,
                                 referentielHabiletesMRS: ReferentielHabiletesMRS): Future[List[Event]] = {
    val mrsSansDoublons = command.mrsValidees.foldLeft(List[MRSValidee]())((acc, mrsValidee) =>
      if (acc.exists(m => m.codeROME == mrsValidee.codeROME && m.codeDepartement == mrsValidee.codeDepartement))
        acc
      else
        mrsValidee :: acc
    )
    if (mrsSansDoublons.size != command.mrsValidees.size) {
      return Future.failed(new IllegalArgumentException(s"Impossible d'ajouter des MRS au candidat ${command.id.value} : la commande contient des MRS avec le même métier pour le même département"))
    }
    val mrsDejaValidees = context.mrsValidees.filter(m => command.mrsValidees.exists(c => c.codeROME == m.codeROME && c.codeDepartement == m.codeDepartement))
    if (mrsDejaValidees.nonEmpty) {
      return Future.failed(new IllegalArgumentException(
        s"Le candidat ${command.id.value} a déjà validé les métiers suivants : ${mrsDejaValidees.foldLeft("")((s, mrs) => s + '\n' + s"${mrs.codeROME.value} dans le département ${mrs.codeDepartement.value}")}"
      ))
    }

    Future.sequence(
      command.mrsValidees.map(m =>
        referentielHabiletesMRS.habiletes(m.codeROME).map(habiletes =>
          MRSAjouteeEvent(
            candidatId = command.id,
            codeROME = m.codeROME,
            departement = m.codeDepartement,
            habiletes = habiletes,
            dateEvaluation = m.dateEvaluation,
            isDHAE = m.isDHAE
          ))
      )
    )
  }

  override def declarerRepriseEmploiParConseiller(context: CandidatContext, command: DeclarerRepriseEmploiParConseillerCommand): List[Event] = {
    if (context.rechercheEmploi.contains(false)) {
      throw new IllegalArgumentException(s"Le candidat ${command.id.value} n'est pas en recherche d'emploi")
    }

    List(RepriseEmploiDeclareeParConseillerEvent(
      candidatId = command.id,
      conseillerId = command.conseillerId
    ))
  }
}
