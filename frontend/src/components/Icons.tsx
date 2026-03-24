interface IconProps {
  className?: string
  size?: number
}

const props = (className = '', size = 20) => ({
  width: size, height: size, viewBox: '0 0 24 24',
  fill: 'none', stroke: 'currentColor',
  strokeWidth: 1.75, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const,
  className,
})

export function DashboardIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <rect x="3" y="3" width="7" height="7" rx="1" />
      <rect x="14" y="3" width="7" height="7" rx="1" />
      <rect x="3" y="14" width="7" height="7" rx="1" />
      <rect x="14" y="14" width="7" height="7" rx="1" />
    </svg>
  )
}

export function TransactionsIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M17 3l4 4-4 4" />
      <path d="M3 7h18" />
      <path d="M7 21l-4-4 4-4" />
      <path d="M21 17H3" />
    </svg>
  )
}

export function BudgetIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <circle cx="12" cy="12" r="10" />
      <path d="M12 2v10l6 3" />
    </svg>
  )
}

export function AccountsIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
      <rect x="9" y="14" width="6" height="7" />
    </svg>
  )
}

export function LogoutIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
      <polyline points="16 17 21 12 16 7" />
      <line x1="21" y1="12" x2="9" y2="12" />
    </svg>
  )
}

export function UserIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  )
}

export function PlusIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <line x1="12" y1="5" x2="12" y2="19" />
      <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
  )
}

export function EditIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
  )
}

export function TrashIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
      <path d="M10 11v6" />
      <path d="M14 11v6" />
      <path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2" />
    </svg>
  )
}

export function CloseIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  )
}

export function ChevronLeftIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <polyline points="15 18 9 12 15 6" />
    </svg>
  )
}

export function ChevronRightIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <polyline points="9 18 15 12 9 6" />
    </svg>
  )
}

export function MenuIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <line x1="3" y1="6" x2="21" y2="6" />
      <line x1="3" y1="12" x2="21" y2="12" />
      <line x1="3" y1="18" x2="21" y2="18" />
    </svg>
  )
}

export function TrendUpIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
      <polyline points="17 6 23 6 23 12" />
    </svg>
  )
}

export function TrendDownIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <polyline points="23 18 13.5 8.5 8.5 13.5 1 6" />
      <polyline points="17 18 23 18 23 12" />
    </svg>
  )
}

export function WalletIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M20 7H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2z" />
      <path d="M16 14a1 1 0 100-2 1 1 0 000 2z" fill="currentColor" stroke="none" />
      <path d="M20 7V5a2 2 0 00-2-2H6a2 2 0 00-2 2v2" />
    </svg>
  )
}

export function ArrowRightIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </svg>
  )
}

export function SearchIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <circle cx="11" cy="11" r="8" />
      <line x1="21" y1="21" x2="16.65" y2="16.65" />
    </svg>
  )
}

export function FilterIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
    </svg>
  )
}

export function BellIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.73 21a2 2 0 01-3.46 0" />
    </svg>
  )
}

export function ShieldIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  )
}

export function GoalIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <circle cx="12" cy="12" r="10" />
      <circle cx="12" cy="12" r="6" />
      <circle cx="12" cy="12" r="2" fill="currentColor" stroke="none" />
    </svg>
  )
}

export function RecurringIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M17 2l4 4-4 4" />
      <path d="M3 11V9a4 4 0 014-4h14" />
      <path d="M7 22l-4-4 4-4" />
      <path d="M21 13v2a4 4 0 01-4 4H3" />
    </svg>
  )
}

export function DownloadIcon({ className, size }: IconProps) {
  return (
    <svg {...props(className, size)}>
      <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
      <polyline points="7 10 12 15 17 10" />
      <line x1="12" y1="15" x2="12" y2="3" />
    </svg>
  )
}
