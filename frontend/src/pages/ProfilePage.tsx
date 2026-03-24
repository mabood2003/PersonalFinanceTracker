import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../api/userApi'
import { ShieldIcon, UserIcon } from '../components/Icons'

function SuccessBanner({ message }: { message: string }) {
  return (
    <div className="flex items-center gap-2 px-4 py-3 bg-success-50 border border-success-100 rounded-xl text-sm text-success-700">
      <span>✓</span> {message}
    </div>
  )
}

function ErrorBanner({ message }: { message: string }) {
  return (
    <div className="flex items-center gap-2 px-4 py-3 bg-red-50 border border-red-100 rounded-xl text-sm text-red-700">
      <span>⚠</span> {message}
    </div>
  )
}

export default function ProfilePage() {
  const { user, updateUser, logout } = useAuth()
  const navigate = useNavigate()

  const [profile, setProfile] = useState({
    firstName: user?.firstName ?? '',
    lastName: user?.lastName ?? '',
    email: user?.email ?? '',
  })
  const [profileLoading, setProfileLoading] = useState(false)
  const [profileSuccess, setProfileSuccess] = useState('')
  const [profileError, setProfileError] = useState('')

  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [passwordLoading, setPasswordLoading] = useState(false)
  const [passwordSuccess, setPasswordSuccess] = useState('')
  const [passwordError, setPasswordError] = useState('')

  const initials = `${user?.firstName?.[0] ?? ''}${user?.lastName?.[0] ?? ''}`.toUpperCase()
  const emailChanged = profile.email.toLowerCase() !== (user?.email ?? '').toLowerCase()

  async function handleProfileSubmit(e: React.FormEvent) {
    e.preventDefault()
    setProfileLoading(true)
    setProfileSuccess('')
    setProfileError('')
    try {
      const updated = await userApi.updateProfile({
        firstName: profile.firstName.trim(),
        lastName: profile.lastName.trim(),
        email: profile.email.trim().toLowerCase(),
      })
      updateUser({ email: updated.email, firstName: updated.firstName, lastName: updated.lastName })
      if (emailChanged) {
        // Email changed — JWT is now invalid, force re-login
        logout()
        navigate('/login')
        return
      }
      setProfileSuccess('Profile updated successfully.')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setProfileError(msg ?? 'Failed to update profile. Please try again.')
    } finally {
      setProfileLoading(false)
    }
  }

  async function handlePasswordSubmit(e: React.FormEvent) {
    e.preventDefault()
    setPasswordSuccess('')
    setPasswordError('')

    if (passwords.newPassword !== passwords.confirmPassword) {
      setPasswordError('New passwords do not match.')
      return
    }
    if (passwords.newPassword.length < 8) {
      setPasswordError('New password must be at least 8 characters.')
      return
    }

    setPasswordLoading(true)
    try {
      await userApi.changePassword({
        currentPassword: passwords.currentPassword,
        newPassword: passwords.newPassword,
      })
      setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setPasswordSuccess('Password changed successfully.')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      setPasswordError(msg ?? 'Failed to change password. Please try again.')
    } finally {
      setPasswordLoading(false)
    }
  }

  return (
    <div className="space-y-6 max-w-2xl">
      {/* Header */}
      <div>
        <h1 className="page-title">Profile & Settings</h1>
        <p className="text-sm text-gray-500 mt-0.5">Manage your account information</p>
      </div>

      {/* Avatar */}
      <div className="card p-6 flex items-center gap-5">
        <div className="w-16 h-16 rounded-2xl bg-primary-600 flex items-center justify-center flex-shrink-0">
          <span className="text-xl font-bold text-white">{initials}</span>
        </div>
        <div>
          <p className="font-semibold text-gray-900 text-lg">{user?.firstName} {user?.lastName}</p>
          <p className="text-sm text-gray-500">{user?.email}</p>
        </div>
      </div>

      {/* Profile info */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-5">
          <div className="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
            <UserIcon size={16} className="text-primary-600" />
          </div>
          <h2 className="section-title">Personal Information</h2>
        </div>

        <form onSubmit={handleProfileSubmit} className="space-y-4">
          {profileSuccess && <SuccessBanner message={profileSuccess} />}
          {profileError && <ErrorBanner message={profileError} />}
          {emailChanged && (
            <div className="px-4 py-3 bg-warning-50 border border-warning-100 rounded-xl text-sm text-warning-600">
              ⚠ Changing your email will sign you out and require you to log in again.
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="form-label">First Name</label>
              <input
                type="text" required maxLength={100} className="form-input"
                value={profile.firstName}
                onChange={e => setProfile(p => ({ ...p, firstName: e.target.value }))}
              />
            </div>
            <div>
              <label className="form-label">Last Name</label>
              <input
                type="text" required maxLength={100} className="form-input"
                value={profile.lastName}
                onChange={e => setProfile(p => ({ ...p, lastName: e.target.value }))}
              />
            </div>
          </div>

          <div>
            <label className="form-label">Email Address</label>
            <input
              type="email" required className="form-input"
              value={profile.email}
              onChange={e => setProfile(p => ({ ...p, email: e.target.value }))}
            />
          </div>

          <div className="flex justify-end pt-1">
            <button
              type="submit"
              disabled={profileLoading}
              className="btn-primary text-sm px-6"
            >
              {profileLoading ? 'Saving...' : emailChanged ? 'Save & Re-login' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>

      {/* Change password */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-5">
          <div className="w-8 h-8 rounded-lg bg-warning-50 flex items-center justify-center">
            <ShieldIcon size={16} className="text-warning-600" />
          </div>
          <h2 className="section-title">Change Password</h2>
        </div>

        <form onSubmit={handlePasswordSubmit} className="space-y-4">
          {passwordSuccess && <SuccessBanner message={passwordSuccess} />}
          {passwordError && <ErrorBanner message={passwordError} />}

          <div>
            <label className="form-label">Current Password</label>
            <input
              type="password" required className="form-input"
              placeholder="Enter your current password"
              value={passwords.currentPassword}
              onChange={e => setPasswords(p => ({ ...p, currentPassword: e.target.value }))}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="form-label">New Password</label>
              <input
                type="password" required minLength={8} className="form-input"
                placeholder="Min. 8 characters"
                value={passwords.newPassword}
                onChange={e => setPasswords(p => ({ ...p, newPassword: e.target.value }))}
              />
            </div>
            <div>
              <label className="form-label">Confirm New Password</label>
              <input
                type="password" required className="form-input"
                placeholder="Repeat new password"
                value={passwords.confirmPassword}
                onChange={e => setPasswords(p => ({ ...p, confirmPassword: e.target.value }))}
              />
            </div>
          </div>

          {passwords.newPassword && passwords.confirmPassword && passwords.newPassword !== passwords.confirmPassword && (
            <p className="text-xs text-red-500">Passwords do not match</p>
          )}

          <div className="flex justify-end pt-1">
            <button
              type="submit"
              disabled={passwordLoading}
              className="btn-primary text-sm px-6"
            >
              {passwordLoading ? 'Updating...' : 'Change Password'}
            </button>
          </div>
        </form>
      </div>

      {/* Danger zone */}
      <div className="card p-6 border border-red-100">
        <h2 className="section-title text-danger-600 mb-3">Sign Out</h2>
        <p className="text-sm text-gray-500 mb-4">Sign out of your FinanceIQ account on this device.</p>
        <button
          onClick={() => { logout(); navigate('/login') }}
          className="btn-danger text-sm"
        >
          Sign Out
        </button>
      </div>
    </div>
  )
}
