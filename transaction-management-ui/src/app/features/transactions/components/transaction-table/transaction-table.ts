import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { MatTableModule } from '@angular/material/table';

import { Transaction, STATUS_LABELS, TransactionStatus } from '../../../../core/models/transaction';

@Component({
  selector: 'app-transaction-table',
  imports: [
    MatTableModule,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './transaction-table.html',
  styleUrl: './transaction-table.scss'
})
export class TransactionTable {
  readonly transactions = input.required<Transaction[]>();

  readonly displayedColumns: string[] = [
    'transactionDate',
    'accountNumber',
    'accountHolderName',
    'amount',
    'status'
  ];

  getStatusLabel(status: TransactionStatus): string {
    return STATUS_LABELS[status];
  }
}
