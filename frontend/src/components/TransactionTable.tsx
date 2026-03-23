import type { Transaction } from '../api/transactionApi'
import { EditIcon, TrashIcon } from './Icons'

interface Props {
  transactions: Transaction[]
  onEdit?: (t: Transaction) => void
  onDelete?: (id: number) => void
  compact?: boolean
}

function formatDate(dateStr: string) {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-CA', {
    month: 'short', day: 'numeric', year: 'numeric',
  })
}

function amountPresentation(transaction: Transaction) {
  if (transaction.type === 'INCOME') {
    return { sign: '+', className: 'text-success-600' }
  }
  if (transaction.type === 'EXPENSE') {
    return { sign: '-', className: 'text-danger-600' }
  }
  if (transaction.transferLeg === 'IN') {
    return { sign: '+', className: 'text-primary-600' }
  }
  return { sign: '-', className: 'text-primary-600' }
}

export default function TransactionTable({ transactions, onEdit, onDelete, compact = false }: Props) {
  if (transactions.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-14 text-center">
        <div className="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center mb-3">
          <span className="text-2xl">🧾</span>
        </div>
        <p className="text-sm font-medium text-gray-500">No transactions found</p>
        <p className="text-xs text-gray-400 mt-1">Adjust your filters or add a new transaction</p>
      </div>
    )
  }

  return (
    <div className="overflow-x-auto -mx-1">
      <table className="w-full text-sm min-w-[600px]">
        <thead>
          <tr className="border-b border-gray-100">
            <th className="text-left text-[11px] font-semibold text-gray-400 uppercase tracking-wider pb-3 pl-1 pr-4">Date</th>
            <th className="text-left text-[11px] font-semibold text-gray-400 uppercase tracking-wider pb-3 pr-4">Description</th>
            <th className="text-left text-[11px] font-semibold text-gray-400 uppercase tracking-wider pb-3 pr-4">Category</th>
            {!compact && <th className="text-left text-[11px] font-semibold text-gray-400 uppercase tracking-wider pb-3 pr-4">Account</th>}
            <th className="text-right text-[11px] font-semibold text-gray-400 uppercase tracking-wider pb-3 pr-1">Amount</th>
            {!compact && onEdit && <th className="pb-3 pr-1 w-16" />}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-50">
          {transactions.map((t) => {
            const amountUi = amountPresentation(t)
            return (
            <tr key={t.id} className="group hover:bg-gray-50/70 transition-colors">
              <td className="py-3.5 pl-1 pr-4 text-gray-500 whitespace-nowrap text-xs">
                {formatDate(t.transactionDate)}
              </td>
              <td className="py-3.5 pr-4">
                <p className="font-medium text-gray-900 truncate max-w-[180px]">
                  {t.merchant || t.description || '—'}
                </p>
                {t.merchant && t.description && (
                  <p className="text-xs text-gray-400 truncate max-w-[180px] mt-0.5">{t.description}</p>
                )}
              </td>
              <td className="py-3.5 pr-4">
                {t.type === 'TRANSFER' ? (
                  <span className="inline-flex items-center gap-1.5 text-xs bg-primary-50 text-primary-700 rounded-full px-2.5 py-1 font-medium whitespace-nowrap">
                    Transfer
                  </span>
                ) : t.category ? (
                  <span className="inline-flex items-center gap-1.5 text-xs bg-gray-100 text-gray-600 rounded-full px-2.5 py-1 font-medium whitespace-nowrap">
                    {t.category.icon}
                    {t.category.name}
                  </span>
                ) : (
                  <span className="text-gray-300 text-xs">—</span>
                )}
              </td>
              {!compact && (
                <td className="py-3.5 pr-4 text-gray-500 text-xs whitespace-nowrap">{t.accountName}</td>
              )}
              <td className="py-3.5 pr-1 text-right whitespace-nowrap">
                <span className={`font-semibold text-sm ${amountUi.className}`}>
                  {amountUi.sign}${Math.abs(t.amount).toLocaleString('en-CA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </td>
              {!compact && onEdit && (
                <td className="py-3.5 pr-1">
                  <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => onEdit(t)}
                      className="p-1.5 rounded-lg text-gray-400 hover:bg-primary-50 hover:text-primary-600 transition-colors"
                    >
                      <EditIcon size={13} />
                    </button>
                    {onDelete && (
                      <button
                        onClick={() => onDelete(t.id)}
                        className="p-1.5 rounded-lg text-gray-400 hover:bg-danger-50 hover:text-danger-600 transition-colors"
                      >
                        <TrashIcon size={13} />
                      </button>
                    )}
                  </div>
                </td>
              )}
            </tr>
          )})}
        </tbody>
      </table>
    </div>
  )
}
