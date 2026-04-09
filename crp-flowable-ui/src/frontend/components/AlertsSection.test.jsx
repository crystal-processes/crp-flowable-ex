import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import AlertsSection from './AlertsSection'
import CONFIG from '../config'

describe('AlertsSection', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('renders nothing when no error or success message', () => {
    const { container } = render(
      <AlertsSection
        error={null}
        successMessage={null}
        onRetry={vi.fn()}
        onCloseSuccess={vi.fn()}
      />
    )

    expect(container.firstChild).toBeNull()
  })

  it('displays error message when error exists', () => {
    render(
      <AlertsSection
        error="Test error message"
        successMessage={null}
        onRetry={vi.fn()}
        onCloseSuccess={vi.fn()}
      />
    )

    expect(screen.getByText(/Test error message/)).toBeInTheDocument()
    expect(screen.getByText('Retry')).toBeInTheDocument()
  })

  it('displays success message', () => {
    render(
      <AlertsSection
        error={null}
        successMessage="Test success"
        onRetry={vi.fn()}
        onCloseSuccess={vi.fn()}
      />
    )

    expect(screen.getByText(/Test success/)).toBeInTheDocument()
  })

  it('calls onRetry when retry button is clicked', () => {
    const onRetry = vi.fn()
    render(
      <AlertsSection
        error="Test error"
        successMessage={null}
        onRetry={onRetry}
        onCloseSuccess={vi.fn()}
      />
    )

    fireEvent.click(screen.getByText('Retry'))
    expect(onRetry).toHaveBeenCalled()
  })

  it('auto-dismisses success message after timeout', () => {
    const onCloseSuccess = vi.fn()
    render(
      <AlertsSection
        error={null}
        successMessage="Test success"
        onRetry={vi.fn()}
        onCloseSuccess={onCloseSuccess}
      />
    )

    expect(onCloseSuccess).not.toHaveBeenCalled()

    vi.advanceTimersByTime(CONFIG.UI.ALERT_TIMEOUT)

    expect(onCloseSuccess).toHaveBeenCalled()
  })

  it('calls onCloseSuccess when close button is clicked', () => {
    const onCloseSuccess = vi.fn()
    render(
      <AlertsSection
        error={null}
        successMessage="Test success"
        onRetry={vi.fn()}
        onCloseSuccess={onCloseSuccess}
      />
    )

    const closeButton = screen.getByLabelText('Close success message')
    fireEvent.click(closeButton)

    expect(onCloseSuccess).toHaveBeenCalled()
  })
})
