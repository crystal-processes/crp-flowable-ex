import { useState, useCallback } from 'react'
import { makeAuthenticatedRequest } from './utils/api'
import CONFIG from './config'

function ProcessesTab({ processes, loading, error, setError, successMessage, setSuccessMessage, fetchProcessInstances, fetchTasks }) {
  const [selectedProcess, setSelectedProcess] = useState(null)
  const [showStartForm, setShowStartForm] = useState(false)
  const [businessKey, setBusinessKey] = useState('')
  const [startingProcess, setStartingProcess] = useState(false)

  const startProcessInstance = useCallback(async (e) => {
    e.preventDefault()

    if (!selectedProcess) {
      setError('Please select a process')
      return
    }

    setStartingProcess(true)
    setError(null)
    setSuccessMessage(null)

    try {
      const requestBody = {
        processDefinitionKey: selectedProcess.key,
        businessKey: businessKey || undefined
      }

      const response = await makeAuthenticatedRequest(CONFIG.ENDPOINTS.PROCESS_INSTANCES, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })

      if (response.ok) {
        const result = await response.json()
        console.log('Process instance started:', result)
        setSuccessMessage(`✅ Process "${selectedProcess.name}" started successfully! Instance ID: ${result.id}`)

        // Reset form
        setBusinessKey('')
        setSelectedProcess(null)
        setShowStartForm(false)

        // Refetch process instances
        if (fetchProcessInstances) {
          fetchProcessInstances()
        }
        if (fetchTasks) {
          fetchTasks()
        }
      } else {
        const errorData = await response.json()
        setError(`Failed to start process: ${errorData.message || 'Unknown error'}`)
      }
    } catch (error) {
      console.error('Error starting process:', error)
      setError(`Connection error: ${error.message}`)
    } finally {
      setStartingProcess(false)
    }
  }, [selectedProcess, businessKey, setError, setSuccessMessage, fetchProcessInstances, fetchTasks])

  return (
    <div className="tab-content">

      {!showStartForm ? (
        <div>
          {loading && processes.length === 0 ? (
            <p className="loading-message">⏳ Loading processes from Flowable...</p>
          ) : processes.length > 0 ? (
            <div>
              <div className="processes-list" role="region" aria-label="Available processes">
                {processes.map((process) => (
                  <div
                    key={process.id}
                    className={`process-item ${selectedProcess?.id === process.id ? 'selected' : ''}`}
                    onClick={() => setSelectedProcess(process)}
                    role="button"
                    tabIndex={0}
                    aria-pressed={selectedProcess?.id === process.id}
                    aria-label={`Process: ${process.name}`}
                  >
                    <h4>{process.name}</h4>
                    <p className="process-id">Key: {process.key}</p>
                    <p className="process-version">Version: {process.version}</p>
                  </div>
                ))}
              </div>
              {selectedProcess && (
                <button
                  onClick={() => setShowStartForm(true)}
                  className="btn-primary"
                  style={{ marginTop: '15px' }}
                >
                  🚀 Start Process Instance
                </button>
              )}
            </div>
          ) : (
            <p className="empty-message">ℹ️ No processes available</p>
          )}
        </div>
      ) : (
        <form onSubmit={startProcessInstance} className="start-process-form">
          <div className="form-group">
            <label htmlFor="processName"><strong>Selected Process:</strong></label>
            <p className="selected-value">{selectedProcess.name} ({selectedProcess.key})</p>
          </div>

          <div className="form-group">
            <label htmlFor="businessKey">
              <strong>Business Key (Optional):</strong>
            </label>
            <input
              type="text"
              id="businessKey"
              value={businessKey}
              onChange={(e) => setBusinessKey(e.target.value)}
              placeholder="Enter a unique business key (e.g., claim-123, order-456)"
              className="form-input"
            />
            <small>Used to identify the process instance in your business context</small>
          </div>

          <div className="form-actions">
            <button
              type="submit"
              disabled={startingProcess}
              className="btn-primary"
            >
              {startingProcess ? '⏳ Starting...' : '✅ Start Process'}
            </button>
            <button
              type="button"
              onClick={() => {
                setShowStartForm(false)
                setSelectedProcess(null)
                setBusinessKey('')
              }}
              className="btn-secondary"
              aria-label="Cancel process start"
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

export default ProcessesTab
