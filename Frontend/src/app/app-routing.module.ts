import { Routes, RouterModule } from '@angular/router';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CockpitComponent } from './pages/cockpit/cockpit.component';
import { SimulatorComponent } from './pages/simulator/simulator.component';

const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,   // <— layout with sidebar + topbar
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'cockpit', component: CockpitComponent },
      { path: 'simulator', component: SimulatorComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ]
  },
  // IMPORTANT: do NOT also declare any top-level route to DashboardComponent here.
  // If you have something like { path: 'dashboard', component: DashboardComponent } at root level, remove it.
];

export const AppRoutingModule = RouterModule.forRoot(routes);
