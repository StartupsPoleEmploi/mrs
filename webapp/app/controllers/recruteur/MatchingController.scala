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
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
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
                                   rechercheCandidatQueryHandler: RechercheCandidatQueryHandler,
                                   recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val matchingForm = MatchingForm(
        secteurActivite = None,
        metier = None,
        departement = None
      )
      rechercher(request = messagesRequest, matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map(resultatRechercheCandidatDto =>
        Ok(views.html.recruteur.matching(
          matchingForm = MatchingForm.form.fill(matchingForm),
          recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
          resultatRechercheCandidat = resultatRechercheCandidatDto,
          secteursActivites = rechercheCandidatQueryHandler.secteursProposes,
          departements = rechercheCandidatQueryHandler.departementsProposes
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
          rechercher(request = messagesRequest, matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map(resultatRechercheCandidatDto =>
            Ok(views.html.recruteur.partials.resultatsRecherche(resultatRechercheCandidatDto))
          ).recover {
            case _: ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def rechercher(request: Request[AnyContent],
                         matchingForm: MatchingForm,
                         recruteurId: RecruteurId): Future[ResultatRechercheCandidat] =
    getTypeRecruteur(request, recruteurId).flatMap(typeRecruteur =>
      if (matchingForm.metier.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParMetier(RechercherCandidatsParMetierQuery(
          codeROME = matchingForm.metier.map(CodeROME).get,
          typeRecruteur = typeRecruteur,
          codeDepartement = matchingForm.departement
        ))
      } else if (matchingForm.secteurActivite.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParSecteur(RechercherCandidatsParSecteurQuery(
          codeSecteurActivite = matchingForm.secteurActivite.map(CodeSecteurActivite(_)).get,
          typeRecruteur = typeRecruteur,
          codeDepartement = matchingForm.departement
        ))
      } else {
        candidatQueryHandler.rechercherCandidatsParDateInscription(RechercherCandidatsParDateInscriptionQuery(
          typeRecruteur = typeRecruteur,
          codeDepartement = matchingForm.departement
        ))
      })

  private def getTypeRecruteur(request: Request[AnyContent], recruteurId: RecruteurId): Future[TypeRecruteur] =
    request.flash.getTypeRecruteur
      .map(t => Future.successful(t))
      .getOrElse {
        recruteurQueryHandler.typeRecruteur(recruteurId).map(_.getOrElse(throw ProfilRecruteurIncompletException()))
      }

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
