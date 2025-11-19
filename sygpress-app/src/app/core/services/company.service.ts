import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Company, CompanyRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private readonly apiUrl = `${environment.apiUrl}/company`;

  constructor(private http: HttpClient) {}

  getCompany(): Observable<Company> {
    return this.http.get<Company>(this.apiUrl);
  }

  updateCompany(company: CompanyRequest): Observable<Company> {
    return this.http.post<Company>(this.apiUrl, company);
  }

  uploadLogo(file: File): Observable<Company> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Company>(`${this.apiUrl}/logo`, formData);
  }
}
