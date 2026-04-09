import { useEffect } from 'react'

/**
 * Custom hook for managing auto-refresh functionality
 * Handles interval creation/cleanup based on enabled state
 */
export const useAutoRefresh = (enabled, callback, interval = 10000) => {
  useEffect(() => {
    if (!enabled) return

    const intervalId = setInterval(callback, interval)

    return () => {
      if (intervalId) clearInterval(intervalId)
    }
  }, [enabled, callback, interval])
}
