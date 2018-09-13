package controllers.recruteur

import play.api.data.Form
import play.api.data.Forms._

case class MatchingForm(secteurActivite: Option[String],
                        departement: Option[String],
                        metier: Option[String])

object MatchingForm {

  val form = Form(
    mapping(
      "secteurActivite" -> optional(text),
      "departement" -> optional(text),
      "metier" -> optional(text)
    )(MatchingForm.apply)(MatchingForm.unapply)
  )
}