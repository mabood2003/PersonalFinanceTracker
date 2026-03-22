import type { Account } from '../api/accountApi'
import { EditIcon, TrashIcon } from './Icons'

const typeConfig: Record<string, { label: string; gradient: string; textColor: string; accentColor: string }> = {
  CHECKING:    { label: 'Chequing',    gradient: 'from-navy-700 to-navy-900',        textColor: 'text-white',     accentColor: 'bg-white/20' },
  SAVINGS:     { label: 'Savings',     gradient: 'from-emerald-600 to-emerald-900',   textColor: 'text-white',     accentColor: 'bg-white/20' },
  CREDIT_CARD: { label: 'Credit Card', gradient: 'from-slate-700 to-slate-900',       textColor: 'text-white',     accentColor: 'bg-white/20' },
  CASH:        { label: 'Cash',        gradient: 'from-amber-500 to-amber-700',       textColor: 'text-white',     accentColor: 'bg-white/20' },
  INVESTMENT:  { label: 'Investment',  gradient: 'from-violet-600 to-violet-900',     textColor: 'text-white',     accentColor: 'bg-white/20' },
}

const typeIcons: Record<string, string> = {
  CHECKING: '🏦', SAVINGS: '💰', CREDIT_CARD: '💳', CASH: '💵', INVESTMENT: '📈',
}

interface Props {
  account: Account
  onEdit: (account: Account) => void
  onDelete: (id: number) => void
}

export default function AccountCard({ account, onEdit, onDelete }: Props) {
  const cfg = typeConfig[account.accountType] ?? typeConfig.CHECKING
  const isNegative = account.balance < 0

  return (
    <div className={`relative overflow-hidden rounded-2xl bg-gradient-to-br ${cfg.gradient} p-6 shadow-card-hover`}>
      {/* Background pattern */}
      <div className="absolute top-0 right-0 w-48 h-48 rounded-full bg-white/5 -translate-y-1/3 translate-x-1/3" />
      <div className="absolute bottom-0 left-0 w-32 h-32 rounded-full bg-white/5 translate-y-1/3 -translate-x-1/3" />

      {/* Header */}
      <div className="relative flex items-start justify-between mb-8">
        <div>
          <span className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full ${cfg.accentColor} text-white/80`}>
            <span>{typeIcons[account.accountType]}</span>
            {cfg.label}
          </span>
        </div>
        <div className="flex items-center gap-1.5">
          <button
            onClick={() => onEdit(account)}
            className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white/70 hover:text-white transition-colors"
            title="Edit account"
          >
            <EditIcon size={13} />
          </button>
          <button
            onClick={() => onDelete(account.id)}
            className="p-1.5 rounded-lg bg-white/10 hover:bg-red-500/40 text-white/70 hover:text-white transition-colors"
            title="Delete account"
          >
            <TrashIcon size={13} />
          </button>
        </div>
      </div>

      {/* Balance */}
      <div className="relative">
        <p className="text-white/60 text-xs font-medium uppercase tracking-wider mb-1">Current Balance</p>
        <p className={`text-3xl font-bold tracking-tight ${isNegative ? 'text-red-300' : 'text-white'}`}>
          {account.balance.toLocaleString('en-CA', { style: 'currency', currency: account.currency })}
        </p>
      </div>

      {/* Footer */}
      <div className="relative mt-5 pt-4 border-t border-white/10 flex items-center justify-between">
        <p className="text-white font-semibold text-sm truncate max-w-[70%]">{account.name}</p>
        <span className="text-white/50 text-xs font-mono">{account.currency}</span>
      </div>
    </div>
  )
}
