import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize } from 'rxjs';

import { CreateTransactionRequest, Transaction } from '../../../../core/models/transaction';
import { TransactionService } from '../../../../core/services/transaction.service';
import { getApiErrorMessage, isValidationError } from '../../../../core/utils/api-error.util';
import {
  applyBackendValidationErrors,
  formatLocalDate,
  removeBackendError,
} from '../../utils/transaction-form.util';

@Component({
  selector: 'app-add-transaction-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule,
    MatFormField,
    MatLabel,
    MatError,
    MatInputModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatIconModule,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './add-transaction-dialog.html',
  styleUrl: './add-transaction-dialog.scss',
})
export class AddTransactionDialog {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly transactionService = inject(TransactionService);
  private readonly dialogRef = inject(MatDialogRef<AddTransactionDialog, Transaction>);

  readonly today = new Date();

  readonly form = this.formBuilder.group({
    transactionDate: this.formBuilder.control<Date | null>(null, Validators.required),
    accountNumber: ['', Validators.required],
    accountHolderName: ['', [Validators.required, Validators.minLength(2)]],
    amount: [
      0,
      [Validators.required, Validators.min(0.01), Validators.pattern(/^\d+(\.\d{1,2})?$/)],
    ],
  });

  readonly isSubmitting = signal(false);
  readonly submissionError = signal<string | null>(null);

  constructor() {
    this.registerBackendErrorCleanup();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();

    if (!rawValue.transactionDate) {
      return;
    }

    const request: CreateTransactionRequest = {
      transactionDate: formatLocalDate(rawValue.transactionDate),
      accountNumber: rawValue.accountNumber.trim(),
      accountHolderName: rawValue.accountHolderName.trim(),
      amount: rawValue.amount,
    };

    this.createTransaction(request);
  }

  private createTransaction(request: CreateTransactionRequest): void {
    this.isSubmitting.set(true);
    this.submissionError.set(null);

    this.transactionService
      .createTransaction(request)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: (createdTransaction) => {
          this.dialogRef.close(createdTransaction);
        },
        error: (error) => {
          this.handleSubmissionError(error);
        },
      });
  }

  private handleSubmissionError(error: unknown): void {
    console.error('Failed to create transaction', error);

    if (isValidationError(error)) {
      applyBackendValidationErrors(this.form, error.error);

      this.submissionError.set(error.error.message || 'Please correct the highlighted fields.');

      return;
    }

    this.submissionError.set(
      getApiErrorMessage(error, 'The transaction could not be added. Please try again.'),
    );
  }

  private registerBackendErrorCleanup(): void {
    Object.keys(this.form.controls).forEach((controlName) => {
      const control = this.form.get(controlName);

      control?.valueChanges.subscribe(() => {
        removeBackendError(control);
      });
    });
  }
}
