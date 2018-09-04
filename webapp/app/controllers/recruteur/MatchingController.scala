package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.metier.MetierQueryHandler
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur.RecruteurId
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.mvc.{Action, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(cc: ControllerComponents,
                                   implicit val webAppConfig: WebAppConfig,
                                   messagesAction: MessagesActionBuilder,
                                   candidatQueryHandler: CandidatQueryHandler,
                                   recruteurQueryHandler: RecruteurQueryHandler,
                                   metierQueryHandler: MetierQueryHandler,
                                   recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val matchingForm = MatchingForm(
        secteurActivite = None,
        metier = None
      )
      rechercher(matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map(resultatRechercheCandidatDto =>
        Ok(views.html.recruteur.matching(
          matchingForm = MatchingForm.form.fill(matchingForm),
          recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
          resultatRechercheCandidat = resultatRechercheCandidatDto,
          secteursActivites = metierQueryHandler.secteursProposesPourRecherche
        ))
      ).recover {
        case _: ProfilRecruteurIncompletException =>
          Redirect(routes.ProfilController.modificationProfil())
            .flashing(messagesRequest.flash.withMessageErreur("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche"))
      }
    }(recruteurAuthentifieRequest)
  }

  def rechercherCandidats(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      MatchingForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(formWithErrors.errorsAsJson))
        },
        matchingForm => {
          rechercher(matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map(resultatRechercheCandidatDto =>
            Ok(views.html.recruteur.partials.resultatsRecherche(resultatRechercheCandidatDto))
          ).recover {
            case _: ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def rechercher(matchingForm: MatchingForm, recruteurId: RecruteurId): Future[ResultatRechercheCandidat] =
    getRecruteurAvecProfilComplet(recruteurId).flatMap(recruteurDto =>
      if (matchingForm.metier.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParMetier(RechercherCandidatsParMetierQuery(
          codeROME = matchingForm.metier.map(CodeROME).get,
          typeRecruteur = recruteurDto.typeRecruteur.get
        ))
      } else if (matchingForm.secteurActivite.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParSecteur(RechercherCandidatsParSecteurQuery(
          codeSecteurActivite = matchingForm.secteurActivite.map(CodeSecteurActivite(_)).get,
          typeRecruteur = recruteurDto.typeRecruteur.get
        ))
      } else {
        candidatQueryHandler.rechercherCandidatsParDateInscription(RechercherCandidatsParDateInscriptionQuery(
          typeRecruteur = recruteurDto.typeRecruteur.get
        ))
      })

  private def getRecruteurAvecProfilComplet(recruteurId: RecruteurId): Future[ProfilRecruteurDto] =
    recruteurQueryHandler.profilRecruteur(ProfilRecruteurQuery(recruteurId)).map(r => if (!r.profilComplet) throw ProfilRecruteurIncompletException() else r)

  def telechargerCV(candidatId: String, nomFichier: String): Action[AnyContent] = recruteurAuthentifieAction.async { implicit recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.cvCandidatPourRecruteur(CVCandidatPourRecruteurQuery(
      candidatId = CandidatId(candidatId),
      recruteurId = recruteurAuthentifieRequest.recruteurId
    )).map(fichierCv => {
      val source: Source[ByteString, _] = Source.fromIterator[ByteString](
        () => Iterator.fill(1)(ByteString(fichierCv.data))
      )

      Result(
        header = ResponseHeader(200, Map(
          "Content-Disposition" -> "inline"
        )),
        body = HttpEntity.Streamed(
          data = source,
          contentLength = Some(fichierCv.data.length.toLong),
          contentType = Some(fichierCv.typeMedia.value))
      )
    }).recover {
      case _: UnauthorizedQueryException =>
        Redirect(routes.LandingController.landing()).flashing(recruteurAuthentifieRequest.flash.withMessageErreur("Vous n'êtes pas autorisé à accéder à cette ressource"))
    }
  }

}

case class ProfilRecruteurIncompletException() extends Exception
