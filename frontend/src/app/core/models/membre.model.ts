export enum TypeMembre {
  GLOBAL = 'GLOBAL',
  SITE = 'SITE',
  LIBRE = 'LIBRE'
}

export interface Membre {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  typeMembre: TypeMembre;
  siteId: number | null;
  siteNom: string | null;
  solde: number;
}
