import { useState, useCallback } from 'react'
import { makeAuthenticatedRequest } from '../utils/api'

/**
 * Custom hook for fetching Flowable API data
 * Handles loading, error states, and data management
 */
export const useFetchFlowable = () => {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetch = useCallback(async (endpoint, options = {}) => {
    setLoading(true)
    setError(null)
    try {
      const response = await makeAuthenticatedRequest(endpoint, {
        method: options.method || 'GET',
        headers: options.headers || { 'Content-Type': 'application/json' },
        body: options.body ? JSON.stringify(options.body) : undefined
      })

      if (response.ok) {
        const json = await response.json()
        // Flowable returns data in 'data' property
        const fetchedData = json.data || []
        setData(Array.isArray(fetchedData) ? fetchedData : [])
        return { success: true, data: fetchedData }
      } else {
        const errorMsg = options.errorMessage || `Failed to fetch from ${endpoint}`
        setError(errorMsg)
        setData([])
        return { success: false, error: errorMsg }
      }
    } catch (err) {
      const errorMsg = `Connection error: ${err.message}`
      setError(errorMsg)
      setData([])
      console.error(`Error fetching ${endpoint}:`, err)
      return { success: false, error: errorMsg }
    } finally {
      setLoading(false)
    }
  }, [])

  const reset = useCallback(() => {
    setData([])
    setLoading(false)
    setError(null)
  }, [])

  return {
    data,
    loading,
    error,
    fetch,
    reset,
    setData // Allow manual data updates
  }
}
