package controllers.candidat

import authentification.infra.play.{OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import fr.poleemploi.perspectives.projections.metier.MetierQueryHandler
import javax.inject._
import play.api.mvc._

@Singleton
class LandingController @Inject()(cc: ControllerComponents,
                                  implicit val webAppConfig: WebAppConfig,
                                  optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                  metierQueryHandler: MetierQueryHandler) extends AbstractController(cc) {

  def landing() = optionalCandidatAuthentifieAction { implicit request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    Ok(views.html.candidat.landing(
      candidatAuthentifie = request.candidatAuthentifie,
      secteursActivites = metierQueryHandler.secteursProposesPourRecherche
    ))
  }
}
