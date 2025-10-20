import { Component, OnInit, inject } from '@angular/core';
import { ApiService, PredictResponse, SchemaResponse } from '../../core/api.service';

@Component({
  selector: 'app-simulator',
  templateUrl: './simulator.component.html',
  styleUrls: ['./simulator.component.scss']
})
export class SimulatorComponent implements OnInit {
  private api = inject(ApiService);

  keys: string[] = [];
  currentValues: Record<string, number> = {};
  simValues: Record<string, number> = {};
  maxValues: Record<string, number> = {};

  baselineLoaded = false;
  busy = false;
  errorMsg = '';

  currentRes: PredictResponse | null = null;
  whatIfRes: PredictResponse | null = null;

  ngOnInit(): void {
    this.api.getSchema().subscribe({
      next: (s: SchemaResponse) => {
        this.keys = s.required_json_keys;
        // Set arbitrary max values or calculate based on expected ranges
        for (const k of this.keys) {
          this.maxValues[k] = (s.example?.[k] ?? 100) * 2; // simple heuristic
        }
      },
      error: (err) => {
        this.errorMsg = 'Failed to load schema.';
        console.error(err);
      }
    });
  }

  loadCurrentState() {
    // Here we mimic pulling from Cockpit — in your real app you can use a shared service or API
    this.api.getSchema().subscribe({
      next: (s) => {
        this.currentValues = { ...s.example };
        this.simValues = { ...s.example };
        this.baselineLoaded = true;
      },
      error: (err) => {
        this.errorMsg = 'Failed to load baseline.';
        console.error(err);
      }
    });

    this.api.predict(this.currentValues).subscribe({
      next: (res) => (this.currentRes = res),
      error: (err) => console.error(err)
    });
  }

  applyPreset(kind: 'cool' | 'lower_torque' | 'new_tool') {
    if (!this.baselineLoaded) return;
    const s = { ...this.simValues };
    for (const k of this.keys) {
      if (kind === 'cool') {
        if (k.includes('Air temperature')) s[k] -= 5;
        if (k.includes('Process temperature')) s[k] -= 5;
      }
      if (kind === 'lower_torque' && k.includes('Torque')) {
        s[k] = Math.max(0, s[k] * 0.9);
      }
      if (kind === 'new_tool' && k.includes('Tool wear')) {
        s[k] = Math.max(0, s[k] - 20);
      }
    }
    this.simValues = s;
  }

  runSimulation() {
    this.busy = true;
    this.api.predict(this.simValues).subscribe({
      next: (res) => (this.whatIfRes = res),
      error: (err) => {
        this.errorMsg = 'Simulation failed.';
        console.error(err);
      }
    }).add(() => this.busy = false);
  }

  deltaPct(): number | null {
    if (!this.currentRes || !this.whatIfRes) return null;
    const a = Math.round(this.currentRes.probability * 100);
    const b = Math.round(this.whatIfRes.probability * 100);
    return b - a;
  }
}
