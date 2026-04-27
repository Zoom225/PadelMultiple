export enum TypeMatch {
  PRIVE = 'PRIVE',
  PUBLIC = 'PUBLIC'
}

export enum StatutMatch {
  PLANIFIE = 'PLANIFIE',
  COMPLET = 'COMPLET',
  ANNULE = 'ANNULE'
}

export interface Match {
  id: number;
  terrainId: number;
  terrainNom: string;
  siteNom: string;
  organisateurId: number;
  organisateurNom: string;
  date: string;
  heureDebut: string;
  heureFin: string;
  typeMatch: TypeMatch;
  statut: StatutMatch;
  nbJoueursActuels: number;
  prixParJoueur: number;
  dateConversionPublic: string | null;
}
