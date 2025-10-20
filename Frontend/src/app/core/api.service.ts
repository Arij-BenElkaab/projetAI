// src/app/core/api.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE_URL } from '../config';

export interface SchemaResponse {
  required_json_keys: string[];
  example: Record<string, number>;
}

export interface PredictResponse {
  prediction: 'panne_probable' | 'aucune_panne';
  probability: number;          // 0..1
  threshold_used: number;       // e.g., 0.707
  mode_hint?: string | null;    // 'balanced' | 'high_recall' | 'default'
  features_used: string[];
  z_scores?: number[];
  warnings?: string[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = API_BASE_URL;

  getSchema(): Observable<SchemaResponse> {
    return this.http.get<SchemaResponse>(`${this.base}/schema`);
  }

  // mode: 'balanced' | 'high_recall' (optional)
  // thr: custom threshold number (optional, overrides mode if provided)
  predict(
    body: Record<string, number>,
    mode?: 'balanced' | 'high_recall',
    thr?: number
  ): Observable<PredictResponse> {
    let params = new HttpParams();
    if (mode) params = params.set('mode', mode);
    if (thr != null) params = params.set('thr', String(thr));
    return this.http.post<PredictResponse>(`${this.base}/predict`, body, { params });
  }

  health(): Observable<any> {
    return this.http.get(`${this.base}/health`);
  }
}
