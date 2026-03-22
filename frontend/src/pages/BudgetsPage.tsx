import { useEffect, useState } from 'react'
import { budgetApi, type BudgetProgress, type CreateBudgetRequest } from '../api/budgetApi'
import { categoryApi } from '../api/categoryApi'
import type { Category } from '../api/transactionApi'
import BudgetCard from '../components/BudgetCard'
import { PlusIcon, CloseIcon } from '../components/Icons'

function Modal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-white w-full sm:rounded-2xl sm:max-w-lg shadow-modal overflow-y-auto">
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

export default function BudgetsPage() {
  const [budgets, setBudgets] = useState<BudgetProgress[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<BudgetProgress | null>(null)

  useEffect(() => {
    Promise.all([budgetApi.getAll(), categoryApi.getAll()]).then(([b, c]) => {
      setBudgets(b); setCategories(c)
    })
  }, [])

  async function handleCreate(form: CreateBudgetRequest) {
    await budgetApi.create(form)
    setShowForm(false)
    setBudgets(await budgetApi.getAll())
  }

  async function handleUpdate(form: CreateBudgetRequest) {
    if (!editing) return
    await budgetApi.update(editing.id, form)
    setEditing(null)
    setBudgets(await budgetApi.getAll())
  }

  async function handleDelete(id: number) {
    await budgetApi.delete(id)
    setBudgets(prev => prev.filter(b => b.id !== id))
  }

  const usedCategoryIds = new Set(budgets.map(b => b.category.id))
  const availableCategories = categories.filter(c => !usedCategoryIds.has(c.id))

  const exceeded = budgets.filter(b => b.status === 'EXCEEDED').length
  const warning  = budgets.filter(b => b.status === 'WARNING').length
  const onTrack  = budgets.filter(b => b.status === 'ON_TRACK').length

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Budgets</h1>
          <p className="text-sm text-gray-500 mt-0.5">{budgets.length} budget{budgets.length !== 1 ? 's' : ''} set</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary text-sm flex items-center gap-2">
          <PlusIcon size={16} />
          <span className="hidden sm:inline">Add Budget</span>
          <span className="sm:hidden">Add</span>
        </button>
      </div>

      {/* Summary bar */}
      {budgets.length > 0 && (
        <div className="grid grid-cols-3 gap-4">
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-success-600">{onTrack}</p>
            <p className="text-xs text-gray-400 font-medium mt-0.5 uppercase tracking-wide">On Track</p>
          </div>
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-warning-600">{warning}</p>
            <p className="text-xs text-gray-400 font-medium mt-0.5 uppercase tracking-wide">Warning</p>
          </div>
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-danger-600">{exceeded}</p>
            <p className="text-xs text-gray-400 font-medium mt-0.5 uppercase tracking-wide">Exceeded</p>
          </div>
        </div>
      )}

      {/* Budget cards */}
      {budgets.length === 0 ? (
        <div className="card p-12 text-center">
          <div className="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center mx-auto mb-4 text-2xl">🎯</div>
          <p className="font-medium text-gray-500">No budgets set yet</p>
          <p className="text-sm text-gray-400 mt-1 mb-5">Set spending limits to stay on track</p>
          <button onClick={() => setShowForm(true)} className="btn-primary text-sm mx-auto flex items-center gap-2 w-fit">
            <PlusIcon size={15} />
            Add your first budget
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {budgets.map(b => (
            <BudgetCard key={b.id} budget={b} onEdit={setEditing} onDelete={handleDelete} />
          ))}
        </div>
      )}

      {showForm && (
        <Modal title="Add Budget" onClose={() => setShowForm(false)}>
          <BudgetForm categories={availableCategories} onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </Modal>
      )}

      {editing && (
        <Modal title="Edit Budget" onClose={() => setEditing(null)}>
          <BudgetForm categories={categories} initial={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}
    </div>
  )
}

function BudgetForm({ categories, initial, onSubmit, onCancel }: {
  categories: Category[]
  initial?: BudgetProgress
  onSubmit: (req: CreateBudgetRequest) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState<CreateBudgetRequest>({
    categoryId: initial?.category.id ?? (categories[0]?.id ?? 0),
    amountLimit: initial?.amountLimit ?? 0,
    period: initial?.period ?? 'MONTHLY',
    startDate: initial?.startDate ?? new Date().toISOString().slice(0, 7) + '-01',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!form.categoryId) { setError('Please select a category.'); return }
    setLoading(true)
    setError('')
    try {
      await onSubmit(form)
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'An error occurred. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (categories.length === 0 && !initial) {
    return (
      <div className="text-center py-4">
        <p className="text-gray-500 text-sm mb-1 font-medium">All categories are budgeted</p>
        <p className="text-gray-400 text-sm mb-5">You've set a budget for every available category.</p>
        <button type="button" onClick={onCancel} className="btn-secondary text-sm">Close</button>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="flex items-start gap-2 px-4 py-3 bg-danger-50 border border-danger-100 rounded-xl text-sm text-danger-700">
          <span className="mt-0.5">⚠</span>
          {error}
        </div>
      )}
      <div>
        <label className="form-label">Category</label>
        <select className="form-input" value={form.categoryId}
          onChange={e => setForm(f => ({ ...f, categoryId: parseInt(e.target.value) }))}>
          {categories.map(c => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
        </select>
      </div>
      <div>
        <label className="form-label">Monthly Limit</label>
        <div className="relative">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-medium text-sm">$</span>
          <input type="number" min="0.01" step="0.01" required
            className="form-input pl-8"
            placeholder="0.00"
            value={form.amountLimit || ''}
            onChange={e => setForm(f => ({ ...f, amountLimit: parseFloat(e.target.value) }))} />
        </div>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="form-label">Period</label>
          <select className="form-input" value={form.period}
            onChange={e => setForm(f => ({ ...f, period: e.target.value as 'MONTHLY' | 'WEEKLY' }))}>
            <option value="MONTHLY">Monthly</option>
            <option value="WEEKLY">Weekly</option>
          </select>
        </div>
        <div>
          <label className="form-label">Start Date</label>
          <input type="date" required className="form-input" value={form.startDate}
            onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} />
        </div>
      </div>
      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
          {loading ? 'Saving...' : initial ? 'Update Budget' : 'Add Budget'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary flex-1 text-sm">Cancel</button>
      </div>
    </form>
  )
}
