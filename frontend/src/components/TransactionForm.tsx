import { useState, useEffect } from 'react'
import type { Transaction, CreateTransactionRequest } from '../api/transactionApi'
import type { Account } from '../api/accountApi'
import type { Category } from '../api/transactionApi'

interface Props {
  accounts: Account[]
  categories: Category[]
  initial?: Transaction | null
  onSubmit: (req: CreateTransactionRequest) => Promise<void>
  onCancel: () => void
}

export default function TransactionForm({ accounts, categories, initial, onSubmit, onCancel }: Props) {
  const [form, setForm] = useState<CreateTransactionRequest>({
    accountId: initial?.accountId ?? (accounts[0]?.id ?? 0),
    categoryId: initial?.category?.id,
    type: initial?.type ?? 'EXPENSE',
    amount: initial?.amount ?? 0,
    description: initial?.description ?? '',
    merchant: initial?.merchant ?? '',
    transactionDate: initial?.transactionDate ?? new Date().toISOString().slice(0, 10),
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Sync accountId if accounts load after form mounts and no account was pre-selected
  useEffect(() => {
    if (!initial && !form.accountId && accounts[0]) {
      setForm(f => ({ ...f, accountId: accounts[0].id }))
    }
  }, [accounts])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!form.accountId) { setError('Please select an account.'); return }
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

  const incomeSelected = form.type === 'INCOME'

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="flex items-start gap-2 px-4 py-3 bg-danger-50 border border-danger-100 rounded-xl text-sm text-danger-700">
          <span className="mt-0.5">⚠</span>
          {error}
        </div>
      )}

      {/* Type toggle */}
      <div>
        <label className="form-label">Transaction Type</label>
        <div className="grid grid-cols-2 gap-2 p-1 bg-gray-100 rounded-xl">
          {(['EXPENSE', 'INCOME'] as const).map(type => (
            <button
              key={type}
              type="button"
              onClick={() => setForm(f => ({ ...f, type }))}
              className={`py-2 rounded-lg text-sm font-semibold transition-all ${
                form.type === type
                  ? type === 'INCOME'
                    ? 'bg-success-600 text-white shadow-sm'
                    : 'bg-danger-600 text-white shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              {type === 'INCOME' ? '↑ Income' : '↓ Expense'}
            </button>
          ))}
        </div>
      </div>

      {/* Amount */}
      <div>
        <label className="form-label">Amount</label>
        <div className="relative">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-medium text-sm">$</span>
          <input
            type="number" min="0.01" step="0.01" required
            className={`form-input pl-8 font-semibold text-base ${incomeSelected ? 'focus:ring-success-500' : 'focus:ring-danger-500'}`}
            placeholder="0.00"
            value={form.amount || ''}
            onChange={e => setForm(f => ({ ...f, amount: parseFloat(e.target.value) }))}
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        {/* Date */}
        <div>
          <label className="form-label">Date</label>
          <input
            type="date" required className="form-input"
            value={form.transactionDate}
            onChange={e => setForm(f => ({ ...f, transactionDate: e.target.value }))}
          />
        </div>

        {/* Account */}
        <div>
          <label className="form-label">Account</label>
          <select
            className="form-input"
            value={form.accountId}
            onChange={e => setForm(f => ({ ...f, accountId: parseInt(e.target.value) }))}
          >
            {accounts.map(a => (
              <option key={a.id} value={a.id}>{a.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Category */}
      <div>
        <label className="form-label">Category</label>
        <select
          className="form-input"
          value={form.categoryId ?? ''}
          onChange={e => setForm(f => ({ ...f, categoryId: e.target.value ? parseInt(e.target.value) : undefined }))}
        >
          <option value="">No category</option>
          {categories.map(c => (
            <option key={c.id} value={c.id}>{c.icon} {c.name}</option>
          ))}
        </select>
      </div>

      {/* Merchant */}
      <div>
        <label className="form-label">Merchant <span className="text-gray-400 font-normal">(optional)</span></label>
        <input
          type="text" className="form-input" placeholder="e.g. Tim Hortons"
          value={form.merchant ?? ''}
          onChange={e => setForm(f => ({ ...f, merchant: e.target.value }))}
        />
      </div>

      {/* Description */}
      <div>
        <label className="form-label">Description <span className="text-gray-400 font-normal">(optional)</span></label>
        <input
          type="text" className="form-input" placeholder="Add a note..."
          value={form.description ?? ''}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
        />
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
              </svg>
              Saving...
            </span>
          ) : initial ? 'Update Transaction' : 'Add Transaction'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary flex-1 text-sm">
          Cancel
        </button>
      </div>
    </form>
  )
}
