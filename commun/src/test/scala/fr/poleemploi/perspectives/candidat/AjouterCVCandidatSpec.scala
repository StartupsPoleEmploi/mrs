package fr.poleemploi.perspectives.candidat

import java.nio.file.Path
import java.util.UUID

import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, CVService}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

import scala.concurrent.Future

class AjouterCVCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatBuilder: CandidatBuilder = new CandidatBuilder
  val cvId: CVId = CVId(UUID.randomUUID().toString)

  val commande: AjouterCVCommand =
    AjouterCVCommand(
      id = candidatBuilder.candidatId,
      nomFichier = "cv.doc",
      typeMedia = "application/word",
      path = mock[Path]
    )

  var cvService: CVService = _

  before {
    cvService = mock[CVService]
    when(cvService.nextIdentity) thenReturn cvId
  }

  "ajouterCV" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
      )
    }
    "renvoyer une erreur lorsque le CV existe déjà" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecCV(cvId).build

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Impossible d'ajouter un CV au candidat ${candidat.id.value}, il existe déjà"
      )
    }
    "renvoyer une erreur lorsque le service externe qui enregistre le CV echoue" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      when(cvService.save(cvId, commande.id, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe "erreur de service"
      )
    }
    "generer un evenement lorsque le CV est ajouté" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      when(cvService.save(cvId, commande.id, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.ajouterCV(commande, cvService)

      // Then
      future map (events => events.size mustBe 1)
    }
    "genere un événement contenant les informations modifiees" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      when(cvService.save(cvId, commande.id, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.ajouterCV(commande, cvService)

      // Then
      future map (events => {
        val event = events.head.asInstanceOf[CVAjouteEvent]
        event.candidatId mustBe commande.id
        event.cvId mustBe cvId
      })
    }
  }

}