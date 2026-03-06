/**
 * Utility function to make authenticated API requests with session cookies
 * Uses Spring Security's HTTP Session (JSESSIONID cookie)
 */
export const authenticatedFetch = (url, options = {}) => {
  const fetchOptions = {
    ...options,
    credentials: 'include' // Include session cookie
  };

  return fetch(url, fetchOptions);
};

/**
 * Make a request that requires authentication
 * If 401 is returned, redirect to login
 */
export const makeAuthenticatedRequest = async (url, options = {}) => {
  const response = await authenticatedFetch(url, options);

  if (response.status === 401) {
    // Not authenticated - redirect to login
    window.location.href = '/login';
  }

  return response;
};
