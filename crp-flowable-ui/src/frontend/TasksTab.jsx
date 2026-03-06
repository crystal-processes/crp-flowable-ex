import { useState, useCallback, useMemo } from 'react'
import TaskItem from './components/TaskItem'
import PaginationControls from './components/PaginationControls'
import { useFilter } from './context/FilterContext'
import CONFIG from './config'

function TasksTab({ tasks, selectedTask, setSelectedTask, loading, fetchTasks, onNavigateToInstance, fetchProcessInstances }) {
  const [formSubmitMessage, setFormSubmitMessage] = useState(null)
  const {
    taskFilter,
    setTaskFilter,
    taskFilterType,
    setTaskFilterType,
    showFinishedTasks,
    setShowFinishedTasks,
    taskLimit
  } = useFilter()

  const handleFilterChange = useCallback((e) => {
    const newFilter = e.target.value
    setTaskFilter(newFilter)
    fetchTasks(showFinishedTasks, newFilter, taskFilterType)
  }, [setTaskFilter, fetchTasks, showFinishedTasks, taskFilterType])

  const handleFilterTypeChange = useCallback((e) => {
    const newType = e.target.value
    setTaskFilterType(newType)
    fetchTasks(showFinishedTasks, taskFilter, newType)
  }, [setTaskFilterType, fetchTasks, showFinishedTasks, taskFilter])

  const handleFormSubmit = useCallback((formData, result) => {
    setFormSubmitMessage('✅ Task form submitted successfully!')
    setSelectedTask(null)
    fetchTasks(showFinishedTasks, taskFilter, taskFilterType)
    if (fetchProcessInstances) {
      fetchProcessInstances()
    }
    const timer = setTimeout(() => setFormSubmitMessage(null), CONFIG.UI.ALERT_TIMEOUT)
    return () => clearTimeout(timer)
  }, [fetchTasks, fetchProcessInstances, showFinishedTasks, taskFilter, taskFilterType, setSelectedTask])

  // Memoize task list to prevent unnecessary re-renders
  const memoizedTasks = useMemo(() => tasks, [tasks])

  const handleFormClose = useCallback(() => {
    setSelectedTask(null)
  }, [setSelectedTask])

  const handleShowFinishedTasks = useCallback(() => {
    const newValue = !showFinishedTasks
    setShowFinishedTasks(newValue)
    setSelectedTask(null)
    fetchTasks(newValue, taskFilter, taskFilterType)
  }, [showFinishedTasks, setShowFinishedTasks, setSelectedTask, fetchTasks, taskFilter, taskFilterType])

  const handleClearFilter = useCallback(() => {
    setTaskFilter('')
    fetchTasks(showFinishedTasks, '', taskFilterType)
  }, [setTaskFilter, fetchTasks, showFinishedTasks, taskFilterType])

  return (
    <div className="tab-content">
      {formSubmitMessage && (
        <div className="alert alert-success">
          <strong>{formSubmitMessage}</strong>
          <button onClick={() => setFormSubmitMessage(null)} className="close-btn" aria-label="Close message">✕</button>
        </div>
      )}

      {/* Task Filter Input */}
      <div className="task-filter-section">
        <label htmlFor="finished-checkbox" className="filter-label">
          <input
            id="finished-checkbox"
            type="checkbox"
            name="showFinishedTasks"
            checked={showFinishedTasks}
            onChange={handleShowFinishedTasks}
            aria-label="finished"
          />
          <span> Finished</span>
        </label>

        <select
          value={taskFilterType}
          onChange={handleFilterTypeChange}
          className="task-filter-type-select"
          aria-label="Filter by task property"
        >
          <option value="name">🏷️ Task Name</option>
          <option value="businessKey">🔑 Business Key</option>
          <option value="processName">⚙️ Process Name</option>
          <option value="taskKey">📌 Task Definition Key</option>
        </select>

        <input
          type="text"
          placeholder="🔍 Enter search term..."
          value={taskFilter}
          onChange={handleFilterChange}
          className="task-filter-input"
          aria-label="Search tasks"
        />

        {taskFilter && (
          <button
            onClick={handleClearFilter}
            className="btn-clear-filter"
            title="Clear search filter"
            aria-label="Clear search filter"
          >
            ✕ Clear Filter
          </button>
        )}

          <PaginationControls
            currentCount={memoizedTasks.length}
            totalAvailable={memoizedTasks.length + (memoizedTasks.length >= taskLimit ? 1 : 0)}
          />
      </div>

      {loading && memoizedTasks.length === 0 ? (
        <p className="loading-message">⏳ Loading tasks from Flowable...</p>
      ) : memoizedTasks.length > 0 ? (
        <>
          <div className="tasks-list">
            {memoizedTasks.map((task) => (
              <TaskItem
                key={task.id}
                task={task}
                isSelected={selectedTask?.id === task.id}
                onSelect={() => setSelectedTask(task)}
                onDeselect={() => setSelectedTask(null)}
                onFormSubmit={handleFormSubmit}
                onFormClose={handleFormClose}
                onNavigateToInstance={onNavigateToInstance}
              />
            ))}
          </div>
        </>
      ) : (
        <p className="empty-message">✅ No tasks currently assigned. Great job!</p>
      )}
    </div>
  )
}

export default TasksTab
