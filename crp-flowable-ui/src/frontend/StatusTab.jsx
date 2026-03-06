import ProcessesTab from './ProcessesTab'
import { useFilter } from './context/FilterContext'
import CONFIG from './config'

function StatusTab({
  tasks,
  processes,
  tasksLoading,
  processesLoading,
  autoRefresh,
  setAutoRefresh,
  fetchTasks,
  fetchProcessInstances,
  fetchProcesses,
  error,
  setError,
  successMessage,
  setSuccessMessage,
  statusSubTab,
  setStatusSubTab
}) {
  const { taskLimit, setTaskLimit } = useFilter()
  const loading = tasksLoading || processesLoading

  return (
    <div className="tab-content">
      {/* Sub-tabs navigation */}
      <div className="subtabs-nav" role="tablist">
        <button
          role="tab"
          aria-selected={statusSubTab === 'processes'}
          aria-controls="panel-processes"
          className={`subtab-button ${statusSubTab === 'processes' ? 'active' : ''}`}
          onClick={() => setStatusSubTab('processes')}
        >
          ⚙️ Process Definitions
        </button>
        <button
          role="tab"
          aria-selected={statusSubTab === 'system'}
          aria-controls="panel-system"
          className={`subtab-button ${statusSubTab === 'system' ? 'active' : ''}`}
          onClick={() => setStatusSubTab('system')}
        >
          🖥️ System
        </button>
      </div>

      {/* System Sub-tab */}
      {statusSubTab === 'system' && (
        <div id="panel-system" className="subtab-content" role="tabpanel" aria-labelledby="system-tab">
          <div className="status-overview">
            <div className="status-grid">
              <div className="status-card" role="status">
                <div className="status-icon">📋</div>
                <div className="status-info">
                  <p className="status-title">Active Tasks</p>
                  <p className="status-value">{tasks.length}</p>
                </div>
              </div>

              <div className="status-card" role="status">
                <div className="status-icon">⚙️</div>
                <div className="status-info">
                  <p className="status-title">Process Definitions</p>
                  <p className="status-value">{processes.length}</p>
                </div>
              </div>

              <div className="status-card" role="status" aria-live="polite">
                <div className="status-icon">{loading ? '⏳' : '✅'}</div>
                <div className="status-info">
                  <p className="status-title">System Status</p>
                  <p className="status-value">{loading ? 'Loading...' : 'Ready'}</p>
                </div>
              </div>

              <div className="status-card" role="status">
                <div className="status-icon">🔄</div>
                <div className="status-info">
                  <p className="status-title">Auto-Refresh</p>
                  <p className="status-value">{autoRefresh ? 'Enabled' : 'Disabled'}</p>
                </div>
              </div>
            </div>

            <div className="status-actions">
              <button
                onClick={() => {
                  fetchTasks()
                  fetchProcesses()
                }}
                disabled={loading}
                className="btn-primary"
                aria-label="Refresh system data"
              >
                {loading ? '⏳ Refreshing...' : '🔄 Refresh Now'}
              </button>

              <label className="checkbox-label">
                <input
                  type="checkbox"
                  checked={autoRefresh}
                  onChange={(e) => setAutoRefresh(e.target.checked)}
                  aria-label={`Auto-refresh every ${CONFIG.REFRESH.INTERVAL / 1000} seconds`}
                />
                Enable auto-refresh every {CONFIG.REFRESH.INTERVAL / 1000} seconds
              </label>

              <label htmlFor="task-limit-input" className="task-limit-label">
                <span>📊 Task Limit:</span>
                <input
                  id="task-limit-input"
                  type="number"
                  min={CONFIG.PAGINATION.MIN_LIMIT}
                  max={CONFIG.PAGINATION.MAX_LIMIT}
                  value={taskLimit}
                  onChange={(e) => {
                    const newLimit = parseInt(e.target.value, 10)
                    if (newLimit > 0) {
                      setTaskLimit(newLimit)
                    }
                  }}
                  className="task-limit-input"
                  aria-label="Set maximum number of tasks to display"
                />
              </label>
            </div>

        <div className="status-details">
          <h3>📈 Details</h3>
          <div className="details-grid">
            <div className="detail-item">
              <span className="detail-label">Total Tasks:</span>
              <span className="detail-value">{tasks.length}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Total Processes:</span>
              <span className="detail-value">{processes.length}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Last Refresh:</span>
              <span className="detail-value">{new Date().toLocaleTimeString()}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">System Health:</span>
              <span className="detail-value" style={{ color: '#4CAF50' }}>✅ Healthy</span>
            </div>
          </div>
        </div>

        <div className="info-card">
          <h3>💡 Information</h3>
          <ul>
            <li>Tasks are automatically loaded when the app starts</li>
            <li>Use the "Tasks" tab to view and manage your tasks</li>
            <li>Use the "Process Definitions" sub-tab to start new process instances</li>
            <li>Enable auto-refresh to keep data synchronized</li>
            <li>All data comes directly from the Flowable REST API</li>
          </ul>
        </div>
      </div>
        </div>
      )}

      {/* Process Definitions Sub-tab */}
      {statusSubTab === 'processes' && (
        <div id="panel-processes" className="subtab-content" role="tabpanel" aria-labelledby="processes-tab">
          <ProcessesTab
            processes={processes}
            loading={processesLoading}
            error={error}
            setError={setError}
            successMessage={successMessage}
            setSuccessMessage={setSuccessMessage}
            fetchProcessInstances={fetchProcessInstances}
            fetchTasks={fetchTasks}
          />
        </div>
      )}
    </div>
  )
}

export default StatusTab
