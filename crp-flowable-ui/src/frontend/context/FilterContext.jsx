import React, { createContext, useContext, useState, useCallback } from 'react'
import CONFIG from '../config'

export const FilterContext = createContext()

export const FilterProvider = ({ children }) => {
  const [taskFilter, setTaskFilter] = useState('')
  const [taskFilterType, setTaskFilterType] = useState(CONFIG.UI.TASK_FILTER_TYPE_DEFAULT)
  const [showFinishedTasks, setShowFinishedTasks] = useState(CONFIG.UI.SHOW_FINISHED_TASKS_DEFAULT)
  const [taskLimit, setTaskLimit] = useState(CONFIG.PAGINATION.DEFAULT_LIMIT)

  const resetFilters = useCallback(() => {
    setTaskFilter('')
    setTaskFilterType(CONFIG.UI.TASK_FILTER_TYPE_DEFAULT)
    setShowFinishedTasks(CONFIG.UI.SHOW_FINISHED_TASKS_DEFAULT)
    setTaskLimit(CONFIG.PAGINATION.DEFAULT_LIMIT)
  }, [])

  const value = {
    // Filter state
    taskFilter,
    setTaskFilter,
    taskFilterType,
    setTaskFilterType,
    showFinishedTasks,
    setShowFinishedTasks,
    taskLimit,
    setTaskLimit,
    // Actions
    resetFilters
  }

  return (
    <FilterContext.Provider value={value}>
      {children}
    </FilterContext.Provider>
  )
}

export const useFilter = () => {
  const context = useContext(FilterContext)
  if (!context) {
    throw new Error('useFilter must be used within a FilterProvider')
  }
  return context
}
