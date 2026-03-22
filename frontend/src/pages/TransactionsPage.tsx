import { useEffect, useState } from 'react'
import { transactionApi, type Transaction, type TransactionFilter } from '../api/transactionApi'
import { accountApi, type Account } from '../api/accountApi'
import { categoryApi } from '../api/categoryApi'
import type { Category } from '../api/transactionApi'
import TransactionTable from '../components/TransactionTable'
import TransactionForm from '../components/TransactionForm'
import { PlusIcon, SearchIcon, ChevronLeftIcon, ChevronRightIcon, CloseIcon } from '../components/Icons'

function Modal({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/50 backdrop-blur-sm">
      <div className="bg-white w-full sm:rounded-2xl sm:max-w-lg shadow-modal sm:max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white z-10">
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

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [filter, setFilter] = useState<TransactionFilter>({ size: 20, sort: 'transactionDate,desc' })
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Transaction | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    Promise.all([accountApi.getAll(), categoryApi.getAll()]).then(([a, c]) => {
      setAccounts(a); setCategories(c)
    })
  }, [])

  useEffect(() => { loadTransactions() }, [filter, page])

  async function loadTransactions() {
    setLoading(true)
    try {
      const res = await transactionApi.getAll({ ...filter, page })
      setTransactions(res.content)
      setTotal(res.totalElements)
      setTotalPages(res.totalPages)
    } finally {
      setLoading(false)
    }
  }

  async function handleCreate(req: Parameters<typeof transactionApi.create>[0]) {
    await transactionApi.create(req)
    setShowForm(false)
    loadTransactions()
  }

  async function handleUpdate(req: Parameters<typeof transactionApi.create>[0]) {
    if (!editing) return
    await transactionApi.update(editing.id, req)
    setEditing(null)
    loadTransactions()
  }

  async function handleDelete(id: number) {
    await transactionApi.delete(id)
    setDeleteConfirm(null)
    loadTransactions()
  }

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Transactions</h1>
          <p className="text-sm text-gray-500 mt-0.5">{total.toLocaleString()} total</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary text-sm flex items-center gap-2">
          <PlusIcon size={16} />
          <span className="hidden sm:inline">Add Transaction</span>
          <span className="sm:hidden">Add</span>
        </button>
      </div>

      {/* Filters */}
      <div className="card p-4">
        <div className="flex flex-wrap gap-3">
          <div className="relative flex-1 min-w-[160px]">
            <SearchIcon size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
            <input
              type="text"
              placeholder="Search transactions..."
              className="form-input pl-9 py-2 text-sm"
              onChange={e => { setPage(0); setFilter(f => ({ ...f, search: e.target.value || undefined })) }}
            />
          </div>
          <select
            className="form-input py-2 text-sm w-auto"
            onChange={e => { setPage(0); setFilter(f => ({ ...f, type: e.target.value || undefined })) }}
          >
            <option value="">All types</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
          <select
            className="form-input py-2 text-sm w-auto"
            onChange={e => { setPage(0); setFilter(f => ({ ...f, categoryId: e.target.value ? parseInt(e.target.value) : undefined })) }}
          >
            <option value="">All categories</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
          </select>
          <div className="flex items-center gap-2">
            <input
              type="date"
              className="form-input py-2 text-sm"
              onChange={e => { setPage(0); setFilter(f => ({ ...f, startDate: e.target.value || undefined })) }}
            />
            <span className="text-gray-400 text-sm">–</span>
            <input
              type="date"
              className="form-input py-2 text-sm"
              onChange={e => { setPage(0); setFilter(f => ({ ...f, endDate: e.target.value || undefined })) }}
            />
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="card p-6">
        {loading ? (
          <div className="space-y-3 py-4">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="flex gap-4 animate-pulse">
                <div className="h-4 bg-gray-100 rounded w-20 flex-shrink-0" />
                <div className="h-4 bg-gray-100 rounded flex-1" />
                <div className="h-4 bg-gray-100 rounded w-24" />
                <div className="h-4 bg-gray-100 rounded w-16" />
              </div>
            ))}
          </div>
        ) : (
          <TransactionTable
            transactions={transactions}
            onEdit={setEditing}
            onDelete={setDeleteConfirm}
          />
        )}

        {totalPages > 1 && (
          <div className="flex items-center justify-between mt-5 pt-4 border-t border-gray-100">
            <p className="text-sm text-gray-400">
              Page {page + 1} of {totalPages}
            </p>
            <div className="flex items-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="p-2 rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronLeftIcon size={16} />
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="p-2 rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronRightIcon size={16} />
              </button>
            </div>
          </div>
        )}
      </div>

      {showForm && (
        <Modal title="Add Transaction" onClose={() => setShowForm(false)}>
          <TransactionForm accounts={accounts} categories={categories} onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </Modal>
      )}

      {editing && (
        <Modal title="Edit Transaction" onClose={() => setEditing(null)}>
          <TransactionForm accounts={accounts} categories={categories} initial={editing} onSubmit={handleUpdate} onCancel={() => setEditing(null)} />
        </Modal>
      )}

      {deleteConfirm !== null && (
        <Modal title="Delete Transaction" onClose={() => setDeleteConfirm(null)}>
          <div className="text-center py-2">
            <div className="w-12 h-12 rounded-full bg-danger-50 flex items-center justify-center mx-auto mb-4">
              <span className="text-2xl">🗑️</span>
            </div>
            <p className="text-gray-700 text-sm mb-1 font-medium">Are you sure?</p>
            <p className="text-gray-400 text-sm mb-6">This will delete the transaction and reverse the account balance.</p>
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
