package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

object CandidatProjectionElasticsearchMapping {
  val indexName = "candidats"
  val docType = "_doc"

  val candidat_id = "candidat_id"
  val peconnect_id = "peconnect_id"
  val identifiant_local = "identifiant_local"
  val code_neptune = "code_neptune"
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
  val contact_recruteur = "contact_recruteur"
  val contact_formation = "contact_formation"
  val cv_id = "cv_id"
  val cv_type_media = "cv_type_media"
  val centres_interet = "centres_interet"
  val langues = "langues"
  val permis = "permis"
  val savoir_etre = "savoir_etre"
  val savoir_faire = "savoir_faire"
  val formations = "formations"
  val experiences_professionnelles = "experiences_professionnelles"
  val date_inscription = "date_inscription"
  val date_derniere_connexion = "date_derniere_connexion"
  val date_derniere_maj_disponibilite = "date_derniere_maj_disponibilite"

  val criteres_recherche = "criteres_recherche"
  val metiers_valides_recherche = "criteres_recherche.metiers_valides"
  val metiers_recherche = "criteres_recherche.metiers"
  val domaines_professionnels_recherche = "criteres_recherche.domaines_professionnels"
  val code_postal_recherche = "criteres_recherche.code_postal"
  val commune_recherche = "criteres_recherche.commune"
  val rayon_recherche = "criteres_recherche.rayon"
  val zone_recherche = "criteres_recherche.zone"

  val prochaine_disponibilite = "prochaine_disponibilite"
  val emploi_trouve_grace_perspectives = "emploi_trouve_grace_perspectives"
}
