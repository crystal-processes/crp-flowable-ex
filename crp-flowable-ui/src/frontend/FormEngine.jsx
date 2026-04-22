import { useState, useEffect, useRef, lazy, Suspense } from 'react'
import React from 'react'
import { makeAuthenticatedRequest } from './utils/api'
import './FormEngine.css'

function FormEngine({ taskId, onSubmit, onClose }) {
  const [component, setComponent] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [formData, setFormData] = useState({})
  const formRef = useRef(null)

  useEffect(() => {
    const loadFormComponent = async () => {
      setLoading(true)
      setError(null)
      try {
        // Fetch the form component code from the backend
        const response = await makeAuthenticatedRequest(
          `/process-api/runtime/tasks/${taskId}/form`
        )

        if (!response.ok) {
          throw new Error(`Failed to load form: ${response.statusText}`)
        }

        let componentCode = await response.text()

        if (!componentCode || componentCode.trim().length === 0) {
          throw new Error('Form component code is empty')
        }

        // Make React available globally for the dynamic component
        window.React = React

        // Create a dynamic module using a blob and import it
        const moduleBlob = new Blob([componentCode], { type: 'application/javascript' })
        const moduleUrl = URL.createObjectURL(moduleBlob)

        // Dynamically import the module
        const module = await import(moduleUrl)
        const FormModel = module.default

        if (!FormModel) {
          throw new Error('Component does not have a default export')
        }

        setComponent(() => FormModel)

        // Fetch task variables and populate formData
        await fetchTaskVariables()
      } catch (err) {
        console.error('Error loading form component:', err)
        setError(`Failed to load form: ${err.message}`)
        setComponent(null)
      } finally {
        setLoading(false)
      }
    }

    if (taskId) {
      loadFormComponent()
    }
  }, [taskId])

  const fetchTaskVariables = async () => {
    try {
      const response = await makeAuthenticatedRequest(
        `/process-api/runtime/tasks/${taskId}/variables`
      )

      if (response.ok) {
        const variables = await response.json()
        console.log('Task variables loaded:', variables)

        // Convert array of variables to object for form data
        if (Array.isArray(variables)) {
          const variablesObject = {}
          variables.forEach((variable) => {
            variablesObject[variable.name] = variable.value
          })
          setFormData(variablesObject)
          console.log('Form data populated from variables:', variablesObject)
        }
      } else if (response.status !== 404) {
        console.warn('Failed to fetch task variables:', response.statusText)
      }
    } catch (err) {
      console.warn('Error fetching task variables:', err.message)
      // Don't throw error, just log warning - form can still work without pre-populated data
    }
  }

  const handleFormDataChange = (newData) => {
    setFormData(prevData => ({
      ...prevData,
      ...newData
    }))
  }

  const handleSubmit = async (e) => {
    if (e) {
      e.preventDefault()
    }

    setSubmitting(true)
    setError(null)

    try {
      // Convert formData object to variables array format
      const variables = Object.entries(formData).map(([name, value]) => ({
        name: name,
        type: typeof value,
        value: value
      }))

      const response = await makeAuthenticatedRequest(
        `/process-api/runtime/tasks/${taskId}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            'action': 'complete',
            'outcome': 'submitted',
            'variables': variables
          })
        }
      )

      if (!response.ok) {
        throw new Error(`Form submission failed: ${response.statusText}`)
      }

      console.log('Form submitted successfully:', response)

      if (onSubmit) {
        onSubmit(formData, response)
      }

      // Redirect back to task list after successful submission
      setTimeout(() => {
        if (onClose) {
          onClose()
        }
      }, 500)
    } catch (err) {
      console.error('Error submitting form:', err)
      setError(`Submission failed: ${err.message}`)
    } finally {
      setSubmitting(false)
    }
  }

  const handleClose = () => {
    if (onClose) {
      onClose()
    }
  }

  if (loading) {
    return (
      <div className="form-engine-container">
        <div className="form-engine-loading">
          <p>⏳ Loading form component...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="form-engine-container">
        <div className="form-engine-error">
          <h3>⚠️ Error Loading Form</h3>
          <p>{error}</p>
          <button onClick={handleClose} className="form-engine-btn-close">
            Close
          </button>
        </div>
      </div>
    )
  }

  if (!component) {
    return (
      <div className="form-engine-container">
        <div className="form-engine-error">
          <h3>❌ No Form Available</h3>
          <p>The form component could not be loaded.</p>
          <button onClick={handleClose} className="form-engine-btn-close">
            Close
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="form-engine-container">
      <form ref={formRef} onSubmit={handleSubmit} className="form-engine-form">
        {component && (
          <Suspense fallback={<div className="form-engine-loading"><p>⏳ Loading form...</p></div>}>
            {React.createElement(component, {
              onDataChange: handleFormDataChange,
              formData: formData,
              taskId: taskId
            })}
          </Suspense>
        )}
        <div className="form-engine-actions">
          <button
            type="submit"
            disabled={submitting}
            className="form-engine-btn-submit"
          >
            {submitting ? '⏳ Submitting...' : '✅ Submit'}
          </button>
          <button
            type="button"
            onClick={handleClose}
            disabled={submitting}
            className="form-engine-btn-cancel"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}

export default FormEngine
