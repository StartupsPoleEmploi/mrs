package fr.poleemploi.perspectives.projections.candidat.infra.slack

import fr.poleemploi.perspectives.candidat.CandidatInscritEvent
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.slack.SlackConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.projections.candidat.CandidatNotificationProjection
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatNotificationSlackConfig(slackConfig: SlackConfig,
                                           environnement: Environnement) {
  def webhookURL: String = slackConfig.webhookURL
}

class CandidatNotificationSlackAdapter(config: CandidatNotificationSlackConfig,
                                       wsClient: WSClient) extends CandidatNotificationProjection with WSAdapter {

  override def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    wsClient
      .url(s"${config.webhookURL}")
      .addHttpHeaders(jsonContentType)
      .post(Json.obj("text" -> s"Nouveau candidat inscrit en ${config.environnement.value}"))
      .flatMap(filtreStatutReponse(_))
      .map(_ => ())
}
