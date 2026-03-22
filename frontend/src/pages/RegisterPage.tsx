import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { WalletIcon, ShieldIcon } from '../components/Icons'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function validate() {
    if (!form.firstName.trim()) return 'First name is required.'
    if (!form.lastName.trim()) return 'Last name is required.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Enter a valid email address.'
    if (form.password.length < 8) return 'Password must be at least 8 characters.'
    return null
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const err = validate()
    if (err) { setError(err); return }
    setError('')
    setLoading(true)
    try {
      await register(form)
      navigate('/dashboard')
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      setError(status === 409 ? 'This email is already registered.' : 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left panel */}
      <div className="hidden lg:flex lg:w-[45%] bg-navy-800 flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute -top-32 -right-32 w-80 h-80 bg-primary-600/20 rounded-full blur-3xl" />
          <div className="absolute -bottom-32 -left-32 w-80 h-80 bg-primary-800/30 rounded-full blur-3xl" />
        </div>

        <div className="relative flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-primary-600 flex items-center justify-center shadow-lg">
            <WalletIcon className="text-white" size={20} />
          </div>
          <div>
            <p className="text-white font-bold text-lg leading-tight">FinanceIQ</p>
            <p className="text-navy-300 text-xs">Personal Banking</p>
          </div>
        </div>

        <div className="relative space-y-6">
          <h1 className="text-4xl font-bold text-white leading-tight">
            Start your<br />
            <span className="text-primary-400">financial journey.</span>
          </h1>
          <p className="text-navy-300 text-base leading-relaxed max-w-sm">
            Join thousands of Canadians who track their spending and hit their savings goals.
          </p>

          <div className="space-y-3 pt-2">
            {[
              { icon: '📊', title: 'Smart Dashboard', desc: 'See all your accounts at a glance' },
              { icon: '🎯', title: 'Budget Tracking', desc: 'Set limits and get alerted before you overspend' },
              { icon: '📈', title: 'Trend Analysis', desc: '6-month income vs. expense charts' },
            ].map(item => (
              <div key={item.title} className="flex items-start gap-3 p-3 bg-white/5 rounded-xl border border-white/10">
                <span className="text-lg">{item.icon}</span>
                <div>
                  <p className="text-white text-sm font-medium">{item.title}</p>
                  <p className="text-navy-400 text-xs">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="relative flex items-center gap-2 text-navy-400 text-xs">
          <ShieldIcon size={14} />
          <span>Bank-level 256-bit encryption</span>
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-6 lg:p-16 bg-[#F0F4FA]">
        <div className="w-full max-w-md">
          <div className="lg:hidden flex items-center gap-3 mb-8">
            <div className="w-9 h-9 rounded-xl bg-primary-600 flex items-center justify-center shadow-md">
              <WalletIcon className="text-white" size={18} />
            </div>
            <p className="text-gray-900 font-bold text-lg">FinanceIQ</p>
          </div>

          <div className="bg-white rounded-2xl shadow-card border border-gray-100 p-8">
            <div className="mb-7">
              <h2 className="text-2xl font-bold text-gray-900">Create account</h2>
              <p className="text-sm text-gray-500 mt-1">It's free — no credit card needed</p>
            </div>

            {error && (
              <div className="mb-5 flex items-start gap-2.5 px-4 py-3 bg-danger-50 border border-danger-100 rounded-xl text-sm text-danger-700">
                <span className="mt-0.5 flex-shrink-0">⚠</span>
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="form-label">First name</label>
                  <input
                    type="text" required autoComplete="given-name"
                    placeholder="Jane"
                    className="form-input"
                    value={form.firstName}
                    onChange={e => setForm(f => ({ ...f, firstName: e.target.value }))}
                  />
                </div>
                <div>
                  <label className="form-label">Last name</label>
                  <input
                    type="text" required autoComplete="family-name"
                    placeholder="Smith"
                    className="form-input"
                    value={form.lastName}
                    onChange={e => setForm(f => ({ ...f, lastName: e.target.value }))}
                  />
                </div>
              </div>
              <div>
                <label className="form-label">Email address</label>
                <input
                  type="email" required autoComplete="email"
                  placeholder="you@example.com"
                  className="form-input"
                  value={form.email}
                  onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                />
              </div>
              <div>
                <label className="form-label">Password</label>
                <input
                  type="password" required minLength={8} autoComplete="new-password"
                  placeholder="Min. 8 characters"
                  className="form-input"
                  value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                />
              </div>
              <button type="submit" disabled={loading} className="btn-primary w-full text-sm mt-2">
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                    </svg>
                    Creating account...
                  </span>
                ) : 'Create account'}
              </button>
            </form>

            <p className="text-sm text-center text-gray-500 mt-6">
              Already have an account?{' '}
              <Link to="/login" className="text-primary-600 font-semibold hover:text-primary-700">Sign in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
