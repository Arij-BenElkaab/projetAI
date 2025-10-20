import { Component, OnInit, inject } from '@angular/core';
import { ApiService, SchemaResponse, PredictResponse } from '../../core/api.service';
// Optional if you built the StateService earlier
// import { StateService } from '../../core/state.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  // private state = inject(StateService);

  keys: string[] = [];
  example: Record<string, number> = {};
  values: Record<string, number> = {};
  maxValues: Record<string, number> = {};

  mode: 'balanced' | 'high_recall' = 'balanced';
  thr: number | null = null;

  last: PredictResponse | null = null;
  sim: PredictResponse | null = null;
  baselineInputs: Record<string, number> | null = null;

  busy = false;
  errorMsg = '';

  ngOnInit(): void {
    this.api.getSchema().subscribe({
      next: (s: SchemaResponse) => {
        this.keys = s.required_json_keys;
        this.example = s.example || {};
        for (const k of this.keys) {
          this.values[k] = this.example[k] ?? 0;
          this.maxValues[k] = Math.max((this.example[k] ?? 100) * 2, 50);
        }
      },
      error: () => this.errorMsg = 'Failed to load schema.'
    });
  }

  applyPreset(kind: 'cool' | 'lower_torque' | 'new_tool') {
    const v = { ...this.values };
    for (const k of this.keys) {
      if (kind === 'cool') {
        if (k.includes('Air temperature')) v[k] -= 5;
        if (k.includes('Process temperature')) v[k] -= 5;
      }
      if (kind === 'lower_torque' && k.includes('Torque')) v[k] = Math.max(0, v[k] * 0.9);
      if (kind === 'new_tool' && k.includes('Tool wear')) v[k] = Math.max(0, v[k] - 20);
    }
    this.values = v;
  }

  predict() {
    this.errorMsg = '';
    this.busy = true;
    this.sim = null;

    this.api.predict(this.values, this.thr == null ? this.mode : undefined, this.thr ?? undefined)
      .subscribe({
        next: (res) => {
          this.last = res;
          this.baselineInputs = { ...this.values };
          // this.state?.setCockpitSnapshot(this.values, { probability: res.probability, prediction: res.prediction, threshold_used: res.threshold_used });
          // this.state?.saveToLocalStorage();
        },
        error: () => this.errorMsg = 'Prediction failed.'
      }).add(() => this.busy = false);
  }

  simulate() {
    if (!this.baselineInputs || !this.last) {
      this.errorMsg = 'Run Predict first to set a baseline.';
      return;
    }
    this.errorMsg = '';
    this.busy = true;

    this.api.predict(this.values, this.thr == null ? this.mode : undefined, this.thr ?? undefined)
      .subscribe({
        next: (res) => this.sim = res,
        error: () => this.errorMsg = 'Simulation failed.'
      }).add(() => this.busy = false);
  }

  deltaPP(): number | null {
    if (!this.last || !this.sim) return null;
    return Math.round(this.sim.probability * 100) - Math.round(this.last.probability * 100);
  }

  unit(k: string) {
    if (k.includes('[K]')) return 'K';
    if (k.includes('[rpm]')) return 'rpm';
    if (k.includes('[Nm]')) return 'Nm';
    if (k.includes('[min]')) return 'min';
    return '';
  }
  ///
  // Add these inside export class DashboardComponent { ... }

loadExample() {
  // move the spread out of the template
  this.values = { ...this.example };
}

// % values for gauges (avoids math in template)
get lastPct(): number {
  return Math.round((this.last ? this.last.probability : 0) * 100);
}
get simPct(): number {
  return Math.round((this.sim ? this.sim.probability : 0) * 100);
}

// inline style strings for CSS var --pct
lastStyle(): string {
  return `--pct:${this.lastPct}`;
}
simStyle(): string {
  return `--pct:${this.simPct}`;
}

// badge classes (avoid object/ternary noise in template)
riskBadge(): string {
  const high = this.last && this.last.prediction === 'panne_probable';
  return `badge ${high ? 'badge-risk-high' : 'badge-risk-low'}`;
}
simBadge(): string {
  const high = this.sim && this.sim.prediction === 'panne_probable';
  return `badge ${high ? 'badge-risk-high' : 'badge-risk-low'}`;
}

// Δ risk text color
deltaClass(): string {
  const d = this.deltaPP();
  if (d == null) return '';
  return d < 0 ? 'text-success' : d > 0 ? 'text-danger' : '';
}

}
