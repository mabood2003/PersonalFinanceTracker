import type { BudgetProgress } from '../api/budgetApi'
import { EditIcon, TrashIcon } from './Icons'

interface Props {
  budget: BudgetProgress
  onEdit: (budget: BudgetProgress) => void
  onDelete: (id: number) => void
}

export default function BudgetCard({ budget, onEdit, onDelete }: Props) {
  const pct = Math.min(budget.percentUsed, 100)
  const isExceeded = budget.status === 'EXCEEDED'
  const isWarning  = budget.status === 'WARNING'

  const barColor = isExceeded ? 'bg-danger-500' : isWarning ? 'bg-warning-500' : 'bg-success-500'
  const badgeClass = isExceeded
    ? 'badge badge-danger'
    : isWarning
    ? 'badge badge-warning'
    : 'badge badge-success'

  const badgeLabel = isExceeded ? 'Exceeded' : isWarning ? 'Warning' : 'On Track'

  return (
    <div className="card p-5 hover:shadow-card-hover transition-shadow">
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center text-xl flex-shrink-0">
            {budget.category.icon}
          </div>
          <div>
            <p className="font-semibold text-gray-900 text-sm">{budget.category.name}</p>
            <p className="text-xs text-gray-400 capitalize">{budget.period.toLowerCase()} budget</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <span className={badgeClass}>{badgeLabel}</span>
          <button
            onClick={() => onEdit(budget)}
            className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
          >
            <EditIcon size={13} />
          </button>
          <button
            onClick={() => onDelete(budget.id)}
            className="p-1.5 rounded-lg text-gray-400 hover:bg-danger-50 hover:text-danger-600 transition-colors"
          >
            <TrashIcon size={13} />
          </button>
        </div>
      </div>

      {/* Progress bar */}
      <div className="mb-3">
        <div className="flex items-center justify-between text-xs text-gray-500 mb-1.5">
          <span>{pct.toFixed(0)}% used</span>
          <span className={isExceeded ? 'text-danger-600 font-semibold' : ''}>
            {isExceeded
              ? `$${(budget.amountSpent - budget.amountLimit).toLocaleString()} over`
              : `$${budget.amountRemaining.toLocaleString()} left`}
          </span>
        </div>
        <div className="w-full bg-gray-100 rounded-full h-2">
          <div
            className={`h-2 rounded-full transition-all duration-500 ${barColor}`}
            style={{ width: `${pct}%` }}
          />
        </div>
      </div>

      {/* Amounts */}
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs text-gray-400">Spent</p>
          <p className={`text-sm font-semibold ${isExceeded ? 'text-danger-600' : 'text-gray-900'}`}>
            ${budget.amountSpent.toLocaleString()}
          </p>
        </div>
        <div className="h-6 w-px bg-gray-100" />
        <div className="text-right">
          <p className="text-xs text-gray-400">Limit</p>
          <p className="text-sm font-semibold text-gray-900">${budget.amountLimit.toLocaleString()}</p>
        </div>
      </div>
    </div>
  )
}
