package fr.poleemploi.perspectives.commun.infra.oauth

case class OauthScope(value: String)

object OauthScope {

  val API_INDIVIDU: List[OauthScope] = List(
    OauthScope("api_peconnect-individuv1"),
    OauthScope("openid"),
    OauthScope("profile"),
    OauthScope("email")
  )

  val API_COORDONNEES = List(
    OauthScope("api_peconnect-coordonneesv1"),
    OauthScope("coordonnees")
  )

  val API_STATUT = List(
    OauthScope("api_peconnect-statutv1"),
    OauthScope("statut")
  )

  val API_PRESTATIONS = List(
    OauthScope("api_prestationssuiviesv1"),
    OauthScope("prestationIntermediaire")
  )

  val API_COMPETENCES = List(
    OauthScope("api_peconnect-competencesv2"),
    OauthScope("pfccompetences"),
    OauthScope("pfclangues"),
    OauthScope("pfccentresinteret")
  )

  val API_EXPERIENCES_PRO = List(
    OauthScope("api_peconnect-experiencesv1"),
    OauthScope("pfcexperiences")
  )

  val API_FORMATIONS = List(
    OauthScope("api_peconnect-formationsv1"),
    OauthScope("pfcformations"),
    OauthScope("pfcpermis")
  )

  val API_ENTREPRISE = List(
    OauthScope("api_peconnect-entreprisev1"),
    OauthScope("openid"),
    OauthScope("profile"),
    OauthScope("email"),
    OauthScope("habilitation")
  )

  val API_OFFRE = List(
    OauthScope("api_offresdemploiv2"),
    OauthScope("o2dsoffre")
  )

  val API_OFFRE_QOS_SILVER = OauthScope("qos_silver_offresdemploiv2")
}
