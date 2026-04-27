import {Pipe, PipeTransform} from '@angular/core';
import {Match, StatutMatch} from '../../core/models/match.model';

@Pipe({
  name: 'matchFilter',
  standalone: true
})
export class MatchFilterPipe implements PipeTransform {
  transform(matches: Match[], statut: StatutMatch): Match[] {
    return matches.filter(m => m.statut === statut);
  }
}
