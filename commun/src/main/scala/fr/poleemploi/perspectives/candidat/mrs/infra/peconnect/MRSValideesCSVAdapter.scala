package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Récupère les MRS validees des candidats issues d'un fichier CSV
  */
class MRSValideesCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  /**
    * VSL : Selectionné
    * VEF : Entrée en formation
    * VEM : Embauché
    */
  val resultatsBeneficiairesValides = List("VSL", "VEF", "VEM")

  val idPEConnectPattern: Pattern = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")

  def load(source: Source[ByteString, _]): Future[Stream[MRSValideeCandidatPEConnect]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(
        m => m.get("dc_ididentiteexterne").exists(s => idPEConnectPattern.matcher(s).matches()) &&
          m.get("dc_rome_id").exists(_.nonEmpty) &&
          m.get("dd_daterealisation").exists(_.nonEmpty) &&
          m.get("kc_resultatsbeneficiaire_id").exists(resultatsBeneficiairesValides.contains)
      ).map(data =>
      MRSValideeCandidatPEConnect(
        peConnectId = PEConnectId(data("dc_ididentiteexterne")),
        codeROME = CodeROME(data("dc_rome_id")), // Pas de validation du code ROME, on fait confiance au SI Pole Emploi
        dateEvaluation = data.get("dd_daterealisation").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get
      )
    ).runWith(Sink.collection)
       // Il peut y avoir deux fois la même MRSValideeCandidatPEConnect avec un statut différent (VSL ou VEM par exemple : on ne prend qu'une seule fois cette MRS)
      .map(s => s.groupBy[MRSValideeCandidatPEConnect](identity).keys.toStream)
  }
}