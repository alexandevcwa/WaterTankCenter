import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'timeFormat'
})
export class TimeFormatPipe implements PipeTransform {

  transform(value: number): string {
    if (value == null) return '-';
    const minutes = Math.floor(value / 60);
    const seconds = Math.floor(value % 60);
    return minutes > 0 ? `${minutes} min ${seconds} s` : `${seconds} s`;
  }

}
