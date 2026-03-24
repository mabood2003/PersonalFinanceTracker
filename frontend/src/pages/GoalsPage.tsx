import { useEffect, useState } from 'react'
import { goalApi, type Goal, type CreateGoalRequest, type GoalStatus } from '../api/goalApi'
import { PlusIcon, EditIcon, TrashIcon, CloseIcon } from '../components/Icons'

const GOAL_ICONS = ['🎯', '🏠', '🚗', '✈️', '📚', '💍', '🏖️', '💻', '🏋️', '🎓', '🌍', '💰']
const GOAL_COLORS = [
  { value: 'blue',   label: 'Blue',   ring: 'ring-blue-500',   bar: 'bg-blue-500',   bg: 'bg-blue-50',   text: 'text-blue-700' },
  { value: 'green',  label: 'Green',  ring: 'ring-green-500',  bar: 'bg-green-500',  bg: 'bg-green-50',  text: 'text-green-700' },
  { value: 'purple', label: 'Purple', ring: 'ring-purple-500', bar: 'bg-purple-500', bg: 'bg-purple-50', text: 'text-purple-700' },
  { value: 'orange', label: 'Orange', ring: 'ring-orange-500', bar: 'bg-orange-500', bg: 'bg-orange-50', text: 'text-orange-700' },
  { value: 'pink',   label: 'Pink',   ring: 'ring-pink-500',   bar: 'bg-pink-500',   bg: 'bg-pink-50',   text: 'text-pink-700' },
  { value: 'teal',   label: 'Teal',   ring: 'ring-teal-500',   bar: 'bg-teal-500',   bg: 'bg-teal-50',   text: 'text-teal-700' },
]

function getColor(value: string) {
  return GOAL_COLORS.find(c => c.value === value) ?? GOAL_COLORS[0]
}

function fmt(n: number) {
  return n.toLocaleString('en-CA', { style: 'currency', currency: 'CAD', maximumFractionDigits: 0 })
}

function daysUntil(dateStr?: string) {
  if (!dateStr) return null
  const diff = Math.ceil((new Date(dateStr).getTime() - Date.now()) / 86400000)
  return diff
}

function Modal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-white w-full sm:rounded-2xl sm:max-w-lg shadow-modal overflow-y-auto max-h-[90vh]">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">{title}</h3>
          <button onClick={onClose} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
            <CloseIcon size={16} />
          </button>
        </div>
        <div className="px-6 py-5">{children}</div>
      </div>
    </div>
  )
}

function GoalForm({ initial, onSubmit, onCancel }: {
  initial?: Goal
  onSubmit: (req: CreateGoalRequest) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState<CreateGoalRequest>({
    name: initial?.name ?? '',
    description: initial?.description ?? '',
    targetAmount: initial?.targetAmount ?? 0,
    currentAmount: initial?.currentAmount ?? 0,
    targetDate: initial?.targetDate ?? '',
    icon: initial?.icon ?? '🎯',
    color: initial?.color ?? 'blue',
    status: initial?.status,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await onSubmit({
        ...form,
        targetDate: form.targetDate || undefined,
        description: form.description || undefined,
      })
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'An error occurred. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="flex items-start gap-2 px-4 py-3 bg-red-50 border border-red-100 rounded-xl text-sm text-red-700">
          ⚠ {error}
        </div>
      )}

      {/* Icon picker */}
      <div>
        <label className="form-label">Icon</label>
        <div className="flex flex-wrap gap-2">
          {GOAL_ICONS.map(icon => (
            <button
              key={icon}
              type="button"
              onClick={() => setForm(f => ({ ...f, icon }))}
              className={`w-10 h-10 rounded-xl text-lg flex items-center justify-center border-2 transition-all
                ${form.icon === icon ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300'}`}
            >
              {icon}
            </button>
          ))}
        </div>
      </div>

      {/* Color picker */}
      <div>
        <label className="form-label">Color</label>
        <div className="flex gap-2">
          {GOAL_COLORS.map(c => (
            <button
              key={c.value}
              type="button"
              onClick={() => setForm(f => ({ ...f, color: c.value }))}
              className={`w-8 h-8 rounded-full ${c.bar} ring-2 ring-offset-2 transition-all
                ${form.color === c.value ? c.ring : 'ring-transparent'}`}
            />
          ))}
        </div>
      </div>

      <div>
        <label className="form-label">Goal Name</label>
        <input
          type="text" required maxLength={100} className="form-input"
          placeholder="e.g. Emergency Fund, New Car..."
          value={form.name}
          onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
        />
      </div>

      <div>
        <label className="form-label">Description <span className="text-gray-400 font-normal">(optional)</span></label>
        <input
          type="text" maxLength={255} className="form-input"
          placeholder="What are you saving for?"
          value={form.description ?? ''}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="form-label">Target Amount</label>
          <div className="relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
            <input
              type="number" min="0.01" step="0.01" required className="form-input pl-8"
              placeholder="0.00"
              value={form.targetAmount || ''}
              onChange={e => setForm(f => ({ ...f, targetAmount: parseFloat(e.target.value) }))}
            />
          </div>
        </div>
        <div>
          <label className="form-label">Amount Saved So Far</label>
          <div className="relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
            <input
              type="number" min="0" step="0.01" className="form-input pl-8"
              placeholder="0.00"
              value={form.currentAmount || ''}
              onChange={e => setForm(f => ({ ...f, currentAmount: parseFloat(e.target.value) || 0 }))}
            />
          </div>
        </div>
      </div>

      <div>
        <label className="form-label">Target Date <span className="text-gray-400 font-normal">(optional)</span></label>
        <input
          type="date" className="form-input"
          value={form.targetDate ?? ''}
          onChange={e => setForm(f => ({ ...f, targetDate: e.target.value }))}
        />
      </div>

      {initial && (
        <div>
          <label className="form-label">Status</label>
          <select
            className="form-input"
            value={form.status ?? initial.status}
            onChange={e => setForm(f => ({ ...f, status: e.target.value as GoalStatus }))}
          >
            <option value="IN_PROGRESS">In Progress</option>
            <option value="ACHIEVED">Achieved</option>
            <option value="ABANDONED">Abandoned</option>
          </select>
        </div>
      )}

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
          {loading ? 'Saving...' : initial ? 'Update Goal' : 'Create Goal'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary flex-1 text-sm">Cancel</button>
      </div>
    </form>
  )
}

export default function GoalsPage() {
  const [goals, setGoals] = useState<Goal[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Goal | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<Goal | null>(null)

  useEffect(() => {
    goalApi.getAll().then(setGoals).catch(console.error).finally(() => setLoading(false))
  }, [])

  async function handleCreate(req: CreateGoalRequest) {
    const created = await goalApi.create(req)
    setGoals(prev => [created, ...prev])
    setShowForm(false)
  }

  async function handleUpdate(req: CreateGoalRequest) {
    if (!editing) return
    const updated = await goalApi.update(editing.id, req)
    setGoals(prev => prev.map(g => g.id === updated.id ? updated : g))
    setEditing(null)
  }

  async function handleDelete(id: number) {
    await goalApi.delete(id)
    setGoals(prev => prev.filter(g => g.id !== id))
    setDeleteConfirm(null)
  }

  const inProgress = goals.filter(g => g.status === 'IN_PROGRESS')
  const achieved = goals.filter(g => g.status === 'ACHIEVED')
  const totalSaved = goals.reduce((sum, g) => sum + g.currentAmount, 0)
  const totalTarget = goals.reduce((sum, g) => sum + g.targetAmount, 0)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Financial Goals</h1>
          <p className="text-sm text-gray-500 mt-0.5">Track your savings milestones</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary text-sm flex items-center gap-2">
          <PlusIcon size={16} />
          <span className="hidden sm:inline">New Goal</span>
          <span className="sm:hidden">Add</span>
        </button>
      </div>

      {/* Summary */}
      {goals.length > 0 && (
        <div className="grid grid-cols-3 gap-4">
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-primary-600">{inProgress.length}</p>
            <p className="text-xs text-gray-500 mt-1 uppercase tracking-wide font-medium">In Progress</p>
          </div>
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-success-600">{achieved.length}</p>
            <p className="text-xs text-gray-500 mt-1 uppercase tracking-wide font-medium">Achieved</p>
          </div>
          <div className="card p-4 text-center">
            <p className="text-xl font-bold text-gray-900">{fmt(totalSaved)}</p>
            <p className="text-xs text-gray-500 mt-1 uppercase tracking-wide font-medium">
              of {fmt(totalTarget)}
            </p>
          </div>
        </div>
      )}

      {/* Goals grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="card p-5 animate-pulse space-y-3">
              <div className="flex gap-3">
                <div className="w-12 h-12 rounded-2xl bg-gray-100" />
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-100 rounded w-3/4" />
                  <div className="h-3 bg-gray-100 rounded w-1/2" />
                </div>
              </div>
              <div className="h-2 bg-gray-100 rounded-full" />
            </div>
          ))}
        </div>
      ) : goals.length === 0 ? (
        <div className="card p-12 text-center">
          <div className="w-16 h-16 rounded-2xl bg-primary-50 flex items-center justify-center mx-auto mb-4 text-3xl">🎯</div>
          <p className="font-semibold text-gray-700">No goals yet</p>
          <p className="text-sm text-gray-400 mt-1 mb-5">Set a savings target and track your progress</p>
          <button onClick={() => setShowForm(true)} className="btn-primary text-sm mx-auto flex items-center gap-2 w-fit">
            <PlusIcon size={15} />
            Create your first goal
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {goals.map(goal => <GoalCard key={goal.id} goal={goal} onEdit={setEditing} onDelete={setDeleteConfirm} />)}
        </div>
      )}

      {showForm && (
        <Modal title="New Goal" onClose={() => setShowForm(false)}>
          <GoalForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </Modal>
      )}
      {editing && (
        <Modal title="Edit Goal" onClose={() => setEditing(null)}>
          <GoalForm initial={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}
      {deleteConfirm && (
        <Modal title="Delete Goal" onClose={() => setDeleteConfirm(null)}>
          <div className="text-center py-2">
            <div className="w-14 h-14 rounded-2xl bg-gray-50 flex items-center justify-center mx-auto mb-4 text-3xl">
              {deleteConfirm.icon}
            </div>
            <p className="font-medium text-gray-800 mb-1">Delete "{deleteConfirm.name}"?</p>
            <p className="text-sm text-gray-400 mb-6">This action cannot be undone.</p>
            <div className="flex gap-3">
              <button onClick={() => handleDelete(deleteConfirm.id)} className="btn-danger flex-1 text-sm">Delete</button>
              <button onClick={() => setDeleteConfirm(null)} className="btn-secondary flex-1 text-sm">Cancel</button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}

function GoalCard({ goal, onEdit, onDelete }: {
  goal: Goal
  onEdit: (g: Goal) => void
  onDelete: (g: Goal) => void
}) {
  const color = getColor(goal.color)
  const days = daysUntil(goal.targetDate)
  const pct = Math.min(goal.percentComplete, 100)
  const isAchieved = goal.status === 'ACHIEVED'
  const isAbandoned = goal.status === 'ABANDONED'

  return (
    <div className={`card p-5 flex flex-col gap-4 group relative ${isAbandoned ? 'opacity-60' : ''}`}>
      {/* Action buttons */}
      <div className="absolute top-4 right-4 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
        <button
          onClick={() => onEdit(goal)}
          className="w-7 h-7 rounded-lg bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-500 hover:text-gray-700 transition-colors"
        >
          <EditIcon size={13} />
        </button>
        <button
          onClick={() => onDelete(goal)}
          className="w-7 h-7 rounded-lg bg-gray-100 hover:bg-red-50 flex items-center justify-center text-gray-500 hover:text-red-500 transition-colors"
        >
          <TrashIcon size={13} />
        </button>
      </div>

      {/* Header */}
      <div className="flex items-start gap-3 pr-16">
        <div className={`w-12 h-12 rounded-2xl ${color.bg} flex items-center justify-center text-2xl flex-shrink-0`}>
          {goal.icon}
        </div>
        <div className="min-w-0">
          <p className="font-semibold text-gray-900 truncate">{goal.name}</p>
          {goal.description && (
            <p className="text-xs text-gray-400 mt-0.5 truncate">{goal.description}</p>
          )}
          {/* Status badge */}
          {isAchieved && (
            <span className="inline-flex items-center gap-1 text-xs font-medium text-success-700 bg-success-50 px-2 py-0.5 rounded-full mt-1">
              ✓ Achieved
            </span>
          )}
          {isAbandoned && (
            <span className="inline-flex items-center gap-1 text-xs font-medium text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full mt-1">
              Paused
            </span>
          )}
        </div>
      </div>

      {/* Progress bar */}
      <div>
        <div className="flex justify-between items-baseline mb-1.5">
          <span className="text-sm font-bold text-gray-900">{fmt(goal.currentAmount)}</span>
          <span className="text-xs text-gray-400">of {fmt(goal.targetAmount)}</span>
        </div>
        <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${isAchieved ? 'bg-success-500' : color.bar}`}
            style={{ width: `${pct}%` }}
          />
        </div>
        <div className="flex justify-between mt-1.5">
          <span className={`text-xs font-semibold ${isAchieved ? 'text-success-600' : color.text}`}>
            {pct.toFixed(0)}% complete
          </span>
          {!isAchieved && (
            <span className="text-xs text-gray-400">{fmt(goal.amountRemaining)} to go</span>
          )}
        </div>
      </div>

      {/* Deadline */}
      {goal.targetDate && !isAchieved && (
        <div className={`text-xs flex items-center gap-1.5 px-3 py-2 rounded-xl
          ${days !== null && days < 0 ? 'bg-red-50 text-red-600' :
            days !== null && days < 30 ? 'bg-orange-50 text-orange-600' : 'bg-gray-50 text-gray-500'}`}>
          <span>📅</span>
          {days === null ? '' :
            days < 0 ? `${Math.abs(days)} days overdue` :
            days === 0 ? 'Due today' :
            `${days} days remaining`}
        </div>
      )}
    </div>
  )
}
