CREATE TABLE recruteurs
(
  id             BIGSERIAL              NOT NULL,
  recruteur_id CHARACTER VARYING(255) NOT NULL,
  nom   CHARACTER VARYING(255) NOT NULL,
  prenom   CHARACTER VARYING(255) NOT NULL,
  genre   CHARACTER VARYING(255) NOT NULL,
  email   CHARACTER VARYING(255) NOT NULL,
  numero_siret   CHARACTER VARYING(255),
  numero_telephone   CHARACTER VARYING(255),
  raison_sociale   CHARACTER VARYING(255),
  type_recruteur   CHARACTER VARYING(255),
  contact_par_candidats BOOL,
  date_inscription TIMESTAMP with time zone,
  CONSTRAINT recruteurs_pk PRIMARY KEY (id),
  UNIQUE (recruteur_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE recruteurs OWNER TO perspectives;
COMMENT ON TABLE recruteurs IS 'Table des recruteurs';
COMMENT ON COLUMN recruteurs.recruteur_id IS 'Identifiant unique du recruteur';

CREATE UNIQUE INDEX recruteur_id_idx ON recruteurs (recruteur_id);