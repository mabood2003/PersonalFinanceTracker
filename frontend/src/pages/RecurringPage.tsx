import { useEffect, useState } from 'react'
import { recurringApi, type RecurringTransaction, type CreateRecurringTransactionRequest, type RecurringFrequency, type TransactionType } from '../api/recurringApi'
import { accountApi, type Account } from '../api/accountApi'
import { categoryApi } from '../api/categoryApi'
import type { Category } from '../api/transactionApi'
import { PlusIcon, EditIcon, TrashIcon, CloseIcon } from '../components/Icons'

const FREQ_LABELS: Record<RecurringFrequency, string> = {
  DAILY: 'Daily', WEEKLY: 'Weekly', MONTHLY: 'Monthly', YEARLY: 'Yearly',
}
const FREQ_ICONS: Record<RecurringFrequency, string> = {
  DAILY: '📆', WEEKLY: '📅', MONTHLY: '🗓️', YEARLY: '📊',
}

function fmt(n: number) {
  return n.toLocaleString('en-CA', { style: 'currency', currency: 'CAD' })
}

function daysUntil(dateStr: string) {
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

function RecurringForm({ initial, accounts, categories, onSubmit, onCancel }: {
  initial?: RecurringTransaction
  accounts: Account[]
  categories: Category[]
  onSubmit: (req: CreateRecurringTransactionRequest) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState<CreateRecurringTransactionRequest>({
    accountId: initial?.accountId ?? (accounts[0]?.id ?? 0),
    destinationAccountId: initial?.destinationAccountId,
    categoryId: initial?.category?.id,
    type: initial?.type ?? 'EXPENSE',
    amount: initial?.amount ?? 0,
    description: initial?.description ?? '',
    merchant: initial?.merchant ?? '',
    frequency: initial?.frequency ?? 'MONTHLY',
    startDate: initial?.startDate ?? new Date().toISOString().slice(0, 10),
    endDate: initial?.endDate ?? '',
    active: initial?.active ?? true,
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const isTransfer = form.type === 'TRANSFER'

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await onSubmit({
        ...form,
        endDate: form.endDate || undefined,
        description: form.description || undefined,
        merchant: form.merchant || undefined,
        destinationAccountId: isTransfer ? form.destinationAccountId : undefined,
        categoryId: isTransfer ? undefined : form.categoryId,
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

      {/* Type */}
      <div>
        <label className="form-label">Type</label>
        <div className="grid grid-cols-3 gap-2 p-1 bg-gray-100 rounded-xl">
          {(['EXPENSE', 'INCOME', 'TRANSFER'] as TransactionType[]).map(type => (
            <button key={type} type="button"
              onClick={() => setForm(f => ({ ...f, type }))}
              className={`py-2 rounded-lg text-sm font-semibold transition-all ${
                form.type === type
                  ? type === 'INCOME' ? 'bg-success-600 text-white shadow-sm'
                  : type === 'EXPENSE' ? 'bg-danger-600 text-white shadow-sm'
                  : 'bg-primary-600 text-white shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'}`}
            >
              {type === 'INCOME' ? 'Income' : type === 'EXPENSE' ? 'Expense' : 'Transfer'}
            </button>
          ))}
        </div>
      </div>

      {/* Amount */}
      <div>
        <label className="form-label">Amount</label>
        <div className="relative">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
          <input type="number" min="0.01" step="0.01" required className="form-input pl-8"
            placeholder="0.00" value={form.amount || ''}
            onChange={e => setForm(f => ({ ...f, amount: parseFloat(e.target.value) || 0 }))} />
        </div>
      </div>

      {/* Frequency */}
      <div>
        <label className="form-label">Frequency</label>
        <div className="grid grid-cols-4 gap-2">
          {(['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'] as RecurringFrequency[]).map(freq => (
            <button key={freq} type="button"
              onClick={() => setForm(f => ({ ...f, frequency: freq }))}
              className={`flex flex-col items-center gap-1 py-2.5 rounded-xl border-2 text-xs font-medium transition-all
                ${form.frequency === freq
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-gray-200 text-gray-500 hover:border-gray-300'}`}
            >
              <span className="text-base">{FREQ_ICONS[freq]}</span>
              {FREQ_LABELS[freq]}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        {/* Start date */}
        <div>
          <label className="form-label">Start Date</label>
          <input type="date" required className="form-input"
            value={form.startDate}
            onChange={e => setForm(f => ({ ...f, startDate: e.target.value }))} />
        </div>
        {/* End date */}
        <div>
          <label className="form-label">End Date <span className="text-gray-400 font-normal">(optional)</span></label>
          <input type="date" className="form-input"
            value={form.endDate ?? ''}
            onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))} />
        </div>
      </div>

      {/* Account */}
      <div>
        <label className="form-label">{isTransfer ? 'From Account' : 'Account'}</label>
        <select className="form-input" value={form.accountId}
          onChange={e => setForm(f => ({ ...f, accountId: parseInt(e.target.value) }))}>
          {accounts.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
        </select>
      </div>

      {isTransfer && (
        <div>
          <label className="form-label">To Account</label>
          <select className="form-input" value={form.destinationAccountId ?? ''}
            onChange={e => setForm(f => ({ ...f, destinationAccountId: parseInt(e.target.value) || undefined }))}>
            <option value="">Select account</option>
            {accounts.filter(a => a.id !== form.accountId).map(a => (
              <option key={a.id} value={a.id}>{a.name}</option>
            ))}
          </select>
        </div>
      )}

      {!isTransfer && (
        <div>
          <label className="form-label">Category <span className="text-gray-400 font-normal">(optional)</span></label>
          <select className="form-input" value={form.categoryId ?? ''}
            onChange={e => setForm(f => ({ ...f, categoryId: e.target.value ? parseInt(e.target.value) : undefined }))}>
            <option value="">No category</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
          </select>
        </div>
      )}

      <div>
        <label className="form-label">Description <span className="text-gray-400 font-normal">(optional)</span></label>
        <input type="text" maxLength={255} className="form-input" placeholder="e.g. Monthly rent"
          value={form.description ?? ''}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
          {loading ? 'Saving...' : initial ? 'Update' : 'Create Rule'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary flex-1 text-sm">Cancel</button>
      </div>
    </form>
  )
}

export default function RecurringPage() {
  const [recurring, setRecurring] = useState<RecurringTransaction[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<RecurringTransaction | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<RecurringTransaction | null>(null)

  useEffect(() => {
    Promise.all([
      recurringApi.getAll(),
      accountApi.getAll(),
      categoryApi.getAll(),
    ]).then(([r, a, c]) => {
      setRecurring(r)
      setAccounts(a)
      setCategories(c)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  async function handleCreate(req: CreateRecurringTransactionRequest) {
    const created = await recurringApi.create(req)
    setRecurring(prev => [created, ...prev])
    setShowForm(false)
  }

  async function handleUpdate(req: CreateRecurringTransactionRequest) {
    if (!editing) return
    const updated = await recurringApi.update(editing.id, req)
    setRecurring(prev => prev.map(r => r.id === updated.id ? updated : r))
    setEditing(null)
  }

  async function handleDelete(id: number) {
    await recurringApi.delete(id)
    setRecurring(prev => prev.filter(r => r.id !== id))
    setDeleteConfirm(null)
  }

  const active = recurring.filter(r => r.active)
  const inactive = recurring.filter(r => !r.active)
  const monthlyTotal = active.reduce((sum, r) => {
    const multiplier = r.frequency === 'DAILY' ? 30 : r.frequency === 'WEEKLY' ? 4.33 : r.frequency === 'YEARLY' ? 1 / 12 : 1
    return sum + (r.type !== 'INCOME' ? r.amount * multiplier : 0)
  }, 0)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Recurring Transactions</h1>
          <p className="text-sm text-gray-500 mt-0.5">Automate bills, subscriptions & salary</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary text-sm flex items-center gap-2">
          <PlusIcon size={16} />
          <span className="hidden sm:inline">Add Rule</span>
          <span className="sm:hidden">Add</span>
        </button>
      </div>

      {/* Summary */}
      {recurring.length > 0 && (
        <div className="grid grid-cols-3 gap-4">
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-primary-600">{active.length}</p>
            <p className="text-xs text-gray-500 mt-1 uppercase tracking-wide font-medium">Active Rules</p>
          </div>
          <div className="card p-4 text-center">
            <p className="text-2xl font-bold text-gray-400">{inactive.length}</p>
            <p className="text-xs text-gray-500 mt-1 uppercase tracking-wide font-medium">Inactive</p>
          </div>
          <div className="card p-4 text-center">
            <p className="text-xl font-bold text-danger-600">~{fmt(monthlyTotal)}</p>
            <p className="text-xs text-gray-500 mt-1 uppercase tracking-wide font-medium">Est. Monthly Out</p>
          </div>
        </div>
      )}

      {/* List */}
      {loading ? (
        <div className="card divide-y divide-gray-50">
          {[1, 2, 3].map(i => (
            <div key={i} className="flex items-center gap-4 p-5 animate-pulse">
              <div className="w-11 h-11 rounded-xl bg-gray-100" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-100 rounded w-1/3" />
                <div className="h-3 bg-gray-100 rounded w-1/4" />
              </div>
              <div className="h-5 bg-gray-100 rounded w-20" />
            </div>
          ))}
        </div>
      ) : recurring.length === 0 ? (
        <div className="card p-12 text-center">
          <div className="w-16 h-16 rounded-2xl bg-primary-50 flex items-center justify-center mx-auto mb-4 text-3xl">🔄</div>
          <p className="font-semibold text-gray-700">No recurring rules yet</p>
          <p className="text-sm text-gray-400 mt-1 mb-5">Automate rent, subscriptions, or your paycheque</p>
          <button onClick={() => setShowForm(true)} className="btn-primary text-sm mx-auto flex items-center gap-2 w-fit">
            <PlusIcon size={15} />
            Create first rule
          </button>
        </div>
      ) : (
        <div className="card overflow-hidden">
          <div className="divide-y divide-gray-50">
            {recurring.map(r => (
              <RecurringRow
                key={r.id}
                item={r}
                onEdit={setEditing}
                onDelete={setDeleteConfirm}
              />
            ))}
          </div>
        </div>
      )}

      {showForm && (
        <Modal title="New Recurring Rule" onClose={() => setShowForm(false)}>
          <RecurringForm accounts={accounts} categories={categories} onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </Modal>
      )}
      {editing && (
        <Modal title="Edit Recurring Rule" onClose={() => setEditing(null)}>
          <RecurringForm initial={editing} accounts={accounts} categories={categories} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}
      {deleteConfirm && (
        <Modal title="Delete Rule" onClose={() => setDeleteConfirm(null)}>
          <div className="text-center py-2">
            <div className="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-4 text-2xl">🗑️</div>
            <p className="font-medium text-gray-800 mb-1">Delete this recurring rule?</p>
            <p className="text-sm text-gray-400 mb-6">
              "{deleteConfirm.description || deleteConfirm.accountName}" — {FREQ_LABELS[deleteConfirm.frequency]}
            </p>
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

function RecurringRow({ item, onEdit, onDelete }: {
  item: RecurringTransaction
  onEdit: (r: RecurringTransaction) => void
  onDelete: (r: RecurringTransaction) => void
}) {
  const days = daysUntil(item.nextRunDate)
  const isIncome = item.type === 'INCOME'
  const isTransfer = item.type === 'TRANSFER'

  return (
    <div className="flex items-center gap-4 px-5 py-4 hover:bg-gray-50 transition-colors group">
      {/* Frequency badge */}
      <div className={`w-11 h-11 rounded-xl flex items-center justify-center text-xl flex-shrink-0
        ${!item.active ? 'bg-gray-100' : isIncome ? 'bg-success-50' : isTransfer ? 'bg-primary-50' : 'bg-danger-50'}`}>
        {FREQ_ICONS[item.frequency]}
      </div>

      {/* Info */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <p className={`font-medium text-sm truncate ${!item.active ? 'text-gray-400' : 'text-gray-900'}`}>
            {item.description || (isTransfer ? `${item.accountName} → ${item.destinationAccountName}` : item.accountName)}
          </p>
          {!item.active && (
            <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full flex-shrink-0">Paused</span>
          )}
        </div>
        <div className="flex items-center gap-3 mt-0.5">
          <span className="text-xs text-gray-400">
            {FREQ_LABELS[item.frequency]} · {item.category ? `${item.category.icon} ${item.category.name}` : item.accountName}
          </span>
          {item.active && (
            <span className={`text-xs font-medium ${days <= 0 ? 'text-orange-500' : days <= 3 ? 'text-orange-400' : 'text-gray-400'}`}>
              {days <= 0 ? 'Due today' : `in ${days}d`}
            </span>
          )}
        </div>
      </div>

      {/* Amount */}
      <div className="text-right flex-shrink-0">
        <p className={`font-semibold text-sm ${isIncome ? 'text-success-600' : isTransfer ? 'text-primary-600' : 'text-danger-600'}`}>
          {isIncome ? '+' : isTransfer ? '↔' : '-'}{fmt(item.amount)}
        </p>
        <p className="text-xs text-gray-400">{FREQ_LABELS[item.frequency].toLowerCase()}</p>
      </div>

      {/* Actions */}
      <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0">
        <button
          onClick={() => onEdit(item)}
          className="w-7 h-7 rounded-lg bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-500 hover:text-gray-700"
        >
          <EditIcon size={13} />
        </button>
        <button
          onClick={() => onDelete(item)}
          className="w-7 h-7 rounded-lg bg-gray-100 hover:bg-red-50 flex items-center justify-center text-gray-500 hover:text-red-500"
        >
          <TrashIcon size={13} />
        </button>
      </div>
    </div>
  )
}
