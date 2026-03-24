import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { analyticsApi, type MonthlySummary, type CategoryBreakdown, type SpendingTrend } from '../api/analyticsApi'
import { transactionApi, type Transaction } from '../api/transactionApi'
import { accountApi, type Account } from '../api/accountApi'
import { budgetApi, type BudgetProgress } from '../api/budgetApi'
import { netWorthApi, type NetWorthSnapshot } from '../api/netWorthApi'
import SpendingChart from '../components/SpendingChart'
import TrendChart from '../components/TrendChart'
import TransactionTable from '../components/TransactionTable'
import { TrendUpIcon, TrendDownIcon, ArrowRightIcon, WalletIcon } from '../components/Icons'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

function fmt(n: number) {
  return n.toLocaleString('en-CA', { style: 'currency', currency: 'CAD', maximumFractionDigits: 0 })
}

function netWorthContribution(account: Account) {
  return account.accountType === 'CREDIT_CARD'
    ? -Math.abs(account.balance)
    : account.balance
}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'Good morning'
  if (h < 17) return 'Good afternoon'
  return 'Good evening'
}

export default function DashboardPage() {
  const { user } = useAuth()
  const now = new Date()
  const [summary, setSummary] = useState<MonthlySummary | null>(null)
  const [breakdown, setBreakdown] = useState<CategoryBreakdown[]>([])
  const [trend, setTrend] = useState<SpendingTrend[]>([])
  const [recent, setRecent] = useState<Transaction[]>([])
  const [totalBalance, setTotalBalance] = useState(0)
  const [alertBudgets, setAlertBudgets] = useState<BudgetProgress[]>([])
  const [netWorthHistory, setNetWorthHistory] = useState<NetWorthSnapshot[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`
    const lastDay = new Date(year, month, 0).getDate()
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`

    Promise.all([
      analyticsApi.getMonthlySummary(year, month),
      analyticsApi.getCategoryBreakdown(startDate, endDate),
      analyticsApi.getSpendingTrend(6),
      transactionApi.getAll({ size: 6, sort: 'transactionDate,desc' }),
      accountApi.getAll(),
      budgetApi.getAll(),
      netWorthApi.getHistory(90),
    ]).then(([s, b, t, txns, accounts, budgets, nw]) => {
      setSummary(s)
      setBreakdown(b)
      setTrend(t)
      setRecent(txns.content)
      setTotalBalance(accounts.reduce((sum, a) => sum + netWorthContribution(a), 0))
      setAlertBudgets(budgets.filter(bud => bud.status !== 'ON_TRACK'))
      setNetWorthHistory(nw)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const monthName = now.toLocaleString('default', { month: 'long' })
  const netSavings = summary?.netSavings ?? 0
  const savingsPositive = netSavings >= 0

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <p className="text-sm text-gray-500">{getGreeting()},</p>
        <h1 className="text-2xl font-bold text-gray-900">{user?.firstName} {user?.lastName}</h1>
      </div>

      {/* Budget alerts */}
      {alertBudgets.length > 0 && (
        <div className="space-y-2">
          {alertBudgets.map(b => (
            <div key={b.id} className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm border
              ${b.status === 'EXCEEDED'
                ? 'bg-danger-50 border-danger-100 text-danger-700'
                : 'bg-warning-50 border-warning-100 text-warning-600'}`}>
              <span className="text-base">{b.category.icon}</span>
              <span>
                <strong>{b.category.name}</strong> budget {b.status === 'EXCEEDED'
                  ? `exceeded — $${(b.amountSpent - b.amountLimit).toLocaleString()} over limit`
                  : `at ${b.percentUsed.toFixed(0)}% — $${b.amountRemaining.toLocaleString()} remaining`}
              </span>
              <Link to="/budgets" className="ml-auto text-xs font-semibold underline underline-offset-2 flex-shrink-0">
                Review
              </Link>
            </div>
          ))}
        </div>
      )}

      {/* Net worth hero card */}
      <div className="bg-gradient-to-br from-navy-800 to-navy-900 rounded-2xl p-6 text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-56 h-56 bg-white/5 rounded-full -translate-y-1/3 translate-x-1/3 pointer-events-none" />
        <p className="text-navy-300 text-xs font-semibold uppercase tracking-wider mb-1">Total Balance</p>
        <p className="text-4xl font-bold tracking-tight mb-4">{fmt(totalBalance)}</p>
        <div className="flex items-center gap-2 text-sm">
          <WalletIcon size={14} className="text-navy-300" />
          <span className="text-navy-300">All accounts combined</span>
          <Link to="/accounts" className="ml-auto text-white/70 hover:text-white flex items-center gap-1 text-xs font-medium transition-colors">
            View accounts <ArrowRightIcon size={12} />
          </Link>
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          label={`Income — ${monthName}`}
          value={fmt(summary?.totalIncome ?? 0)}
          icon={<TrendUpIcon size={16} className="text-success-600" />}
          iconBg="bg-success-50"
          valueColor="text-success-700"
          loading={loading}
        />
        <StatCard
          label={`Expenses — ${monthName}`}
          value={fmt(summary?.totalExpenses ?? 0)}
          icon={<TrendDownIcon size={16} className="text-danger-600" />}
          iconBg="bg-danger-50"
          valueColor="text-danger-700"
          loading={loading}
        />
        <StatCard
          label={`Net Savings — ${monthName}`}
          value={fmt(Math.abs(netSavings))}
          prefix={savingsPositive ? '+' : '-'}
          icon={<span className="text-sm">{savingsPositive ? '🎯' : '⚠️'}</span>}
          iconBg={savingsPositive ? 'bg-primary-50' : 'bg-warning-50'}
          valueColor={savingsPositive ? 'text-primary-700' : 'text-warning-600'}
          loading={loading}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <div className="flex items-center justify-between mb-5">
            <h2 className="section-title">Spending by Category</h2>
            <span className="text-xs text-gray-400">{monthName}</span>
          </div>
          <SpendingChart data={breakdown} />
        </div>
        <div className="card p-6">
          <div className="flex items-center justify-between mb-5">
            <h2 className="section-title">Income vs Expenses</h2>
            <span className="text-xs text-gray-400">Last 6 months</span>
          </div>
          <TrendChart data={trend} />
        </div>
      </div>

      {/* Net Worth History */}
      {netWorthHistory.length > 1 && (
        <div className="card p-6">
          <div className="flex items-center justify-between mb-5">
            <h2 className="section-title">Net Worth History</h2>
            <span className="text-xs text-gray-400">Last 90 days</span>
          </div>
          <ResponsiveContainer width="100%" height={180}>
            <AreaChart data={netWorthHistory} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="nwGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#1D4ED8" stopOpacity={0.15} />
                  <stop offset="95%" stopColor="#1D4ED8" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#F0F4FA" vertical={false} />
              <XAxis
                dataKey="date"
                tick={{ fontSize: 11, fill: '#9CA3AF' }}
                tickLine={false}
                axisLine={false}
                tickFormatter={d => {
                  const date = new Date(d)
                  return `${date.toLocaleString('default', { month: 'short' })} ${date.getDate()}`
                }}
                interval="preserveStartEnd"
              />
              <YAxis
                tick={{ fontSize: 11, fill: '#9CA3AF' }}
                tickLine={false}
                axisLine={false}
                tickFormatter={v => `$${Math.abs(v) >= 1000 ? `${(v / 1000).toFixed(0)}k` : v}`}
                width={48}
              />
              <Tooltip
                contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 20px rgba(0,0,0,0.1)', fontSize: 12 }}
                formatter={(value: number) => [fmt(value), 'Net Worth']}
                labelFormatter={d => new Date(d).toLocaleDateString('en-CA', { month: 'long', day: 'numeric' })}
              />
              <Area
                type="monotone"
                dataKey="netWorth"
                stroke="#1D4ED8"
                strokeWidth={2}
                fill="url(#nwGradient)"
                dot={false}
                activeDot={{ r: 4, fill: '#1D4ED8' }}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Recent transactions */}
      <div className="card p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="section-title">Recent Transactions</h2>
          <Link
            to="/transactions"
            className="flex items-center gap-1 text-sm text-primary-600 font-medium hover:text-primary-700 transition-colors"
          >
            View all <ArrowRightIcon size={14} />
          </Link>
        </div>
        <TransactionTable transactions={recent} compact />
      </div>
    </div>
  )
}

function StatCard({ label, value, prefix, icon, iconBg, valueColor, loading }: {
  label: string
  value: string
  prefix?: string
  icon: React.ReactNode
  iconBg: string
  valueColor: string
  loading: boolean
}) {
  return (
    <div className="card p-5">
      <div className="flex items-center justify-between mb-3">
        <p className="text-xs font-medium text-gray-500 uppercase tracking-wider">{label}</p>
        <div className={`w-8 h-8 rounded-lg ${iconBg} flex items-center justify-center`}>{icon}</div>
      </div>
      {loading ? (
        <div className="h-7 w-28 bg-gray-100 rounded-lg animate-pulse" />
      ) : (
        <p className={`text-xl font-bold tracking-tight ${valueColor}`}>
          {prefix}{value}
        </p>
      )}
    </div>
  )
}
