import { AbstractControl, FormGroup } from '@angular/forms';
import { ValidationError } from '../../../core/models/api-error';

export function formatLocalDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

export function applyBackendValidationErrors(form: FormGroup, response: ValidationError): void {
  Object.entries(response.fieldErrors).forEach(([fieldName, message]) => {
    const control = form.get(fieldName);

    if (!control) {
      return;
    }

    control.setErrors({
      ...control.errors,
      backend: message,
    });

    control.markAsTouched();
  });
}

export function removeBackendError(control: AbstractControl): void {
  if (!control.hasError('backend')) {
    return;
  }

  const errors = { ...control.errors };
  delete errors['backend'];

  control.setErrors(Object.keys(errors).length > 0 ? errors : null);
}
