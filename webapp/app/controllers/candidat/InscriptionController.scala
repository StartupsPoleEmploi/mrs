package controllers.candidat

import java.util.UUID

import authentification.infra.play.SessionCandidatAuthentifie
import authentification.model.CandidatAuthentifie
import conf.WebAppConfig
import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, CandidatId, InscrireCandidatCommand}
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InscriptionController @Inject()(cc: ControllerComponents,
                                      webappConfig: WebAppConfig,
                                      candidatCommandHandler: CandidatCommandHandler,
                                      peConnectController: PEConnectController) extends AbstractController(cc) {

  def inscription(): Action[AnyContent] =
    if (webappConfig.usePEConnect) {
      peConnectController.inscription()
    } else inscriptionSimple()

  private def inscriptionSimple(): Action[AnyContent] = Action.async { implicit request =>
    val candidatId = CandidatId(UUID.randomUUID().toString)
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = "plantu",
      prenom = "robert",
      email = "robert.plantu@mail.com",
      genre = Genre.HOMME
    )
    candidatCommandHandler.inscrire(command).map { _ =>
      val candidatAuthentifie = CandidatAuthentifie(
        candidatId = command.id.value,
        nom = command.nom,
        prenom = command.prenom
      )
      Redirect(routes.SaisieCriteresRechercheController.saisieCriteresRecherche())
        .withSession(SessionCandidatAuthentifie.set(candidatAuthentifie, request.session))
    }
  }
}