import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { FilterProvider, useFilter } from './FilterContext'
import CONFIG from '../config'

describe('FilterContext', () => {
  it('provides default filter values', () => {
    const wrapper = ({ children }) => <FilterProvider>{children}</FilterProvider>
    const { result } = renderHook(() => useFilter(), { wrapper })

    expect(result.current.taskFilter).toBe('')
    expect(result.current.taskFilterType).toBe(CONFIG.UI.TASK_FILTER_TYPE_DEFAULT)
    expect(result.current.showFinishedTasks).toBe(CONFIG.UI.SHOW_FINISHED_TASKS_DEFAULT)
    expect(result.current.taskLimit).toBe(CONFIG.PAGINATION.DEFAULT_LIMIT)
  })

  it('allows setting task filter', () => {
    const wrapper = ({ children }) => <FilterProvider>{children}</FilterProvider>
    const { result } = renderHook(() => useFilter(), { wrapper })

    act(() => {
      result.current.setTaskFilter('test filter')
    })

    expect(result.current.taskFilter).toBe('test filter')
  })

  it('allows setting filter type', () => {
    const wrapper = ({ children }) => <FilterProvider>{children}</FilterProvider>
    const { result } = renderHook(() => useFilter(), { wrapper })

    act(() => {
      result.current.setTaskFilterType('businessKey')
    })

    expect(result.current.taskFilterType).toBe('businessKey')
  })

  it('allows toggling finished tasks', () => {
    const wrapper = ({ children }) => <FilterProvider>{children}</FilterProvider>
    const { result } = renderHook(() => useFilter(), { wrapper })

    const initial = result.current.showFinishedTasks

    act(() => {
      result.current.setShowFinishedTasks(!initial)
    })

    expect(result.current.showFinishedTasks).toBe(!initial)
  })

  it('allows setting task limit', () => {
    const wrapper = ({ children }) => <FilterProvider>{children}</FilterProvider>
    const { result } = renderHook(() => useFilter(), { wrapper })

    act(() => {
      result.current.setTaskLimit(100)
    })

    expect(result.current.taskLimit).toBe(100)
  })

  it('can reset filters to defaults', () => {
    const wrapper = ({ children }) => <FilterProvider>{children}</FilterProvider>
    const { result } = renderHook(() => useFilter(), { wrapper })

    // Change values
    act(() => {
      result.current.setTaskFilter('test')
      result.current.setTaskFilterType('businessKey')
      result.current.setShowFinishedTasks(true)
      result.current.setTaskLimit(200)
    })

    // Reset
    act(() => {
      result.current.resetFilters()
    })

    expect(result.current.taskFilter).toBe('')
    expect(result.current.taskFilterType).toBe(CONFIG.UI.TASK_FILTER_TYPE_DEFAULT)
    expect(result.current.showFinishedTasks).toBe(CONFIG.UI.SHOW_FINISHED_TASKS_DEFAULT)
    expect(result.current.taskLimit).toBe(CONFIG.PAGINATION.DEFAULT_LIMIT)
  })

  it('throws error when used outside provider', () => {
    expect(() => {
      renderHook(() => useFilter())
    }).toThrow('useFilter must be used within a FilterProvider')
  })
})
