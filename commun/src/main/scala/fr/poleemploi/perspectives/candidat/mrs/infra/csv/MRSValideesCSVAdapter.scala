package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.mrs.infra.MRSValideeCandidatPEConnect
import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.Future

/**
  * Récupère les MRS validees des candidats issues d'un fichier CSV
  */
class MRSValideesCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  val resultatsBeneficiairesValides = List("VSL", "VEF", "VEM")

  def load(source: Source[ByteString, _]): Future[Stream[MRSValideeCandidatPEConnect]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ','))
      .via(CsvToMap.toMapAsStrings())
      .filter(
        m => m.get("DC_IDENTITEEXTERNE").exists(_.nonEmpty) &&
          m.get("DC_ROME_ID").exists(_.nonEmpty) &&
          m.get("DD_DATESORTIEPRESTATIONPREVUE").exists(_.nonEmpty) &&
          m.get("KC_RESULTATSBENEFICIAIRE_ID").exists(resultatsBeneficiairesValides.contains)
      )
      .map(data => {
        MRSValideeCandidatPEConnect(
          peConnectId = PEConnectId(data("DC_IDENTITEEXTERNE")),
          codeROME = CodeROME(data("DC_ROME_ID")), // Pas de validation du code ROME, on fait confiance au SI Pole Emploi
          dateEvaluation = data.get("DD_DATESORTIEPRESTATIONPREVUE").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get
        )
      }).runWith(Sink.collection)
  }
}
