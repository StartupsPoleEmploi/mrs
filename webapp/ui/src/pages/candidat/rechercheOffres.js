import Vue from 'vue';
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
import '../../commun/filters.js';
import Pagination from '../../composants/Pagination.vue';
import Places from '../../composants/Places.vue';
import PostulerOffre from '../../composants/PostulerOffre.vue';
import rayonsRechercheOffre from "../../domain/offre/rayonRecherche";
import typesContrats from '../../domain/offre/typesContrats.js';
import tracking from '../../commun/tracking';

new Vue({
    el: '#rechercheOffres',
    components: {
        'pagination': Pagination,
        'places': Places,
        'postuler-offre': PostulerOffre
    },
    data: function () {
        return {
            isCandidatAuthentifie: jsData.candidatAuthentifie,
            cv: jsData.cv,
            csrfToken: jsData.csrfToken,
            nbOffresParPage: 10,
            offres: [],
            pageSuivante: null,
            indexPaginationOffre: 1,
            indexNavigationOffre: 0,
            offreCourante: {
                contrat: {},
                lieuTravail: {},
                salaire: {},
                entreprise: {},
                contact: {},
                formations: [],
                permis: [],
                langues: []
            },
            rechercheFormData: Object.assign({
                motsCles: null,
                localisation: {
                    codePostal: null,
                    lieuTravail: null,
                    rayonRecherche: 0
                },
                typesContrats: [],
                metiers: []
            }, jsData.rechercheFormData),
            rechercheFormErrors: {
                localisation: null
            },
            rayonsRecherche: rayonsRechercheOffre,
            typesContrats: typesContrats,
            metiersValides: Object.assign([], jsData.metiersValides),
            placesOptions : {
                appId: jsData.algoliaPlacesConfig.appId,
                apiKey: jsData.algoliaPlacesConfig.apiKey
            },
            display: {
                contact: false,
                chargement: false,
                erreurRecherche: false,
                modaleDetailOffre: false
            }
        }
    },
    created: function() {
        tracking.trackCommonActions();
        if (!this.rechercheFormData.localisation.rayonRecherche) {
            this.rechercheFormData.localisation.rayonRecherche = 0;
        }
    },
    mounted: function () {
        var self = this;
        window.location = '#';
        var modaleDetail = $('#detailOffre');
        modaleDetail.on('show.bs.modal', function () {
            self.display.modaleDetailOffre = true;
            window.location = '#detailOffre';
        }).on('hide.bs.modal', function () {
            self.display.modaleDetailOffre = false;
            window.location = '#';

            tracking.sendEvent(tracking.Events.CANDIDAT_FERMETURE_DETAIL_OFFRE, self.contexteOffreCourante());
        });
        window.onpopstate = function (event) {
            if (self.display.modaleDetailOffre && window.location.href.endsWith('#')) {
                modaleDetail.modal('hide');
            }
            if (!self.display.modaleDetailOffre && window.location.href.endsWith('#detailOffre')) {
                modaleDetail.modal('show');
            }
        };

        this.rechercherOffresSansPagination();
    },
    computed: {
        pages: function () {
            var nbPages = Math.ceil(this.offres.length / this.nbOffresParPage);
            var result = [];
            for (var i = 0; i < nbPages; i++) {
                result.push(i * this.nbOffresParPage);
            }
            return result;
        }
    },
    methods: {
        nettoyerErreursForm: function() {
            this.rechercheFormErrors = {
                localisation: []
            };
        },
        placesChange: function (suggestion) {
            this.rechercheFormData.localisation.codePostal = suggestion.postcode;
            this.rechercheFormData.localisation.lieuTravail = suggestion.name;

            var localisation = this.rechercheFormData.localisation;
            history.pushState({}, '', window.location.pathname + '?codePostal=' + localisation.codePostal + '&lieuTravail=' + localisation.lieuTravail + '&rayonRecherche=' + localisation.rayonRecherche + '#');
        },
        placesClear: function () {
            this.rechercheFormData.localisation.codePostal = null;
            this.rechercheFormData.localisation.lieuTravail = null;
        },
        chargerPage: function (index) {
            if (index === this.pages.length && this.pageSuivante) {
                var self = this;
                this.rechercherOffres(index, this.pageSuivante).done(function (response) {
                    self.offres = self.offres.concat(response.offres);
                });
            } else {
                this.indexPaginationOffre = index;
                this.$refs.pagination.pageChargee(index);

                tracking.sendEvent(tracking.Events.CANDIDAT_AFFICHAGE_RESULTATS_RECHERCHE_OFFRE, {
                    'page_courante': index
                });
            }
        },
        cssTypeContrat: function (offre) {
            return (offre && offre.contrat && typesContrats[offre.contrat.code]) ? 'typeContrat--' + offre.contrat.code : 'typeContrat--default';
        },
        doitAfficherOffre: function (index) {
            var max = this.indexPaginationOffre * this.nbOffresParPage;
            return index >= (max - this.nbOffresParPage) && index < (max);
        },
        doitAfficherOffreSuivante: function () {
            return this.indexNavigationOffre !== (this.offres.length - 1) || this.pageSuivante;
        },
        doitAfficherOffrePrecedente: function () {
            return this.indexNavigationOffre !== 0;
        },
        afficherOffre: function (offre, index) {
            this.display.contact = false;
            this.offreCourante = offre;
            this.indexNavigationOffre = index;

            tracking.sendEvent(tracking.Events.CANDIDAT_AFFICHAGE_DETAIL_OFFRE, Object.assign({
                'source': 'liste'
            }, this.contexteOffreCourante()));

            $('#detailOffre').modal('show');
        },
        afficherOffreSuivante: function () {
            this.display.contact = false;
            this.indexNavigationOffre = this.indexNavigationOffre + 1;

            if (this.indexNavigationOffre === this.offres.length && this.pageSuivante) {
                var self = this;
                this.rechercherOffres(this.pages.length + 1, this.pageSuivante).done(function (response) {
                    self.offres = self.offres.concat(response.offres);
                    self.offreCourante = self.offres[self.indexNavigationOffre];
                });
            } else {
                this.offreCourante = this.offres[this.indexNavigationOffre];
            }
            this.trackerOffreSuivante();
        },
        afficherOffrePrecedente: function () {
            this.display.contact = false;
            this.indexNavigationOffre = this.indexNavigationOffre - 1;
            this.offreCourante = this.offres[this.indexNavigationOffre];
            this.trackerOffrePrecedente();
        },
        doitAfficherMiseEnAvantInscription: function () {
            return !this.isCandidatAuthentifie;
        },
        afficherFiltres: function () {
            if ($(".formulaireRecherche-jsResponsive").is(":visible")) {
                $(".formulaireRecherche-conteneurFiltres").show();
                $(".formulaireRecherche-retourListeResultats").show();
            }
        },
        cacherFiltres: function () {
            if ($(".formulaireRecherche-jsResponsive").is(":visible")) {
                $(".formulaireRecherche-conteneurFiltres").hide();
                $(".formulaireRecherche-retourListeResultats").hide();
            }
        },
        rechercherOffresSansPagination: function () {
            this.nettoyerErreursForm();

            if (!this.rechercheFormData.localisation.lieuTravail ||
                !this.rechercheFormData.localisation.codePostal) {
                this.rechercheFormErrors.localisation = ["Veuillez saisir une valeur pour ce champ"];
            }

            if (this.rechercheFormErrors.localisation.length === 0) {
                var self = this;
                this.rechercherOffres(1, null).done(function (response) {
                    self.offres = response.offres;
                });
            }
        },
        rechercherOffres: function(index, pageSuivante) {
            var self = this;
            var formData = $("#js-rechercheOffresForm").serializeArray();
            if (pageSuivante) {
                formData.push({name: "page.debut", value: pageSuivante.debut});
                formData.push({name: "page.fin", value: pageSuivante.fin});
            }

            tracking.sendEvent(tracking.Events.CANDIDAT_RECHERCHE_OFFRE, {
                'code_postal': this.rechercheFormData.localisation ? this.rechercheFormData.localisation.codePostal : '',
                'lieu_recherche': this.rechercheFormData.localisation ? this.rechercheFormData.localisation.lieuTravail : '',
                'rayon_recherche': this.rechercheFormData.localisation ? this.rechercheFormData.localisation.rayonRecherche : '',
                'types_contrat': this.rechercheFormData.typesContrats,
                'metiers': this.rechercheFormData.metiers,
                'mots_cles': this.rechercheFormData.motsCles
            });

            return $.ajax({
                type: "POST",
                url: "/candidat/offres",
                data: formData,
                dataType: "json",
                beforeSend: function () {
                    self.display.chargement = true;
                }
            }).done(function (response) {
                self.indexPaginationOffre = index;
                self.$refs.pagination.pageChargee(index);
                self.pageSuivante = response.pageSuivante;
                self.cacherFiltres();
                self.display.erreurRecherche = false;
                self.nettoyerErreursForm();

                tracking.sendEvent(tracking.Events.CANDIDAT_AFFICHAGE_RESULTATS_RECHERCHE_OFFRE, {
                    'page_courante': index
                });
            }).fail(function (jqXHR) {
                self.offres = [];
                if (jqXHR.status === 400) {
                    self.rechercheFormErrors = jqXHR.responseJSON;
                } else {
                    self.nettoyerErreursForm();
                    self.display.erreurRecherche = true;
                }
            }).always(function () {
                self.display.chargement = false;
            });
        },
        afficherContact: function() {
            this.display.contact = true;
            tracking.sendEvent(tracking.Events.CANDIDAT_CLIC_BTN_CONTACT_RECRUTEUR, this.contexteOffreCourante());
        },
        voirOffreSurPoleEmploi: function() {
            tracking.sendEvent(tracking.Events.CANDIDAT_CLIC_BTN_VOIR_OFFRE_SUR_POLE_EMPLOI, this.contexteOffreCourante());
        },
        trackerOffreSuivante: function() {
            tracking.sendEvent(tracking.Events.CANDIDAT_AFFICHAGE_DETAIL_OFFRE, Object.assign({
                'source': 'clic_btn_suivant'
            }, this.contexteOffreCourante()));
        },
        trackerOffrePrecedente: function() {
            tracking.sendEvent(tracking.Events.CANDIDAT_AFFICHAGE_DETAIL_OFFRE, Object.assign({
                'source': 'clic_btn_precedent'
            }, this.contexteOffreCourante()));
        },
        contexteOffreCourante: function() {
            return {
                'offre_id': this.offreCourante.id,
                'code_rome': this.offreCourante.codeROME,
                'code_postal': this.offreCourante.lieuTravail ? this.offreCourante.lieuTravail.codePostal : '',
                'localisation': this.offreCourante.lieuTravail ? this.offreCourante.lieuTravail.libelle : ''
            }
        }
    }
});
