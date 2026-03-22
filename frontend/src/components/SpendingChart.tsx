import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts'
import type { CategoryBreakdown } from '../api/analyticsApi'

const COLORS = [
  '#1D4ED8', '#0891B2', '#059669', '#D97706', '#DC2626',
  '#7C3AED', '#DB2777', '#0284C7', '#16A34A', '#CA8A04',
]

interface Props {
  data: CategoryBreakdown[]
}

function CustomTooltip({ active, payload }: { active?: boolean; payload?: Array<{ name: string; value: number; payload: CategoryBreakdown }> }) {
  if (!active || !payload?.length) return null
  const item = payload[0]
  return (
    <div className="bg-white border border-gray-100 rounded-xl shadow-card-hover px-4 py-3 text-sm">
      <p className="font-semibold text-gray-900">{item.payload.categoryName}</p>
      <p className="text-gray-500 mt-0.5">${item.value.toLocaleString('en-CA', { minimumFractionDigits: 2 })}</p>
      <p className="text-primary-600 font-medium">{item.payload.percentage.toFixed(1)}% of spending</p>
    </div>
  )
}

export default function SpendingChart({ data }: Props) {
  if (data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-center">
        <div className="w-12 h-12 rounded-2xl bg-gray-100 flex items-center justify-center mb-3 text-xl">📊</div>
        <p className="text-sm text-gray-400">No expense data this month</p>
      </div>
    )
  }

  const top = data.slice(0, 7)

  return (
    <div>
      <ResponsiveContainer width="100%" height={240}>
        <PieChart>
          <Pie
            data={top}
            dataKey="totalAmount"
            nameKey="categoryName"
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={100}
            paddingAngle={2}
          >
            {top.map((_, i) => (
              <Cell key={i} fill={COLORS[i % COLORS.length]} stroke="transparent" />
            ))}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
        </PieChart>
      </ResponsiveContainer>

      {/* Legend */}
      <div className="mt-3 space-y-2">
        {top.map((item, i) => (
          <div key={item.categoryName} className="flex items-center justify-between text-sm">
            <div className="flex items-center gap-2 min-w-0">
              <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: COLORS[i % COLORS.length] }} />
              <span className="text-gray-600 truncate">{item.categoryName}</span>
            </div>
            <div className="flex items-center gap-3 flex-shrink-0 ml-4">
              <span className="text-gray-400 text-xs">{item.percentage.toFixed(0)}%</span>
              <span className="font-medium text-gray-900">${item.totalAmount.toLocaleString('en-CA', { minimumFractionDigits: 0 })}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
