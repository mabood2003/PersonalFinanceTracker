import { useEffect, useState } from 'react'
import { accountApi, type Account, type CreateAccountRequest } from '../api/accountApi'
import AccountCard from '../components/AccountCard'
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

const ACCOUNT_TYPE_OPTIONS: { value: Account['accountType']; label: string; icon: string }[] = [
  { value: 'CHECKING',     label: 'Checking',     icon: '🏦' },
  { value: 'SAVINGS',      label: 'Savings',      icon: '💰' },
  { value: 'CREDIT_CARD',  label: 'Credit Card',  icon: '💳' },
  { value: 'CASH',         label: 'Cash',         icon: '💵' },
  { value: 'INVESTMENT',   label: 'Investment',   icon: '📈' },
]

function netWorthContribution(account: Account) {
  return account.accountType === 'CREDIT_CARD'
    ? -Math.abs(account.balance)
    : account.balance
}

export default function AccountsPage() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Account | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null)

  useEffect(() => { accountApi.getAll().then(setAccounts) }, [])

  async function handleCreate(form: CreateAccountRequest) {
    await accountApi.create(form)
    setShowForm(false)
    setAccounts(await accountApi.getAll())
  }

  async function handleUpdate(form: CreateAccountRequest) {
    if (!editing) return
    await accountApi.update(editing.id, form)
    setEditing(null)
    setAccounts(await accountApi.getAll())
  }

  async function handleDelete(id: number) {
    await accountApi.delete(id)
    setDeleteConfirm(null)
    setAccounts(prev => prev.filter(a => a.id !== id))
  }

  const totalBalance = accounts.reduce((sum, a) => sum + netWorthContribution(a), 0)
  const positiveBalance = totalBalance >= 0

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Accounts</h1>
          <p className="text-sm text-gray-500 mt-0.5">{accounts.length} account{accounts.length !== 1 ? 's' : ''} linked</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary text-sm flex items-center gap-2">
          <PlusIcon size={16} />
          <span className="hidden sm:inline">Add Account</span>
          <span className="sm:hidden">Add</span>
        </button>
      </div>

      {/* Net Worth summary */}
      {accounts.length > 0 && (
        <div className="bg-gradient-to-br from-navy-800 to-navy-900 rounded-2xl p-6 text-white relative overflow-hidden">
          <div className="absolute top-0 right-0 w-48 h-48 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3 pointer-events-none" />
          <p className="text-navy-300 text-xs font-semibold uppercase tracking-wider mb-1">Total Net Worth</p>
          <p className={`text-4xl font-bold tracking-tight mb-4 ${!positiveBalance ? 'text-danger-400' : ''}`}>
            {totalBalance.toLocaleString('en-CA', { style: 'currency', currency: 'CAD', maximumFractionDigits: 0 })}
          </p>
          <div className="flex flex-wrap gap-2">
            {ACCOUNT_TYPE_OPTIONS.map(opt => {
              const count = accounts.filter(a => a.accountType === opt.value).length
              if (!count) return null
              return (
                <span key={opt.value} className="text-xs bg-white/10 text-white/80 px-3 py-1 rounded-full">
                  {opt.icon} {count} {opt.label}{count !== 1 ? 's' : ''}
                </span>
              )
            })}
          </div>
        </div>
      )}

      {/* Account cards */}
      {accounts.length === 0 ? (
        <div className="card p-12 text-center">
          <div className="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center mx-auto mb-4 text-2xl">🏦</div>
          <p className="font-medium text-gray-500">No accounts yet</p>
          <p className="text-sm text-gray-400 mt-1 mb-5">Add your bank accounts to start tracking your finances</p>
          <button onClick={() => setShowForm(true)} className="btn-primary text-sm mx-auto flex items-center gap-2 w-fit">
            <PlusIcon size={15} />
            Add your first account
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {accounts.map(a => (
            <AccountCard key={a.id} account={a} onEdit={setEditing} onDelete={setDeleteConfirm} />
          ))}
        </div>
      )}

      {showForm && (
        <Modal title="Add Account" onClose={() => setShowForm(false)}>
          <AccountForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </Modal>
      )}

      {editing && (
        <Modal title="Edit Account" onClose={() => setEditing(null)}>
          <AccountForm initial={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}

      {deleteConfirm !== null && (
        <Modal title="Delete Account" onClose={() => setDeleteConfirm(null)}>
          <div className="text-center py-2">
            <div className="w-12 h-12 rounded-full bg-danger-50 flex items-center justify-center mx-auto mb-4">
              <span className="text-2xl">🗑️</span>
            </div>
            <p className="text-gray-700 text-sm mb-1 font-medium">Are you sure?</p>
            <p className="text-gray-400 text-sm mb-6">This will delete the account and all associated transactions.</p>
            <div className="flex gap-3">
              <button onClick={() => handleDelete(deleteConfirm)} className="btn-danger flex-1 text-sm">Delete</button>
              <button onClick={() => setDeleteConfirm(null)} className="btn-secondary flex-1 text-sm">Cancel</button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}

function AccountForm({ initial, onSubmit, onCancel }: {
  initial?: Account
  onSubmit: (req: CreateAccountRequest) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState<CreateAccountRequest>({
    name: initial?.name ?? '',
    accountType: initial?.accountType ?? 'CHECKING',
    balance: initial?.balance ?? 0,
    currency: initial?.currency ?? 'CAD',
  })
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try { await onSubmit(form) } finally { setLoading(false) }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="form-label">Account Name</label>
        <input type="text" required maxLength={100}
          className="form-input"
          placeholder="e.g. TD Everyday Chequing"
          value={form.name}
          onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
      </div>
      <div>
        <label className="form-label">Account Type</label>
        <div className="grid grid-cols-3 gap-2">
          {ACCOUNT_TYPE_OPTIONS.map(opt => (
            <button
              key={opt.value}
              type="button"
              onClick={() => setForm(f => ({ ...f, accountType: opt.value }))}
              className={`flex flex-col items-center gap-1 p-3 rounded-xl border-2 text-xs font-medium transition-all
                ${form.accountType === opt.value
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-gray-200 text-gray-500 hover:border-gray-300'}`}
            >
              <span className="text-lg">{opt.icon}</span>
              {opt.label}
            </button>
          ))}
        </div>
      </div>
      <div>
        <label className="form-label">{initial ? 'Current Balance' : 'Initial Balance'}</label>
        <div className="relative">
          <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-medium text-sm">$</span>
          <input type="number" step="0.01" required
            className="form-input pl-8"
            placeholder="0.00"
            value={form.balance || ''}
            onChange={e => setForm(f => ({ ...f, balance: parseFloat(e.target.value) }))} />
        </div>
      </div>
      <div>
        <label className="form-label">Currency</label>
        <select className="form-input" value={form.currency}
          onChange={e => setForm(f => ({ ...f, currency: e.target.value }))}>
          <option value="CAD">CAD — Canadian Dollar</option>
          <option value="USD">USD — US Dollar</option>
          <option value="EUR">EUR — Euro</option>
          <option value="GBP">GBP — British Pound</option>
        </select>
      </div>
      <div className="flex gap-3 pt-2">
        <button type="submit" disabled={loading} className="btn-primary flex-1 text-sm">
          {loading ? 'Saving...' : initial ? 'Update Account' : 'Add Account'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary flex-1 text-sm">Cancel</button>
      </div>
    </form>
  )
}
