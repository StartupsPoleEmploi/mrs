package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.recruteur.RecruteurId
import fr.poleemploi.perspectives.domain.{Metier, SecteurActivite}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.recruteur.{GetRecruteurQuery, RecruteurQueryHandler}
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.mvc.{Action, _}
import utils.EitherUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(cc: ControllerComponents,
                                   implicit val webAppConfig: WebAppConfig,
                                   messagesAction: MessagesActionBuilder,
                                   candidatQueryHandler: CandidatQueryHandler,
                                   recruteurQueryHandler: RecruteurQueryHandler,
                                   recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val matchingForm = MatchingForm(
        secteurActivite = None,
        metiers = Set.empty
      )
      rechercher(matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map { resultatRechercheCandidatDto =>
        MatchingForm.form.fill(matchingForm)
        Ok(views.html.recruteur.matching(MatchingForm.form.fill(matchingForm), recruteurAuthentifieRequest.recruteurAuthentifie, resultatRechercheCandidatDto))
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
          )
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def rechercher(matchingForm: MatchingForm, recruteurId: RecruteurId): Future[ResultatRechercheCandidat] =
    for {
      // FIXME : vérification à faire dans la query directement + rediriger vers la modification de profil
      recruteur <- recruteurQueryHandler.getRecruteur(GetRecruteurQuery(recruteurId))
      _ <- Either.cond(recruteur.profilComplet, (), "Le recruteur doit renseigné son profil avant de pouvoir effectuer une recherche").toFuture
      candidats <-
        if (matchingForm.metiers.exists(_.nonEmpty)) {
          candidatQueryHandler.rechercherCandidatsParMetier(RechercherCandidatsParMetierQuery(
            metiers = matchingForm.metiers.flatMap(Metier.from),
            typeRecruteur = recruteur.typeRecruteur.get
          ))
        } else if (matchingForm.secteurActivite.isDefined) {
          candidatQueryHandler.rechercherCandidatsParSecteur(RechercheCandidatsParSecteurQuery(
            secteur = matchingForm.secteurActivite.flatMap(SecteurActivite.from).get,
            typeRecruteur = recruteur.typeRecruteur.get
          ))
        } else {
          candidatQueryHandler.rechercheCandidatsParDateInscription(RechercherCandidatsParDateInscriptionQuery(
            typeRecruteur = recruteur.typeRecruteur.get
          ))
        }
    } yield candidats

  def telechargerCV(candidatId: String): Action[AnyContent] = recruteurAuthentifieAction.async { implicit recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.getCVPourRecruteurParCandidat(GetCVPourRecruteurParCandidatQuery(
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
            contentType = Some(fichierCv.typeMedia))
        )
      }).recover {
      case _: UnauthorizedQueryException =>
        Redirect(routes.LandingController.landing()).flashing(recruteurAuthentifieRequest.flash.withMessageErreur("Vous n'êtes pas autorisé à accéder à cette ressource"))
    }
  }

}
