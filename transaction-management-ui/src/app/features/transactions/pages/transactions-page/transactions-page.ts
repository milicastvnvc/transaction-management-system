import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';

import { Transaction } from '../../../../core/models/transaction';
import { TransactionService } from '../../../../core/services/transaction.service';
import { AddTransactionDialog } from '../../components/add-transaction-dialog/add-transaction-dialog';
import { TransactionTable } from '../../components/transaction-table/transaction-table';
import { getApiErrorMessage } from '../../../../core/utils/api-error.util';

@Component({
  selector: 'app-transactions-page',
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule, TransactionTable],
  templateUrl: './transactions-page.html',
  styleUrl: './transactions-page.scss',
})
export class TransactionsPage implements OnInit {
  private readonly transactionService = inject(TransactionService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly transactions = signal<Transaction[]>([]);
  readonly isLoading = signal(false);
  readonly loadingError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.isLoading.set(true);
    this.loadingError.set(null);

    this.transactionService
      .getTransactions()
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        }),
      )
      .subscribe({
        next: (transactions) => {
          this.transactions.set(transactions);
        },
        error: (error) => {
          console.error('Failed to load transactions', error);

          this.loadingError.set(
            getApiErrorMessage(error, 'Transactions could not be loaded. Please try again.'),
          );
        },
      });
  }

  openAddTransactionDialog(): void {
    const dialogRef = this.dialog.open<AddTransactionDialog, undefined, Transaction>(
      AddTransactionDialog,
      {
        width: '540px',
        maxWidth: '95vw',
        disableClose: true,
        autoFocus: 'first-tabbable',
      },
    );

    dialogRef.afterClosed().subscribe((createdTransaction) => {
      if (!createdTransaction) {
        return;
      }

      this.transactions.update((currentTransactions) => [
        ...currentTransactions,
        createdTransaction,
      ]);

      this.snackBar.open('Transaction added successfully.', 'Close', {
        duration: 4000,
        horizontalPosition: 'end',
        verticalPosition: 'top',
      });
    });
  }
}
