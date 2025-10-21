import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WaterTankRequest } from '../models/water-tank-request';

@Injectable({ providedIn: 'root' })
export class HouseService {
  private readonly baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  /**
   * Create a new house. Accepts a partial HouseDto (backend will assign ids/derived values).
   * Returns the created HouseDto from the backend.
   */
  postHouseWaterTank(house: Partial<WaterTankRequest>): Observable<HttpResponse<void>> {
    return this.http.post<void>(this.baseUrl + '/houses', house, {
      observe: "response"
    });
  }

  getPipes(): Observable<{ key: string; value: string }[]> {
    return this.http.get<{ key: string; value: string }[]>(this.baseUrl + "/pipes");
  }
}
