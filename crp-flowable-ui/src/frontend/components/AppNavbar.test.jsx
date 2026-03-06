import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import AppNavbar from './AppNavbar'

describe('AppNavbar', () => {
  const defaultProps = {
    user: { username: 'admin' },
    activeTab: 'tasks',
    setActiveTab: vi.fn(),
    tasksCount: 5,
    instancesCount: 3,
    onTasksTabClick: vi.fn(),
    onInstancesTabClick: vi.fn(),
    onStatusTabClick: vi.fn(),
    onLogout: vi.fn()
  }

  it('renders navigation bar with user info', () => {
    render(<AppNavbar {...defaultProps} />)

    expect(screen.getByLabelText('Logout')).toBeInTheDocument()
  })

  it('displays task and instance counts', () => {
    render(<AppNavbar {...defaultProps} />)

    expect(screen.getByText(/Active Tasks/)).toBeInTheDocument()
    expect(screen.getByText(/Process Instances/)).toBeInTheDocument()
  })

  it('highlights active tab', () => {
    render(<AppNavbar {...defaultProps} activeTab="tasks" />)

    const tasksButton = screen.getByText(/Active Tasks/).closest('button')
    expect(tasksButton).toHaveClass('active')
  })

  it('calls setActiveTab on tab click', () => {
    const setActiveTab = vi.fn()
    render(<AppNavbar {...defaultProps} setActiveTab={setActiveTab} />)

    const instancesButton = screen.getByText(/Process Instances/)
    fireEvent.click(instancesButton)

    expect(setActiveTab).toHaveBeenCalledWith('instances')
  })

  it('calls onLogout when logout button is clicked', () => {
    const onLogout = vi.fn()
    render(<AppNavbar {...defaultProps} onLogout={onLogout} />)

    const logoutButton = screen.getByText('🚪 Logout')
    fireEvent.click(logoutButton)

    expect(onLogout).toHaveBeenCalled()
  })

  it('renders with alt text for logo', () => {
    render(<AppNavbar {...defaultProps} />)

    const logo = screen.getByAltText('CRP Flowable Logo')
    expect(logo).toBeInTheDocument()
  })
})
