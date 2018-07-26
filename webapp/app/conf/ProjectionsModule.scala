package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher}
import fr.poleemploi.perspectives.domain.candidat.cv.CVService
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.projections.candidat.{CandidatNotificationSlackProjection, CandidatProjection, CandidatQueryHandler}
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurProjection, RecruteurQueryHandler}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcBackend.Database

class RegisterProjections @Inject()(eventPublisher: EventPublisher,
                                    eventHandler: EventHandler,
                                    candidatProjection: CandidatProjection,
                                    candidatNotificationSlackProjection: CandidatNotificationSlackProjection,
                                    recruteurProjection: RecruteurProjection,
                                    webAppConfig: WebAppConfig) {
  eventPublisher.subscribe(eventHandler)

  eventHandler.subscribe(candidatProjection)
  eventHandler.subscribe(recruteurProjection)

  if (webAppConfig.useSlackNotificationCandidat) {
    eventHandler.subscribe(candidatNotificationSlackProjection)
  }
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjection(database: Database): CandidatProjection =
    new CandidatProjection(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def candidatQueryHandler(candidatProjection: CandidatProjection,
                           cvService: CVService): CandidatQueryHandler =
    new CandidatQueryHandler(
      candidatProjection = candidatProjection,
      cvService = cvService
    )

  @Provides
  @Singleton
  def CandidatNotificationSlackProjection(webAppConfig: WebAppConfig,
                                          wsClient: WSClient): CandidatNotificationSlackProjection =
    new CandidatNotificationSlackProjection(
      slackCandidatConfig = webAppConfig.slackCandidatConfig,
      wsClient = wsClient
    )

  @Provides
  @Singleton
  def recruteurProjection(database: Database): RecruteurProjection =
    new RecruteurProjection(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def recruteurQueryHandler(recruteurProjection: RecruteurProjection): RecruteurQueryHandler =
    new RecruteurQueryHandler(
      recruteurProjection = recruteurProjection
    )

}
