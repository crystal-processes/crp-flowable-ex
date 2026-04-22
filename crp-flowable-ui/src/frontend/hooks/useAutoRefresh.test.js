import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useAutoRefresh } from './useAutoRefresh'

describe('useAutoRefresh', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  it('does not call callback when disabled', () => {
    const callback = vi.fn()

    renderHook(() => useAutoRefresh(false, callback, 1000))

    vi.runAllTimers()

    expect(callback).not.toHaveBeenCalled()
  })

  it('calls callback at specified interval when enabled', () => {
    const callback = vi.fn()

    renderHook(() => useAutoRefresh(true, callback, 1000))

    expect(callback).not.toHaveBeenCalled()

    vi.advanceTimersByTime(1000)
    expect(callback).toHaveBeenCalledTimes(1)

    vi.advanceTimersByTime(1000)
    expect(callback).toHaveBeenCalledTimes(2)
  })

  it('uses default interval of 10000ms', () => {
    const callback = vi.fn()

    renderHook(() => useAutoRefresh(true, callback))

    vi.advanceTimersByTime(10000)
    expect(callback).toHaveBeenCalledTimes(1)

    vi.advanceTimersByTime(10000)
    expect(callback).toHaveBeenCalledTimes(2)
  })

  it('clears interval on cleanup', () => {
    const callback = vi.fn()

    const { unmount } = renderHook(() => useAutoRefresh(true, callback, 1000))

    vi.advanceTimersByTime(1000)
    expect(callback).toHaveBeenCalledTimes(1)

    unmount()

    vi.advanceTimersByTime(1000)
    // Should still be 1, not 2, because interval was cleared
    expect(callback).toHaveBeenCalledTimes(1)
  })

  it('stops calling when enabled is changed to false', () => {
    const callback = vi.fn()

    const { rerender } = renderHook(
      ({ enabled }) => useAutoRefresh(enabled, callback, 1000),
      { initialProps: { enabled: true } }
    )

    vi.advanceTimersByTime(1000)
    expect(callback).toHaveBeenCalledTimes(1)

    rerender({ enabled: false })

    vi.advanceTimersByTime(1000)
    expect(callback).toHaveBeenCalledTimes(1)
  })
})
