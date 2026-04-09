import React, { useMemo } from 'react'

function AppNavbar({
  user,
  activeTab,
  setActiveTab,
  tasksCount,
  instancesCount,
  onTasksTabClick,
  onInstancesTabClick,
  onStatusTabClick,
  onLogout
}) {
  const tabButtons = useMemo(() => [
    {
      id: 'tasks',
      label: '📋 Active Tasks',
      count: tasksCount,
      onClick: onTasksTabClick
    },
    {
      id: 'instances',
      label: '🔄 Process Instances',
      count: instancesCount,
      onClick: onInstancesTabClick
    },
    {
      id: 'status',
      label: '📊 Status',
      count: null,
      onClick: onStatusTabClick
    }
  ], [tasksCount, instancesCount, onTasksTabClick, onInstancesTabClick, onStatusTabClick])

  return (
    <nav className="navbar" role="navigation" aria-label="Main navigation">
      <div className="navbar-content">
        <div className="navbar-title">
          <h2>
            <img src="/favicon.svg" alt="CRP Flowable Logo" width="64" height="64" />
          </h2>
        </div>

        <div className="navbar-tabs" role="tablist">
          {tabButtons.map((tab) => (
            <button
              key={tab.id}
              role="tab"
              aria-selected={activeTab === tab.id}
              aria-controls={`panel-${tab.id}`}
              className={`navbar-tab ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => {
                setActiveTab(tab.id)
                tab.onClick()
              }}
            >
              {tab.label}
              {tab.count !== null && ` (${tab.count})`}
            </button>
          ))}
        </div>

        <div className="navbar-status">
          <div className="user-info">
            <span className="user-name" aria-label="Current user">
              👤 {user?.username || 'User'}
            </span>
            <button
              onClick={onLogout}
              className="btn-logout"
              title="Logout from application"
              aria-label="Logout"
            >
              🚪 Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default AppNavbar
