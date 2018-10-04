package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.{CandidatId, _}
import fr.poleemploi.perspectives.projections.candidat.infra.sql._

import scala.concurrent.Future

class CandidatProjection(adapter: CandidatProjectionSqlAdapter) extends Projection {

  override def listenTo: List[Class[_ <: Event]] = List(classOf[CandidatEvent])

  override def isReplayable: Boolean = true

  override def onEvent: ReceiveEvent = {
    case e: CandidatInscritEvent => adapter.onCandidatInscritEvent(e)
    case e: CandidatConnecteEvent => adapter.onCandidatConnecteEvent(e)
    case e: ProfilCandidatModifieEvent => adapter.onProfilModifieEvent(e)
    case e: CriteresRechercheModifiesEvent => adapter.onCriteresRechercheModifiesEvent(e)
    case e: NumeroTelephoneModifieEvent => adapter.onNumeroTelephoneModifieEvent(e)
    case e: AdresseModifieeEvent => adapter.onAdresseModifieeEvent(e)
    case e: StatutDemandeurEmploiModifieEvent => adapter.onStatutDemandeurEmploiModifieEvent(e)
    case e: CVAjouteEvent => adapter.onCVAjouteEvent(e)
    case e: CVRemplaceEvent => adapter.onCVRemplaceEvent(e)
    case e: MRSAjouteeEvent => adapter.onMRSAjouteeEvent(e)
    case e: RepriseEmploiDeclareeParConseillerEvent => adapter.onRepriseEmploiDeclareeParConseillerEvent(e)
  }

  def criteresRecherche(query: CriteresRechercheQuery): Future[CandidatCriteresRechercheDto] =
    adapter.criteresRecherche(query)

  def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurDto] =
    adapter.candidatContactRecruteur(candidatId)

  def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult] =
    adapter.listerPourConseiller(query)

  def rechercherCandidatParDateInscription(query: RechercherCandidatsParDateInscriptionQuery): Future[ResultatRechercheCandidatParDateInscription] =
    adapter.rechercherCandidatParDateInscription(query)

  def rechercherCandidatParSecteur(query: RechercherCandidatsParSecteurQuery): Future[ResultatRechercheCandidatParSecteur] =
    adapter.rechercherCandidatParSecteur(query)

  def rechercherCandidatParMetier(query: RechercherCandidatsParMetierQuery): Future[ResultatRechercheCandidatParMetier] =
    adapter.rechercherCandidatParMetier(query)
}
