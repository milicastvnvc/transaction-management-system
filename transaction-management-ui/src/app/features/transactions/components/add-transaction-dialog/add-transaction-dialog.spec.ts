import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddTransactionDialog } from './add-transaction-dialog';

describe('AddTransactionDialog', () => {
  let component: AddTransactionDialog;
  let fixture: ComponentFixture<AddTransactionDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTransactionDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(AddTransactionDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
