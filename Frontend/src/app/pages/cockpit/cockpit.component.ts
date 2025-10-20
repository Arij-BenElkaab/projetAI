import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService, PredictResponse, SchemaResponse } from '../../core/api.service';

type Mode = 'balanced' | 'high_recall';

@Component({
  selector: 'app-cockpit',
  templateUrl: './cockpit.component.html',
  styleUrls: ['./cockpit.component.scss']
})
export class CockpitComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);

  keys: string[] = [];
  example: Record<string, number> = {};
  form!: FormGroup;

  mode: Mode = 'balanced';
  thr: number | null = null;

  lastResult: PredictResponse | null = null;
  lastProbPct = 0; // 0..100
  busy = false;
  errorMsg = '';

  history: Array<{ when: number; prob: number; prediction: string }> = [];

  ngOnInit(): void {
    this.api.getSchema().subscribe({
      next: (s: SchemaResponse) => {
        this.keys = s.required_json_keys;
        this.example = s.example || {};
        this.buildForm();
      },
      error: (err) => {
        this.errorMsg = 'Failed to load schema. Is the API running?';
        console.error(err);
      }
    });
  }

  buildForm() {
    const group: Record<string, any> = {};
    for (const k of this.keys) {
      group[k] = [this.example[k] ?? null, [Validators.required]];
    }
    this.form = this.fb.group(group);
  }

  useExample() {
    if (!this.form) return;
    for (const k of this.keys) {
      this.form.get(k)?.setValue(this.example[k] ?? null);
    }
  }

  predict() {
    this.errorMsg = '';
    if (!this.form || this.form.invalid) {
      this.errorMsg = 'Please fill all fields correctly.';
      return;
    }
    const body = this.form.value as Record<string, number>;
    this.busy = true;

    this.api
      .predict(body, this.thr == null ? this.mode : undefined, this.thr ?? undefined)
      .subscribe({
        next: (res) => {
          this.lastResult = res;
          this.lastProbPct = Math.round((res.probability || 0) * 100);
          this.history.unshift({
            when: Date.now(),
            prob: this.lastProbPct,
            prediction: res.prediction
          });
          this.history = this.history.slice(0, 8);
        },
        error: (err) => {
          this.errorMsg = 'Prediction failed. Check inputs and try again.';
          console.error(err);
        }
      })
      .add(() => (this.busy = false));
  }

  isRisk(): boolean {
    return this.lastResult?.prediction === 'panne_probable';
  }

  riskBadgeClass(p?: number | null) {
    if (p == null) return 'badge-risk-low';
    if (p < 0.33) return 'badge-risk-low';
    if (p < 0.66) return 'badge-risk-med';
    return 'badge-risk-high';
  }

  riskLabel(p?: number | null) {
    if (p == null) return 'No data';
    if (p < 0.33) return 'Low';
    if (p < 0.66) return 'Medium';
    return 'High';
  }

  unitHint(k: string): string {
    if (k.includes('[K]')) return 'Kelvin';
    if (k.includes('[rpm]')) return 'Revolutions per minute';
    if (k.includes('[Nm]')) return 'Newton-metre';
    if (k.includes('[min]')) return 'Minutes';
    return '';
  }
}
