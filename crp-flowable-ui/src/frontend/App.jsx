import { useState, useEffect, useCallback, useMemo } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import './App.css'
import TasksTab from './TasksTab'
import ProcessesTab from './ProcessesTab'
import StatusTab from './StatusTab'
import ProcessInstancesTab from './ProcessInstancesTab'
import AppNavbar from './components/AppNavbar'
import AlertsSection from './components/AlertsSection'
import { useAuth } from './AuthContext'
import { useFilter } from './context/FilterContext'
import { useFetchFlowable } from './hooks/useFetchFlowable'
import { useAutoRefresh } from './hooks/useAutoRefresh'
import { makeAuthenticatedRequest } from './utils/api'
import CONFIG from './config'

function App() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const {
    taskFilter,
    taskFilterType,
    showFinishedTasks,
    taskLimit
  } = useFilter()

  // Get active tab from URL, fallback to 'tasks'
  const initialTab = searchParams.get('tab') || 'tasks'
  const initialStatusSubTab = searchParams.get('status') || 'processes'

  // Separate loading states for different data sources
  const [tasksLoading, setTasksLoading] = useState(true)
  const [instancesLoading, setInstancesLoading] = useState(true)
  const [processesLoading, setProcessesLoading] = useState(false)

  // Data states
  const [tasks, setTasks] = useState([])
  const [processes, setProcesses] = useState([])
  const [processInstances, setProcessInstances] = useState([])

  // UI states
  const [selectedTask, setSelectedTask] = useState(null)
  const [autoRefresh, setAutoRefresh] = useState(CONFIG.REFRESH.AUTO_REFRESH_ENABLED)
  const [successMessage, setSuccessMessage] = useState(null)
  const [activeTab, setActiveTab] = useState(initialTab)
  const [statusSubTab, setStatusSubTab] = useState(initialStatusSubTab)
  const [error, setError] = useState(null)

  // Sync active tab with URL
  const handleTabChange = useCallback((newTab) => {
    setActiveTab(newTab)
    setSearchParams({ tab: newTab, status: statusSubTab })
  }, [setSearchParams, statusSubTab])

  // Sync status sub-tab with URL
  const handleStatusSubTabChange = useCallback((newSubTab) => {
    setStatusSubTab(newSubTab)
    setSearchParams({ tab: activeTab, status: newSubTab })
  }, [setSearchParams, activeTab])

  // Fetch tasks from Flowable REST API
  const fetchTasks = useCallback(async (finished = showFinishedTasks, filter = taskFilter, filterType = taskFilterType) => {
    setTasksLoading(true)
    setError(null)
    try {
      const requestBody = {
        start: 0,
        size: taskLimit,
        sort: 'startTime',
        order: 'desc',
        finished: finished
      }

      // Add filter parameter based on selected filter type
      if (filter && filter.trim()) {
        switch (filterType) {
          case 'name':
            requestBody.nameLikeIgnoreCase = '%' + filter.trim() + '%'
            break
          case 'businessKey':
            requestBody.processInstanceBusinessKeyLike = '%' + filter.trim() + '%'
            break
          case 'processName':
            requestBody.processDefinitionNameLike = '%' + filter.trim() + '%'
            break
          case 'taskKey':
            requestBody.taskDefinitionKeyLike = '%' + filter.trim() + '%'
            break
          default:
            requestBody.nameLikeIgnoreCase = '%' + filter.trim() + '%'
        }
      }

      const response = await makeAuthenticatedRequest(CONFIG.ENDPOINTS.HISTORIC_TASK_INSTANCES, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      })

      if (response.ok) {
        const json = await response.json()
        const tasksData = json.data || []
        setTasks(Array.isArray(tasksData) ? tasksData : [])
        console.log('Tasks loaded from Flowable API:', json)
      } else {
        const errorMsg = 'Failed to fetch tasks from Flowable'
        setError(errorMsg)
        setTasks([])
      }
    } catch (error) {
      console.error('Error fetching tasks:', error)
      const errorMsg = `Connection error: ${error.message}`
      setError(errorMsg)
      setTasks([])
    } finally {
      setTasksLoading(false)
    }
  }, [taskFilter, taskFilterType, showFinishedTasks, taskLimit])

  // Fetch process definitions from Flowable REST API
  const fetchProcesses = useCallback(async () => {
    setProcessesLoading(true)
    setError(null)
    try {
      const response = await makeAuthenticatedRequest(
        CONFIG.ENDPOINTS.PROCESS_DEFINITIONS + '?latest=true&sort=name&order=asc&start=0&size=50'
      )
      if (response.ok) {
        const json = await response.json()
        const processesData = json.data || []
        setProcesses(Array.isArray(processesData) ? processesData : [])
        console.log('Processes loaded from Flowable API:', json)
      } else {
        setError('Failed to fetch processes from Flowable')
        setProcesses([])
      }
    } catch (error) {
      console.error('Error fetching processes:', error)
      setError(`Connection error: ${error.message}`)
      setProcesses([])
    } finally {
      setProcessesLoading(false)
    }
  }, [])

  // Fetch process instances from Flowable REST API
  const fetchProcessInstances = useCallback(async () => {
    setInstancesLoading(true)
    setError(null)
    try {
      const response = await makeAuthenticatedRequest(
        CONFIG.ENDPOINTS.PROCESS_INSTANCES + '?sort=startTime&order=desc&start=0&size=50'
      )
      if (response.ok) {
        const json = await response.json()
        const instancesData = json.data || []
        setProcessInstances(Array.isArray(instancesData) ? instancesData : [])
        console.log('Process instances loaded from Flowable API:', json)
      } else {
        setError('Failed to fetch process instances')
        setProcessInstances([])
      }
    } catch (error) {
      console.error('Error fetching process instances:', error)
      setError(`Connection error: ${error.message}`)
      setProcessInstances([])
    } finally {
      setInstancesLoading(false)
    }
  }, [])

  // Auto-load tasks on component mount
  useEffect(() => {
    fetchTasks(showFinishedTasks, taskFilter, taskFilterType)
    fetchProcessInstances()
  }, [])

  // Auto-refresh based on active tab and settings
  useAutoRefresh(
    autoRefresh && activeTab === 'tasks',
    () => fetchTasks(showFinishedTasks, taskFilter, taskFilterType),
    CONFIG.REFRESH.INTERVAL
  )

  useAutoRefresh(
    autoRefresh && activeTab === 'instances',
    fetchProcessInstances,
    CONFIG.REFRESH.INTERVAL
  )

  useAutoRefresh(
    autoRefresh && activeTab === 'status',
    () => {
      fetchTasks(showFinishedTasks, taskFilter, taskFilterType)
      fetchProcesses()
    },
    CONFIG.REFRESH.INTERVAL
  )

  // Navigate to process instances tab
  const navigateToProcessInstance = useCallback((instanceId) => {
    setActiveTab('instances')
  }, [])

  // Overall loading state for backward compatibility
  const loading = useMemo(() => tasksLoading || instancesLoading, [tasksLoading, instancesLoading])

  // Memoize navbar props to prevent unnecessary re-renders
  const navbarProps = useMemo(() => ({
    user,
    activeTab,
    tasksCount: tasks.length,
    instancesCount: processInstances.length
  }), [user, activeTab, tasks.length, processInstances.length])

  return (
    <div className="app-layout">
      <main className="main-content">
        <AppNavbar
          user={user}
          activeTab={activeTab}
          setActiveTab={handleTabChange}
          tasksCount={tasks.length}
          instancesCount={processInstances.length}
          onTasksTabClick={() => fetchTasks(showFinishedTasks, taskFilter, taskFilterType)}
          onInstancesTabClick={fetchProcessInstances}
          onStatusTabClick={fetchProcesses}
          onLogout={() => {
            logout()
            navigate('/login')
          }}
        />

        <AlertsSection
          error={error}
          successMessage={successMessage}
          onRetry={() => fetchTasks(showFinishedTasks, taskFilter, taskFilterType)}
          onCloseSuccess={() => setSuccessMessage(null)}
        />

        <div className="tabs-content">
          {activeTab === 'tasks' && (
            <TasksTab
              tasks={tasks}
              selectedTask={selectedTask}
              setSelectedTask={setSelectedTask}
              loading={tasksLoading}
              fetchTasks={fetchTasks}
              fetchProcessInstances={fetchProcessInstances}
              onNavigateToInstance={navigateToProcessInstance}
            />
          )}

          {activeTab === 'instances' && (
            <ProcessInstancesTab
              loading={instancesLoading}
              error={error}
              setError={setError}
              processInstances={processInstances}
              fetchProcessInstances={fetchProcessInstances}
            />
          )}

          {activeTab === 'status' && (
            <StatusTab
              tasks={tasks}
              processes={processes}
              tasksLoading={tasksLoading}
              processesLoading={processesLoading}
              autoRefresh={autoRefresh}
              setAutoRefresh={setAutoRefresh}
              fetchTasks={fetchTasks}
              fetchProcessInstances={fetchProcessInstances}
              fetchProcesses={fetchProcesses}
              error={error}
              setError={setError}
              successMessage={successMessage}
              setSuccessMessage={setSuccessMessage}
              statusSubTab={statusSubTab}
              setStatusSubTab={handleStatusSubTabChange}
            />
          )}
        </div>
      </main>
    </div>
  )
}

export default App
