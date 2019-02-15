package fr.poleemploi.perspectives.offre.infra.local

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Metier, NumeroTelephone}
import fr.poleemploi.perspectives.offre.domain._

import scala.concurrent.Future

class ReferentielOffreLocalAdapter extends ReferentielOffre {

  override def rechercherOffres(criteres: CriteresRechercheOffre): Future[List[Offre]] =
    Future.successful(List.tabulate(40)(n =>
    if (n % 3 == 0)
      Offre(
        id = OffreId("083LRLN"),
        intitule = "Manoeuvre bâtiment",
        urlOrigine = "https://candidat.pole-emploi.fr/offres/recherche/detail/4038653",
        contrat = Contrat(code = "CDD", label = "Contrat à durée déterminée - 1 Mois"),
        metier = Some(Metier(codeROME = CodeROME("H2903"), label = "Conduite d'équipement d'usinage")),
        description = Some("L'agence Osmose emploi recherche pour l'un de ses clients un MANŒUVRE (H/F) sur le secteur de Pau (64)\n\nVotre mission consiste a préparer le terrain, les outils et les matériaux nécessaires à l'exécution de travaux de construction, de réparation ou d'entretien dans le bâtiment, sur les routes ou voiries,selon les règles de sécurité.\n\nVous pouvez être amené a assister le maçon et développer vos compétences en maçonnerie.\n\nCe poste est ouvert a toutes les personnes qui souhaitent s'investir pleinement et devenir autonome et polyvalent sur différentes tâches."),
        lieuTravail = LieuTravail(libelle = Some("Tournai"), codePostal = None),
        libelleDureeTravail = None,
        complementExercice = None,
        conditionExercice = None,
        libelleDeplacement = None,
        experience = ExperienceExige(label = Some("2 ans"), commentaire = None, exige = Some(true)),
        competences = List(
          Competence(label = "Appliquer les mesures correctives", exige = false),
          Competence(label = "Contrôler un produit fini", exige = true),
          Competence(label = "Etudier une demande client", exige = true),
          Competence(label = "Modalités d'accueil", exige = false),
          Competence(label = "Orienter les personnes selon leur demande", exige = true),
          Competence(label = "Règles et consignes de sécurité", exige = true)
        ),
        qualitesProfessionnelles = List(
          QualiteProfessionnelle(label = "Sens de la communication", description = ""),
          QualiteProfessionnelle(label = "Autonomie", description = ""),
          QualiteProfessionnelle(label = "Sens de l’organisation", description = "")
        ),
        salaire = Salaire(
          libelle = Some("Mensuel de 2000.00 Euros à 3000.00 Euros sur 12 mois"),
          commentaire = None,
          complement1 = Some("Chèque repas"),
          complement2 = Some("Mutuelle")
        ),
        permis = List(
          Permis(label = "CE - Poids lourd", exige = true),
          Permis(label = "EC - Remorque", exige = false)
        ),
        langues = Nil,
        formations = Nil,
        entreprise = Entreprise(
          nom = Some("OSMOSE EMPLOI"),
          description = Some("Agence d'emplois, travail temporaire, Placements CDD, CDI"),
          urlLogo = Some("https://entreprise.pole-emploi.fr/static/img/logos/bveAgfMUYhAUWFSa5L0RHqDh7QY7saik.png"),
          urlSite = Some("http://recrutement@iello.fr"),
          effectif = None
        ),
        contact = Contact(
          nom = None,
          coordonnees1 = Some("93 RUE DE LA MARNE"),
          coordonnees2 = Some("95220 HERBLAY"),
          coordonnees3 = Some("Courriel : ape.95132@pole-emploi.fr"),
          telephone = None,
          email = None,
          urlPostuler = None
        ),
        dateActualisation = ZonedDateTime.now()
      )
    else if (n % 2 == 0)
      Offre(
        id = OffreId("083NTLN"),
        intitule = "Responsable contrôle métrologie en industrie",
        urlOrigine = "https://candidat.pole-emploi.fr/offres/recherche/detail/456",
        contrat = Contrat(code = "CDI", label = "Contrat à durée indéterminée"),
        metier = Some(Metier(codeROME = CodeROME("H2903"), label = "Conduite d'équipement d'usinage")),
        description = Some("Rattaché à notre Business Line Chemicals & Pharma, le Responsable Métrologie H/F a pour principales missions : \n\n- Planifier et assurer la gestion métrologique des équipements ainsi que le suivi des maintenances (mise à jour, réparation)\n- Gérer et optimiser les consommables (envoi/réception et gestion des stocks)\n- Prendre en charge la gestion documentaire associée (gestion, archivage, mise en ligne des certificats)\n- Participer à la sélection et assurer le suivi des fournisseurs\n- Piloter la qualification des équipements selon les procédures en place\n- Participer à la rédaction des documents en rapport avec les équipements\n- Réaliser une veille sur les équipements et les nouvelles technologies\n\nBAC+2 Maintenance des Systèmes, maintenance industrielle/métrologie.\nVous avez une connaissance des équipements de laboratoire d'analyses et/ou formation en métrologie.\nVous maîtrisez Word et Excel. \nDes déplacements ponctuels sont à prévoir./F) sur le secteur de Pau (64)\n\nVotre mission consiste a préparer le terrain, les outils et les matériaux nécessaires à l'exécution de travaux de construction, de réparation ou d'entretien dans le bâtiment, sur les routes ou voiries,selon les règles de sécurité.\n\nVous pouvez être amené a assister le maçon et développer vos compétences en maçonnerie.\n\nCe poste est ouvert a toutes les personnes qui souhaitent s'investir pleinement et devenir autonome et polyvalent sur différentes tâches."),
        lieuTravail = LieuTravail(libelle = Some("13 - VITROLLES"), codePostal = Some("13127")),
        libelleDureeTravail = Some("39H Horaires normaux"),
        complementExercice = None,
        conditionExercice = Some("Travail samedi et dimanche"),
        libelleDeplacement = Some("Déplacements : Jamais"),
        experience = ExperienceExige(label = Some("6 mois en commerce"), commentaire = None, exige = Some(false)),
        competences = Nil,
        qualitesProfessionnelles = List(
          QualiteProfessionnelle(label = "Sens de la communication", description = ""),
          QualiteProfessionnelle(label = "Autonomie", description = ""),
          QualiteProfessionnelle(label = "Sens de l’organisation", description = "")
        ),
        salaire = Salaire(
          libelle = Some("Mensuel de 2000.00 Euros à 3000.00 Euros sur 12 mois"),
          commentaire = None,
          complement1 = None,
          complement2 = None
        ),
        permis = List(
          Permis(label = "B", exige = true)
        ),
        langues = Nil,
        formations = List(
          Formation(domaine = None, niveau = None, exige = true),
          Formation(domaine = Some("Commerce international"), niveau = Some("Bac+5 ou équivalent"), exige = false)
        ),
        entreprise = Entreprise(
          nom = Some("INTERTEK FRANCE"),
          description = Some("Entreprise de Charpente Couverture, Maison ossature bois, Escaliéteur, Dal'Alu, composée de 18 salariés. Entreprise créée il y a plus de 35 ans, disposant d'une excellente image dans la région grâce à l'aide de son équipe jeune, passionnée, formée en majorité dans cette entreprise chez les Compagnons du devoir."),
          urlLogo = Some("https://entreprise.pole-emploi.fr/static/img/logos/bveAgfMUYhAUWFSa5L0RHqDh7QY7saik.png"),
          urlSite = Some("http://recrutement@iello.fr"),
          effectif = Some("3 à 5 salariés")
        ),
        contact = Contact(
          nom = Some("M. Bernard Baue"),
          coordonnees1 = None,
          coordonnees2 = None,
          coordonnees3 = None,
          telephone = Some(NumeroTelephone("0169010215")),
          email = None,
          urlPostuler = None
        ),
        dateActualisation = ZonedDateTime.now()
      )
    else
      Offre(
        id = OffreId("083NHGQ"),
        intitule = "Agent de fabrication polyvalent / Agente de fabrication pol (H/F)",
        urlOrigine = "https://candidat.pole-emploi.fr/offres/recherche/detail/123",
        contrat = Contrat(code = "MIS", label = "Travail intérimaire - 6 Mois"),
        metier = Some(Metier(codeROME = CodeROME("H2903"), label = "Conduite d'équipement d'usinage")),
        description = None,
        lieuTravail = LieuTravail(libelle = Some("13 - AUBAGNE"), codePostal = Some("13400")),
        libelleDureeTravail = Some("39H Horaires normaux"),
        complementExercice = Some("Déplacements dans la région à prévoir"),
        conditionExercice = None,
        libelleDeplacement = Some("Déplacements : Ponctuels International"),
        experience = ExperienceExige(label = Some("Débutant accepté"), commentaire = None, exige = None),
        competences = List(
          Competence(label = "Détecter un dysfonctionnement", exige = false),
          Competence(label = "Surveiller le déroulement de l'usinage", exige = true)
        ),
        qualitesProfessionnelles = List(
          QualiteProfessionnelle(label = "Sens de la communication", description = ""),
          QualiteProfessionnelle(label = "Autonomie", description = ""),
          QualiteProfessionnelle(label = "Sens de l’organisation", description = "")
        ),
        salaire = Salaire(
          libelle = None,
          commentaire = Some("Selon profil et expérience"),
          complement1 = None,
          complement2 = None
        ),
        permis = Nil,
        langues = List(
          Langue(label = "Anglais courant", exige = false),
          Langue(label = "Allemand courant", exige = true)
        ),
        formations = List(
          Formation(domaine = Some("Commerce international"), niveau = Some("Bac+2 ou équivalent"), exige = false)
        ),
        entreprise = Entreprise(
          nom = Some("FAB PRODUITS POUR COLLECTIVITES"),
          description = Some("Entreprise de Charpente Couverture, Maison ossature bois, Escaliéteur, Dal'Alu, composée de 18 salariés. Entreprise créée il y a plus de 35 ans, disposant d'une excellente image dans la région grâce à l'aide de son équipe jeune, passionnée, formée en majorité dans cette entreprise chez les Compagnons du devoir."),
          urlLogo = None,
          urlSite = None,
          effectif = Some("20 à 30 salariés")
        ),
        contact = Contact(
          nom = Some("Mme ALEXANDRA SAULNERON"),
          coordonnees1 = None,
          coordonnees2 = None,
          coordonnees3 = None,
          telephone = None,
          email = None,
          urlPostuler = Some("https://parcasterix-recrute.talent-soft.com/offre-de-emploi/emploi-agent-polyvalent-de-restauration-en-contrat-de-professionnalisation_566.aspx")
        ),
        dateActualisation = ZonedDateTime.now()
      )
  ))
}
