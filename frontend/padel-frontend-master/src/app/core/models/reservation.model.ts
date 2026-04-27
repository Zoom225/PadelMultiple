export enum StatutReservation {
  EN_ATTENTE = 'EN_ATTENTE',
  CONFIRMEE = 'CONFIRMEE',
  ANNULEE = 'ANNULEE'
}

export interface Reservation {
  id: number;
  matchId: number;
  matchDateTime: string;
  membreId: number;
  membreNom: string;
  statut: StatutReservation;
  paiement: Paiement | null;
}

export interface Paiement {
  id: number;
  montant: number;
  statut: StatutPaiement;
  datePaiement: string | null;
}

export enum StatutPaiement {
  EN_ATTENTE = 'EN_ATTENTE',
  PAYE = 'PAYE',
  REMBOURSE = 'REMBOURSE'
}
