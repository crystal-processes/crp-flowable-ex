import React, { useEffect } from 'react'
import CONFIG from '../config'

function AlertsSection({
  error,
  successMessage,
  onRetry,
  onCloseSuccess
}) {
  // Auto-dismiss success message after timeout
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(onCloseSuccess, CONFIG.UI.ALERT_TIMEOUT)
      return () => clearTimeout(timer)
    }
  }, [successMessage, onCloseSuccess])

  if (!error && !successMessage) return null

  return (
    <div className="alerts-section" role="region" aria-live="polite">
      {error && (
        <div className="alert alert-error" role="alert">
          <strong>⚠️ Error:</strong> {error}
          <button
            onClick={onRetry}
            className="retry-btn"
            aria-label="Retry loading data"
          >
            Retry
          </button>
        </div>
      )}

      {successMessage && (
        <div className="alert alert-success" role="status">
          <strong>✅ Success:</strong> {successMessage}
          <button
            onClick={onCloseSuccess}
            className="close-btn"
            aria-label="Close success message"
            title="Close"
          >
            ✕
          </button>
        </div>
      )}
    </div>
  )
}

export default AlertsSection
