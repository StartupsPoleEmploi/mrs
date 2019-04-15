package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.LocalisationRecherche
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.metier.domain.{Metier, ReferentielMetier}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.metier.MetierDTO
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME : séparer en mapping Update et en mapping QueryResult
class CandidatProjectionElasticsearchMapping(referentielMetier: ReferentielMetier) {

  import CandidatProjectionElasticsearchMapping._

  def buildCandidatSaisieCriteresRechercheQueryResult(document: CandidatSaisieCriteresRechercheDocument): Future[CandidatSaisieCriteresRechercheQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides).map(metiersValides =>
      CandidatSaisieCriteresRechercheQueryResult(
        candidatId = document.candidatId,
        contactRecruteur = document.contactRecruteur,
        contactFormation = document.contactFormation,
        numeroTelephone = document.numeroTelephone,
        commune = document.commune,
        codePostal = document.codePostal,
        latitude = document.latitude,
        longitude = document.longitude,
        metiersValides = metiersValides.map(buildMetierDTO),
        metiersValidesRecherches = document.criteresRecherche.metiersValides,
        metiersRecherches = document.criteresRecherche.metiers,
        domainesProfessionnelsRecherches = document.criteresRecherche.domainesProfessionels,
        codePostalRecherche = document.criteresRecherche.codePostal,
        communeRecherche = document.criteresRecherche.commune,
        latitudeRecherche = document.criteresRecherche.zone.map(_.latitude),
        longitudeRecherche = document.criteresRecherche.zone.map(_.longitude),
        rayonRecherche = document.criteresRecherche.rayon.map(buildRayonRecherche)
      ))

  def buildCandidatLocalisationQueryResult(document: CandidatLocalisationDocument): CandidatLocalisationQueryResult =
    CandidatLocalisationQueryResult(
      commune = document.commune,
      codePostal = document.codePostal,
      latitude = document.latitude,
      longitude = document.longitude
    )

  def buildMetiersValidesQueryResult(document: CandidatMetiersValidesDocument): Future[CandidatMetiersValidesQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides).map(metiersValides =>
      CandidatMetiersValidesQueryResult(
        metiersValides = metiersValides.map(m => MetierDTO(
          codeROME = m.codeROME,
          label = m.label
        ))
      )
    )

  def buildCandidatDepotCVQueryResult(document: CandidatDepotCVDocument): CandidatDepotCVQueryResult =
    CandidatDepotCVQueryResult(
      candidatId = document.candidatId,
      cvId = document.cvId,
      cvTypeMedia = document.cvTypeMedia
    )

  def buildCandidatPourRechercheOffreQueryResult(document: CandidatPourRechercheOffreDocument): Future[CandidatPourRechercheOffreQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides).map(metiersValides =>
      CandidatPourRechercheOffreQueryResult(
        metiersValides = metiersValides.map(buildMetierDTO),
        localisationRecherche =
          for {
            codePostal <- document.criteresRecherche.codePostal
            commune <- document.criteresRecherche.commune
          } yield LocalisationRecherche(
            commune = commune,
            codePostal = codePostal,
            coordonnees = document.criteresRecherche.zone.map(z => Coordonnees(
              latitude = z.latitude,
              longitude = z.longitude
            )).get,
            rayonRecherche = document.criteresRecherche.rayon.map(buildRayonRecherche)
          ),
        cv = document.cvId.isDefined
      )
    )

  def buildCandidatsRechercheDto(documents: Seq[CandidatRechercheRecruteurDocument]): Future[List[CandidatRechercheRecruteurDto]] =
    for {
      metiersValides <- referentielMetier.metiersParCodesROME(documents.flatMap(_.metiersValides).toSet)
      metiersRecherches <- referentielMetier.metiersRechercheParCodeROME(documents.flatMap(_.metiersRecherches).toSet)
    } yield {
      val mapMetiersValides = metiersValides.groupBy(_.codeROME)
      val mapMetiersRecherches = metiersRecherches.groupBy(_.codeROME)
      documents.map(d => CandidatRechercheRecruteurDto(
        candidatId = d.candidatId,
        nom = d.nom,
        prenom = d.prenom,
        email = d.email,
        metiersValides = d.metiersValides.map(c => buildMetierDTO(mapMetiersValides.get(c).head.head)),
        habiletes = d.habiletes,
        metiersRecherches = d.metiersRecherches.map(c => buildMetierDTO(mapMetiersRecherches.get(c).head.head)),
        numeroTelephone = d.numeroTelephone,
        rayonRecherche = d.rayonRecherche.map(buildRayonRecherche),
        commune = d.communeRecherche,
        cvId = d.cvId,
        cvTypeMedia = d.cvTypeMedia
      )).toList
    }

  def buildCandidatPourConseillerDto(documents: Seq[CandidatPourConseillerDocument]): Future[List[CandidatPourConseillerDto]] =
    for {
      metiersValides <- referentielMetier.metiersParCodesROME((documents.flatMap(_.metiersValidesRecherches) ++ documents.flatMap(_.metiersValides)).toSet)
      metiersRecherches <- referentielMetier.metiersRechercheParCodeROME(documents.flatMap(_.metiersRecherches).toSet)
    } yield {
      val mapMetiersValides = metiersValides.groupBy(_.codeROME)
      val mapMetiersRecherches = metiersRecherches.groupBy(_.codeROME)
      documents.map(d =>
        CandidatPourConseillerDto(
          candidatId = d.candidatId,
          nom = d.nom,
          prenom = d.prenom,
          email = d.email,
          statutDemandeurEmploi = d.statutDemandeurEmploi,
          metiersValides = d.metiersValides.map(c => buildMetierDTO(mapMetiersValides.get(c).head.head)),
          metiersValidesRecherches = d.metiersValidesRecherches.map(c => buildMetierDTO(mapMetiersValides.get(c).head.head)),
          metiersRecherches = d.metiersRecherches.map(c => buildMetierDTO(mapMetiersRecherches.get(c).head.head)),
          contactRecruteur = d.contactRecruteur,
          contactFormation = d.contactFormation,
          communeRecherche = d.communeRecherche,
          codePostalRecherche = d.codePostalRecherche,
          rayonRecherche = d.rayonRecherche.map(buildRayonRecherche),
          numeroTelephone = d.numeroTelephone,
          dateInscription = d.dateInscription,
          dateDerniereConnexion = d.dateDerniereConnexion
        )).toList
    }

  def buildContactRecruteurQueryResult(document: CandidatContactRecruteurDocument): CandidatContactRecruteurQueryResult =
    CandidatContactRecruteurQueryResult(
      contactRecruteur = document.contactRecruteur,
      contactFormation = document.contactFormation
    )

  def buildZoneDocument(coordonnees: Coordonnees, rayonRecherche: Option[RayonRecherche]): ZoneDocument =
    ZoneDocument(
      typeMobilite = rayonRecherche.map(_ => "circle").getOrElse("point"),
      latitude = coordonnees.latitude,
      longitude = coordonnees.longitude,
      radius = rayonRecherche
        .map(r => r.uniteLongueur match {
          case UniteLongueur.KM => s"${r.value * 1.2}km"
          case u@_ => throw new IllegalArgumentException(s"Unite de longueur non gérée ${u.value}")
        })
    )

  def buildRechercheCandidatsParLocalisationQuery(query: RechercheCandidatsQuery): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "filter" -> buildFiltresRechercheCandidatQuery(query)
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          Some(JsNumber(keysetPagination.dateInscription)),
          keysetPagination.candidatId.map(c => JsString(c.value))
        ).flatten)
      )
    ).getOrElse(queryJson)
  }

  def buildRechercheCandidatsParSecteurQuery(query: RechercheCandidatsQuery,
                                             codeSecteurActivite: CodeSecteurActivite): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "function_score" -> Json.obj(
          "query" -> Json.obj(
            "bool" -> Json.obj(
              "must" -> Json.obj("match_all" -> Json.obj()),
              "filter" -> buildFiltresRechercheCandidatQuery(query)
            )
          ),
          "functions" -> JsArray(
            Seq(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeSecteurActivite))
                      ))
                  )
                ),
                "weight" -> 3
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_recherche -> codeSecteurActivite)),
                      ))
                  )
                ),
                "weight" -> 2
              )
            )
          ),
          "score_mode" -> "sum",
          "min_score" -> 2
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj("_score" -> Json.obj("order" -> "desc")),
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          keysetPagination.score.map(JsNumber(_)),
          Some(JsNumber(keysetPagination.dateInscription)),
          keysetPagination.candidatId.map(c => JsString(c.value))
        ).flatten)
      )
    ).getOrElse(queryJson)
  }

  // FIXME : on passe un codeRome mais ca peut aussi etre un code domaine!
  def buildRechercheCandidatsParMetierQuery(query: RechercheCandidatsQuery,
                                            codeROME: CodeROME): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "function_score" -> Json.obj(
          "query" -> Json.obj(
            "bool" -> Json.obj(
              "must" -> Json.obj("match_all" -> Json.obj()),
              "filter" -> buildFiltresRechercheCandidatQuery(query)
            )
          ),
          "functions" -> JsArray(
            Seq(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME)),
                      ))
                  )
                ),
                "weight" -> 6
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must_not" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME))
                      )),
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(metiers_recherche -> codeROME)),
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME.codeSecteurActivite)),
                      ))
                  )
                ),
                "weight" -> 3
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(metiers_recherche -> codeROME))
                      ))
                  )
                ),
                "weight" -> 2
              )
            )
          ),
          "score_mode" -> "sum",
          "min_score" -> 2
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj("_score" -> Json.obj("order" -> "desc")),
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          keysetPagination.score.map(JsNumber(_)),
          Some(JsNumber(keysetPagination.dateInscription)),
          keysetPagination.candidatId.map(c => JsString(c.value))
        ).flatten)
      )
    ).getOrElse(queryJson)
  }

  def buildCandidatPourConseillerQuery(query: CandidatsPourConseillerQuery): JsObject = {
    val queryJson = Json.obj(
      "size" -> (query.nbCandidatsParPage * query.nbPagesACharger),
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "must" -> Json.obj(
            "match_all" -> Json.obj()
          )
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          JsNumber(keysetPagination.dateInscription),
          JsString(keysetPagination.candidatId.value)
        ))
      )
    ).getOrElse(queryJson)
  }

  private def buildFiltresRechercheCandidatQuery(query: RechercheCandidatsQuery): JsArray =
    JsArray(
      Seq(
        buildFiltreTypeRecruteur(query.typeRecruteur),
        Some(buildFiltreMetiersValides),
        Some(buildFiltreRechercheMetier),
        Some(buildFiltreNumeroTelephone),
        Some(buildFiltreCommuneRecherche),
        query.coordonnees.map(c => buildFiltreLocalisation(c))
      ).flatten
    )

  private def buildFiltreTypeRecruteur(typeRecruteur: TypeRecruteur): Option[JsObject] = typeRecruteur match {
    case TypeRecruteur.ORGANISME_FORMATION =>
      Some(Json.obj("term" -> Json.obj(contact_formation -> true)))
    case _ => Some(Json.obj("term" -> Json.obj(contact_recruteur -> true)))
  }

  private def buildFiltreNumeroTelephone: JsObject =
    Json.obj("exists" -> Json.obj("field" -> numero_telephone))

  private def buildFiltreCommuneRecherche: JsObject =
    Json.obj("exists" -> Json.obj("field" -> commune_recherche))

  private def buildFiltreMetiersValides: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc['$metiers_valides'].values.length > 0"
    ))

  private def buildFiltreRechercheMetier: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc['$metiers_valides_recherche'].values.length > 0 || doc['$metiers_recherche'].values.length > 0"
    ))

  private def buildFiltreLocalisation(coordonnees: Coordonnees): JsObject =
    Json.obj("geo_shape" -> Json.obj(
      zone_recherche -> Json.obj(
        "shape" -> Json.obj(
          "coordinates" -> JsArray(Seq(JsNumber(coordonnees.longitude), JsNumber(coordonnees.latitude))),
          "type" -> "point"
        ),
        "relation" -> "contains"
      )
    ))

  private def buildMetierDTO(metier: Metier) = MetierDTO(
    codeROME = metier.codeROME,
    label = metier.label
  )

  private def buildRayonRecherche(document: RayonRechercheDocument): RayonRecherche =
    RayonRecherche(value = document.value, uniteLongueur = document.uniteLongueur)
}

object CandidatProjectionElasticsearchMapping {

  val candidat_id = "candidat_id"
  val nom = "nom"
  val prenom = "prenom"
  val genre = "genre"
  val email = "email"
  val numero_telephone = "numero_telephone"
  val statut_demandeur_emploi = "statut_demandeur_emploi"
  val code_postal = "code_postal"
  val commune = "commune"
  val latitude = "latitude"
  val longitude = "longitude"
  val metiers_valides = "metiers_valides"
  val habiletes = "habiletes"
  val contact_recruteur = "contact_recruteur"
  val contact_formation = "contact_formation"
  val cv_id = "cv_id"
  val cv_type_media = "cv_type_media"
  val date_inscription = "date_inscription"
  val date_derniere_connexion = "date_derniere_connexion"

  val criteres_recherche = "criteres_recherche"
  val metiers_valides_recherche = "criteres_recherche.metiers_valides"
  val metiers_recherche = "criteres_recherche.metiers"
  val domaines_professionnels_recherche = "criteres_recherche.domaines_professionnels"
  val code_postal_recherche = "criteres_recherche.code_postal"
  val commune_recherche = "criteres_recherche.commune"
  val rayon_recherche = "criteres_recherche.rayon"
  val zone_recherche = "criteres_recherche.zone"
}