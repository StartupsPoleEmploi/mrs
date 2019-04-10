package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.{Query, QueryHandler, QueryResult}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MetierQueryHandler(referentielMetier: ReferentielMetier) extends QueryHandler {

  override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
    case SecteursActiviteQuery => referentielMetier.secteursActivitesRecherche.map(l =>
      SecteursActiviteQueryResult(l.map(s => SecteurActiviteDTO(
        code = s.code,
        label = s.label,
        metiers = s.metiers.map(m => MetierDTO(
          codeROME = m.codeROME,
          label = m.label
        )),
        domainesProfessionnels = s.domainesProfessionnels.map(d => DomaineProfessionnelDTO(
          code = d.code,
          label = d.label
        ))
      )))
    )
    case q: MetierRechercheParCodeROMEQuery => referentielMetier.metiersRechercheParCodeROME(Set(q.codeROME)).map(l =>
      MetierRechercheParCodeROMEQueryResult(l.map(m => MetierDTO(
        codeROME = m.codeROME,
        label = m.label
      )).head)
    )
    case q: SecteurActiviteParCodeQuery => referentielMetier.secteurActiviteRechercheParCode(q.code).map(s => SecteurActiviteParCodeQueryResult(SecteurActiviteDTO(
      code = s.code,
      label = s.label,
      metiers = s.metiers.map(m => MetierDTO(
        codeROME = m.codeROME,
        label = m.label
      )),
      domainesProfessionnels = s.domainesProfessionnels.map(d => DomaineProfessionnelDTO(
        code = d.code,
        label = d.label
      ))
    )))
  }


}
