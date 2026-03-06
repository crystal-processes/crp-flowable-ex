import React, { useMemo } from 'react'
import { useFilter } from '../context/FilterContext'
import CONFIG from '../config'

function PaginationControls({ currentCount, totalAvailable }) {
  const { taskLimit, setTaskLimit } = useFilter()

  const limitOptions = useMemo(() => [
    CONFIG.PAGINATION.DEFAULT_LIMIT,
    Math.min(100, CONFIG.PAGINATION.MAX_LIMIT),
    CONFIG.PAGINATION.MAX_LIMIT
  ], [])

  const handleLimitChange = (e) => {
    const newLimit = parseInt(e.target.value)
    setTaskLimit(newLimit)
  }

  const canLoadMore = currentCount < totalAvailable

  return (
    <div className="pagination-controls" role="navigation" aria-label="Pagination">
      <div className="pagination-info">
        <span role="status" aria-live="polite">
          Showing {currentCount} of {totalAvailable} items
        </span>
      </div>

      <div className="pagination-actions">
        <label htmlFor="limit-select" className="limit-label">
          Items per page:
        </label>
        <select
          id="limit-select"
          value={taskLimit}
          onChange={handleLimitChange}
          className="limit-select"
          aria-label="Number of items to display per page"
        >
          {limitOptions.map(limit => (
            <option key={limit} value={limit}>
              {limit}
            </option>
          ))}
        </select>

        {canLoadMore && (
          <button
            className="btn-load-more"
            disabled={!canLoadMore}
            aria-label={`Load more items (currently showing ${currentCount})`}
          >
            📥 Load More ({totalAvailable - currentCount} remaining)
          </button>
        )}
      </div>
    </div>
  )
}

export default PaginationControls
