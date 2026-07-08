import React, { memo, useState } from 'react'
import FormEngine from '../FormEngine'
import { makeAuthenticatedRequest } from '../utils/api'

const TaskItem = memo(function TaskItem({
  task,
  isSelected,
  onSelect,
  onDeselect,
  onFormSubmit,
  onFormClose,
  onNavigateToInstance,
  onTaskComplete,
  fetchTasks
}) {
  const [completing, setCompleting] = useState(false)
  const [completeError, setCompleteError] = useState(null)

  const handleToggle = (e) => {
    if (isSelected) {
      e.stopPropagation()
      onDeselect()
    } else {
      onSelect()
    }
  }

  const handleNavigate = (e) => {
    e.stopPropagation()
    onNavigateToInstance(task.processInstanceId)
  }

  const handleCompleteTask = async (e) => {
    e.stopPropagation()
    setCompleting(true)
    setCompleteError(null)

    try {
      const response = await makeAuthenticatedRequest(
        `/process-api/runtime/tasks/${task.id}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            action: 'complete'
          })
        }
      )

      if (!response.ok) {
        throw new Error(`Failed to complete task: ${response.statusText}`)
      }

      console.log('Task completed successfully:', task.id)
      
      // Notify parent component if callback is provided
      if (onTaskComplete) {
        onTaskComplete(task)
      }
      
      // Refresh the task list
      if (fetchTasks) {
        fetchTasks()
      }
      
      // Close the task details after a short delay
      setTimeout(() => {
        if (onDeselect) {
          onDeselect()
        }
      }, 500)
    } catch (err) {
      console.error('Error completing task:', err)
      setCompleteError(err.message)
    } finally {
      setCompleting(false)
    }
  }

  return (
    <div
      className={`task-item ${isSelected ? 'selected' : ''}`}
      onClick={onSelect}
      role="button"
      tabIndex={0}
      aria-expanded={isSelected}
      aria-label={`Task: ${task.name}`}
    >
      <div className="task-header">
        <h3>{task.name}</h3>
        <span className="task-id">ID: {task.id}</span>
        {isSelected ? (
          <button
            onClick={handleToggle}
            className="toggle-details-btn toggle-expanded"
            title="Hide task details"
            aria-label="Hide task details"
          >
            ▲
          </button>
        ) : (
          <span className="toggle-details-icon" title="Show task details">
            ▼
          </span>
        )}
      </div>

      <div className="task-info">
        <p><strong>Assignee:</strong> {task.assignee || 'Unassigned'}</p>
        <p>
          <strong>Process:</strong>{' '}
          <button
            className="link-button"
            onClick={handleNavigate}
            title="Go to process instance details"
            aria-label={`Navigate to process instance ${task.processInstanceId}`}
          >
            {task.processDefinitionId} → {(task.processInstanceId) ? task.processInstanceId.substring(0, 8) : 'no processInstanceId'}...
          </button>
        </p>
        {task.dueDate && (
          <p><strong>Due Date:</strong> {new Date(task.dueDate).toLocaleDateString()}</p>
        )}
        {task.createTime && (
          <p><strong>Created:</strong> {new Date(task.createTime).toLocaleString()}</p>
        )}
      </div>

      {isSelected && (
        <div className="task-details-panel">
          {task.formKey ? (
            <FormEngine
              taskId={task.id}
              onSubmit={onFormSubmit}
              onClose={onFormClose}
            />
          ) : (
            <div className="no-form-message">
              <p>No form available for this task</p>
              {completeError && (
                <p className="complete-error">{completeError}</p>
              )}
              <button
                onClick={handleCompleteTask}
                disabled={completing}
                className="complete-task-btn"
              >
                {completing ? 'Completing...' : 'Complete Task'}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
})

export default TaskItem
