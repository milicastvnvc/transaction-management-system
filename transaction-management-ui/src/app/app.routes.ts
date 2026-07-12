import { Routes } from '@angular/router';
import { TransactionsPage } from './features/transactions/pages/transactions-page/transactions-page';

export const routes: Routes = [
  {
    path: '',
    component: TransactionsPage
  },
  {
    path: '**',
    redirectTo: ''
  }
];
