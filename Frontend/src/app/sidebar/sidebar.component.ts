import { Component } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  menuItems: any[] = [
    { path: '/dashboard', icon: 'nc-icon nc-chart-bar-32', title: 'Dashboard' },
    //{ path: '/cockpit',   icon: 'nc-icon nc-tv-2',         title: 'Cockpit'   },
    //{ path: '/simulator', icon: 'nc-icon nc-settings-gear-65', title: 'Simulator' },
  ];
}

