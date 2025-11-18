import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDashboard, UserDashboard } from '../models';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getAdminDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.apiUrl}/admin`);
  }

  getUserDashboard(): Observable<UserDashboard> {
    return this.http.get<UserDashboard>(`${this.apiUrl}/user`);
  }
}
