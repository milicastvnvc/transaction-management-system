export type TransactionStatus = 'PENDING' | 'SETTLED' | 'FAILED';

export const STATUS_LABELS: Record<TransactionStatus, string> = {
  PENDING: 'Pending',
  SETTLED: 'Settled',
  FAILED: 'Failed'
};

export interface Transaction {
  transactionDate: string;
  accountNumber: string;
  accountHolderName: string;
  amount: number;
  status: TransactionStatus;
}

export interface CreateTransactionRequest {
  transactionDate: string;
  accountNumber: string;
  accountHolderName: string;
  amount: number;
}
