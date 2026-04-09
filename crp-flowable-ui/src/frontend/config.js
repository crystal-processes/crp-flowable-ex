/**
 * Frontend configuration
 * Environment-specific settings for API endpoints, refresh intervals, etc.
 */

export const CONFIG = {
  // API Configuration
  API: {
    BASE_URL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    PROCESS_API_BASE: '/process-api',
    TIMEOUT: 30000 // ms
  },

  // Refresh & Polling
  REFRESH: {
    INTERVAL: 10000, // ms (10 seconds)
    AUTO_REFRESH_ENABLED: false
  },

  // Pagination
  PAGINATION: {
    DEFAULT_LIMIT: 50,
    MAX_LIMIT: 200,
    MIN_LIMIT: 10
  },

  // Flowable API Endpoints
  ENDPOINTS: {
    HISTORIC_TASK_INSTANCES: '/process-api/query/historic-task-instances',
    PROCESS_DEFINITIONS: '/process-api/repository/process-definitions',
    PROCESS_INSTANCES: '/process-api/runtime/process-instances',
    FORM_DATA: '/process-api/form/form-data',
    FORM_DEFINITION: '/process-api/form/form-definitions',
    SUBMIT_FORM: '/process-api/form/form-submit'
  },

  // UI Configuration
  UI: {
    SHOW_FINISHED_TASKS_DEFAULT: false,
    TASK_FILTER_TYPE_DEFAULT: 'name',
    ALERT_TIMEOUT: 3000, // ms
    TOAST_DURATION: 5000 // ms
  },

  // Feature Flags
  FEATURES: {
    AUTO_REFRESH: true,
    PAGINATION: true,
    FORM_VALIDATION: true,
    ANALYTICS: false
  }
}

export default CONFIG
