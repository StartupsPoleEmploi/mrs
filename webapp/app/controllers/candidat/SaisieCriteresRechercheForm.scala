package controllers.candidat

import controllers.FormHelpers
import fr.poleemploi.perspectives.candidat.TempsTravail
import fr.poleemploi.perspectives.commun.domain.RayonRecherche
import fr.poleemploi.perspectives.projections.candidat.CandidatSaisieCriteresRechercheQueryResult
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.libs.json.{Json, Writes}

case class LocalisationRechercheForm(commune: String,
                                     codePostal: String,
                                     longitude: Double,
                                     latitude: Double)

object LocalisationRechercheForm {

  implicit val writes: Writes[LocalisationRechercheForm] = Json.writes[LocalisationRechercheForm]
}

case class SaisieCriteresRechercheForm(nouveauCandidat: Boolean,
                                       contactRecruteur: String,
                                       contactFormation: String,
                                       numeroTelephone: Option[String],
                                       localisation: LocalisationRechercheForm,
                                       rayonRecherche: Option[Int],
                                       tempsTravail: String,
                                       metiersValidesRecherches: Set[String],
                                       metiersRecherches: Set[String],
                                       domainesProfessionnelsRecherches: Set[String])

object SaisieCriteresRechercheForm {

  implicit val writes: Writes[SaisieCriteresRechercheForm] = Json.writes[SaisieCriteresRechercheForm]

  val rayonRechercheConstraint: Constraint[Int] = Constraint("constraint.rayonRecherche")(
    n => RayonRecherche.from(n)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.rayonRecherche"))))
  )

  val tempsTravailConstraint: Constraint[String] = Constraint("constraint.tempsTravail")(
    text => TempsTravail.from(text)
      .map(_ => Valid)
      .getOrElse(Invalid(Seq(ValidationError("constraint.tempsTravail"))))
  )

  def numeroTelephonePourContactConstraint: Constraint[SaisieCriteresRechercheForm] =
    Constraint(
      fields =>
        if ((fields.contactRecruteur == "true" || fields.contactFormation == "true") &&
          fields.numeroTelephone.isEmpty) {
          Invalid(ValidationError("constraint.criteres.numeroTelephoneContact"))
        } else {
          Valid
        }
    )

  val form = Form(
    mapping(
      "nouveauCandidat" -> boolean,
      "contactRecruteur" -> nonEmptyText,
      "contactFormation" -> nonEmptyText,
      "numeroTelephone" -> optional(text.verifying(FormHelpers.numeroTelephoneConstraint)),
      "localisation" -> mapping(
        "commune" -> nonEmptyText,
        "codePostal" -> nonEmptyText,
        "longitude" -> of[Double],
        "latitude" -> of[Double]
      )(LocalisationRechercheForm.apply)(LocalisationRechercheForm.unapply),
      "rayonRecherche" -> optional(number.verifying(rayonRechercheConstraint)),
      "tempsTravail" -> nonEmptyText.verifying(tempsTravailConstraint),
      "metiersValidesRecherches" -> set(text),
      "metiersRecherches" -> set(text),
      "domainesProfessionnelsRecherches" -> set(text)
    )(SaisieCriteresRechercheForm.apply)(SaisieCriteresRechercheForm.unapply) verifying numeroTelephonePourContactConstraint
  )

  def nouveauCandidat: Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = true,
      contactRecruteur = "",
      contactFormation = "",
      numeroTelephone = None,
      localisation = LocalisationRechercheForm(
        codePostal = "",
        commune = "",
        latitude = 0,
        longitude = 0
      ),
      rayonRecherche = None,
      tempsTravail = "",
      metiersValidesRecherches = Set.empty,
      metiersRecherches = Set.empty,
      domainesProfessionnelsRecherches = Set.empty
    )
  )

  def fromCandidatCriteresRechercheQueryResult(candidat: CandidatSaisieCriteresRechercheQueryResult): Form[SaisieCriteresRechercheForm] = SaisieCriteresRechercheForm.form.fill(
    SaisieCriteresRechercheForm(
      nouveauCandidat = false,
      contactRecruteur = FormHelpers.optBooleanToString(candidat.contactRecruteur),
      contactFormation = FormHelpers.optBooleanToString(candidat.contactFormation),
      numeroTelephone = candidat.numeroTelephone.map(_.value),
      localisation = LocalisationRechercheForm(
        codePostal = candidat.localisationRecherche.map(_.codePostal).getOrElse(""),
        commune = candidat.localisationRecherche.map(_.commune).getOrElse(""),
        latitude = candidat.localisationRecherche.map(_.coordonnees.latitude).getOrElse(0),
        longitude = candidat.localisationRecherche.map(_.coordonnees.longitude).getOrElse(0)
      ),
      rayonRecherche = candidat.localisationRecherche.flatMap(_.rayonRecherche.map(_.value)), // FIXME : unité longueur
      tempsTravail = candidat.tempsTravail.map(_.value).getOrElse(""),
      metiersValidesRecherches = candidat.metiersValidesRecherches.map(_.value),
      metiersRecherches = candidat.metiersRecherches.map(_.value),
      domainesProfessionnelsRecherches = candidat.domainesProfessionnelsRecherches.map(_.value)
    )
  )
}