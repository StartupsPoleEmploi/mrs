package authentification.infra.peconnect

import fr.poleemploi.perspectives.domain.Genre
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class AccessTokenResponse(accessToken: String,
                               idToken: String,
                               nonce: String)

object AccessTokenResponse {

  implicit val accessTokenResponseReads: Reads[AccessTokenResponse] = (
    (JsPath \ "access_token").read[String] and
      (JsPath \ "id_token").read[String] and
      (JsPath \ "nonce").read[String]
    ) (AccessTokenResponse.apply _)
}

case class PEConnectCandidatInfos(peConnectId: String,
                                  nom: String,
                                  prenom: String,
                                  email: String,
                                  genre: Genre)

private[peconnect] case class CandidatUserInfos(sub: String,
                                                familyName: String,
                                                givenName: String,
                                                email: String,
                                                gender: String)

object CandidatUserInfos {

  implicit val candidatUserInfosReads: Reads[CandidatUserInfos] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (CandidatUserInfos.apply _)

  def toPEConnectCandidatInfos(candidatUserInfos: CandidatUserInfos): PEConnectCandidatInfos =
    PEConnectCandidatInfos(
      peConnectId = candidatUserInfos.sub,
      nom = candidatUserInfos.familyName.toLowerCase,
      prenom = candidatUserInfos.givenName.toLowerCase,
      email = candidatUserInfos.email.toLowerCase,
      genre = candidatUserInfos.gender match {
        case "male" => Genre.HOMME
        case "female" => Genre.FEMME
        case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
      }
    )
}


case class PEConnectRecruteurInfos(peConnectId: String,
                                   nom: String,
                                   prenom: String,
                                   email: String,
                                   genre: Genre)

private[peconnect] case class RecruteurUserInfos(sub: String,
                                                 familyName: String,
                                                 givenName: String,
                                                 email: String,
                                                 gender: String)

object RecruteurUserInfos {

  implicit val recruteurUserInfosReads: Reads[RecruteurUserInfos] = (
    (JsPath \ "sub").read[String] and
      (JsPath \ "family_name").read[String] and
      (JsPath \ "given_name").read[String] and
      (JsPath \ "email").read[String] and
      (JsPath \ "gender").read[String]
    ) (RecruteurUserInfos.apply _)

  def toPEConnectRecruteurInfos(recruteurUserInfos: RecruteurUserInfos): PEConnectRecruteurInfos =
    PEConnectRecruteurInfos(
      peConnectId = recruteurUserInfos.sub,
      nom = recruteurUserInfos.familyName.toLowerCase,
      prenom = recruteurUserInfos.givenName.toLowerCase,
      email = recruteurUserInfos.email.toLowerCase,
      genre = recruteurUserInfos.gender match {
        case "male" => Genre.HOMME
        case "female" => Genre.FEMME
        case g@_ => throw new IllegalArgumentException(s"Genre inconnu : $g")
      }
    )
}