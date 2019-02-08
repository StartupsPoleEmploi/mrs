"use strict";

var component = Vue.component('temoignages', {
    props: {
        temoignages: Array
    },
    data: function () {
        return {
            indexTemoignageCourant: (this.temoignages !== undefined && this.temoignages.length > 0) ? 0 : null
        }
    },
    methods: {
        chargerTemoignage: function(index) {
            this.temoignageCourant = this.temoignages[index];
            this.indexTemoignageCourant = index;
        },
        isTemoignageCourant: function(index) {
            return this.indexTemoignageCourant === index;
        }
    },
    template:
        '<div> ' +
            '<div class="temoignage" v-for="(temoignage, index) in temoignages" v-show="isTemoignageCourant(index)"> ' +
                '<img alt="Citation" class="temoignage-image temoignage-debutCitation" width="35" height="35" src="/assets/images/composants/temoignages/debut_citation.svg" /> ' +
                '<div>{{temoignage.texte}}</div> ' +
                '<span class="temoignage-source">{{temoignage.source}}</span> ' +
                '<img alt="Citation" class="temoignage-image temoignage-finCitation" width="35" height="35" src="/assets/images/composants/temoignages/fin_citation.svg" /> ' +
            '</div> ' +
            '<div class="paginationTemoignages"> ' +
                '<span v-for="(temoignage, index) in temoignages" v-on:click="chargerTemoignage(index)"' +
                      'class="paginationTemoignages-item" ' +
                      'v-bind:class="[isTemoignageCourant(index) ? \'paginationTemoignages-pageCourante\' : \'paginationTemoignages-page\']"></span> ' +
            '</div> ' +
        '</div>'
});

export default component;