import { Injectable, computed, signal } from '@angular/core';
import { MembreResponse } from '../../shared/models/membre.model';

export const MEMBER_SESSION_KEY = 'padel_member_session';

interface MemberSessionState {
  member: MembreResponse;
  token: string | null;
}

@Injectable({ providedIn: 'root' })
export class MemberSessionService {
  private readonly sessionState = signal<MemberSessionState | null>(this.loadFromStorage());

  readonly member = computed(() => this.sessionState()?.member ?? null);
  readonly memberId = computed(() => this.sessionState()?.member.id ?? null);
  readonly matricule = computed(() => this.sessionState()?.member.matricule ?? null);
  readonly token = computed(() => this.sessionState()?.token ?? null);
  readonly isAuthenticated = computed(() => !!this.sessionState()?.member.id);

  setMember(member: MembreResponse, token?: string): void {
    const state: MemberSessionState = { member, token: token ?? member.token ?? null };
    this.sessionState.set(state);
    localStorage.setItem(MEMBER_SESSION_KEY, JSON.stringify(state));
  }

  clearMember(): void {
    this.sessionState.set(null);
    localStorage.removeItem(MEMBER_SESSION_KEY);
  }

  private loadFromStorage(): MemberSessionState | null {
    const raw = localStorage.getItem(MEMBER_SESSION_KEY);
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw);
      // Migration of old local storage data
      if (parsed && 'id' in parsed) {
         return { member: parsed as MembreResponse, token: parsed.token ?? null };
      }
      return parsed as MemberSessionState;
    } catch {
      localStorage.removeItem(MEMBER_SESSION_KEY);
      return null;
    }
  }
}
