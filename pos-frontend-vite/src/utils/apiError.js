export function extractApiError(error) {
  const data = error?.response?.data || error;
  return {
    message: data?.message || 'Something went wrong',
    errors: Array.isArray(data?.errors) ? data.errors : [],
    status: error?.response?.status || data?.status,
  };
}

export function mapApiErrorsByField(errors) {
  if (!Array.isArray(errors)) {
    return {};
  }

  return errors.reduce((accumulator, current) => {
    if (current?.field) {
      accumulator[current.field] = current.message || 'Invalid value';
    }
    return accumulator;
  }, {});
}

export const getApiErrorMessage = (errorPayload) => {
  const normalizedError = extractApiError(errorPayload);
  const { status, message, errors } = normalizedError;

  if (status === 403) {
    return 'You do not have permission to perform this action.';
  }

  if (status === 404) {
    return 'Requested resource was not found.';
  }

  if (status === 422) {
    if (errors.length > 0) {
      return errors[0].message || 'Business validation failed.';
    }
    return message || 'Business validation failed.';
  }

  if (errors.length > 0) {
    return errors[0].message || 'Validation failed.';
  }

  if (message) {
    return message;
  }

  return 'Something went wrong. Please try again.';
};
