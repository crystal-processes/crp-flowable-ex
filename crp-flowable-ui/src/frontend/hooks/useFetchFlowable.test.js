import { describe, it, expect, beforeEach, vi } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useFetchFlowable } from './useFetchFlowable'

describe('useFetchFlowable', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('initializes with default state', () => {
    const { result } = renderHook(() => useFetchFlowable())

    expect(result.current.data).toEqual([])
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBe(null)
  })

  it('sets loading state during fetch', async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ data: [{ id: 1 }] })
      })
    )

    const { result } = renderHook(() => useFetchFlowable())

    await act(async () => {
      result.current.fetch('/test-endpoint')
    })

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.data).toEqual([{ id: 1 }])
  })

  it('handles errors gracefully', async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: false,
        status: 500
      })
    )

    const { result } = renderHook(() => useFetchFlowable())

    await act(async () => {
      const response = await result.current.fetch('/test-endpoint')
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
      expect(result.current.data).toEqual([])
    })
  })

  it('can reset state', async () => {
    const { result } = renderHook(() => useFetchFlowable())

    result.current.setData([{ id: 1 }])

    await act(async () => {
      result.current.reset()
    })

    expect(result.current.data).toEqual([])
    expect(result.current.error).toBe(null)
    expect(result.current.loading).toBe(false)
  })

  it('extracts data from response.data property', async () => {
    const mockData = [{ id: 1, name: 'Task 1' }]
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ data: mockData, total: 1 })
      })
    )

    const { result } = renderHook(() => useFetchFlowable())

    await act(async () => {
      await result.current.fetch('/test-endpoint')
    })

    expect(result.current.data).toEqual(mockData)
  })
})
