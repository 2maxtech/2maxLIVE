'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'

interface Playlist {
  id: string
  name: string
  type: 'XTREAM' | 'M3U_URL'
  serverUrl?: string | null
  username?: string | null
  m3uUrl?: string | null
  sortOrder: number
  isActive: boolean
}

interface DeviceDetail {
  id: string
  macAddress: string
  isLocked: boolean
  createdAt: string
  lastCheckedIn: string | null
  activation: {
    id: string
    status: string
    plan: string
    activatedAt: string
    expiresAt: string | null
  } | null
  playlists: Playlist[]
}

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '\u2014'
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function formatDateTime(dateStr: string | null | undefined): string {
  if (!dateStr) return '\u2014'
  const d = new Date(dateStr)
  return d.toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export default function AdminDeviceDetailPage({ params }: { params: { mac: string } }) {
  const { mac } = params
  const router = useRouter()
  const [device, setDevice] = useState<DeviceDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState(false)
  const [actionMsg, setActionMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  // Activate form
  const [showActivateForm, setShowActivateForm] = useState(false)
  const [plan, setPlan] = useState('MANUAL')
  const [expiresAt, setExpiresAt] = useState('')

  const decodedMac = decodeURIComponent(mac)

  useEffect(() => {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }
    fetchDevice(token)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mac, router])

  const fetchDevice = async (token?: string) => {
    const t = token || localStorage.getItem('admin_token')
    if (!t) return

    setLoading(true)
    setError('')
    try {
      const res = await fetch(`/api/admin/devices/${encodeURIComponent(decodedMac)}`, {
        headers: { Authorization: `Bearer ${t}` },
      })

      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }

      if (res.status === 404) {
        setError('Device not found')
        return
      }

      if (!res.ok) throw new Error('Failed to load device')

      const data = await res.json()
      setDevice(data)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load device')
    } finally {
      setLoading(false)
    }
  }

  const handleActivate = async () => {
    setActionLoading(true)
    setActionMsg(null)

    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }

    try {
      const body: Record<string, string> = { macAddress: decodedMac, plan }
      if (expiresAt) body.expiresAt = expiresAt

      const res = await fetch('/api/admin/activate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      })

      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }

      const data = await res.json()
      if (!res.ok) {
        setActionMsg({ type: 'error', text: data.error || 'Activation failed' })
        return
      }

      setActionMsg({ type: 'success', text: 'Device activated successfully' })
      setShowActivateForm(false)
      fetchDevice(token)
    } catch {
      setActionMsg({ type: 'error', text: 'Network error' })
    } finally {
      setActionLoading(false)
    }
  }

  const handleRevoke = async () => {
    if (!confirm('Are you sure you want to revoke this device\'s activation?')) return

    setActionLoading(true)
    setActionMsg(null)

    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }

    try {
      const res = await fetch('/api/admin/revoke', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ macAddress: decodedMac }),
      })

      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }

      const data = await res.json()
      if (!res.ok) {
        setActionMsg({ type: 'error', text: data.error || 'Revoke failed' })
        return
      }

      setActionMsg({ type: 'success', text: 'Activation revoked' })
      fetchDevice(token)
    } catch {
      setActionMsg({ type: 'error', text: 'Network error' })
    } finally {
      setActionLoading(false)
    }
  }

  const statusBadgeClass = () => {
    if (!device?.activation) return 'badge-inactive'
    const map: Record<string, string> = {
      ACTIVE: 'badge-active',
      EXPIRED: 'badge-expired',
      REVOKED: 'badge-revoked',
    }
    return map[device.activation.status] || 'badge-inactive'
  }

  const labelStyle: React.CSSProperties = {
    fontSize: 12,
    fontWeight: 500,
    color: 'var(--text-disabled)',
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
    marginBottom: 4,
  }

  const valueStyle: React.CSSProperties = {
    fontSize: 15,
    color: 'var(--text-primary)',
  }

  if (loading) {
    return (
      <div style={{ padding: 40, textAlign: 'center', color: 'var(--text-secondary)' }}>
        <div
          style={{
            width: 32,
            height: 32,
            border: '3px solid var(--bg-highest)',
            borderTopColor: 'var(--primary)',
            borderRadius: '50%',
            animation: 'spin 0.8s linear infinite',
            margin: '0 auto 16px',
          }}
        />
        Loading device...
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    )
  }

  if (error) {
    return (
      <div>
        <Link
          href="/admin/devices"
          style={{
            color: 'var(--text-secondary)',
            textDecoration: 'none',
            display: 'inline-flex',
            alignItems: 'center',
            gap: 6,
            fontSize: 14,
            marginBottom: 24,
          }}
        >
          {'\u2190'} Back to Devices
        </Link>
        <div
          style={{
            background: 'rgba(255, 82, 82, 0.1)',
            border: '1px solid rgba(255, 82, 82, 0.25)',
            borderRadius: 12,
            padding: '20px 24px',
            color: 'var(--error)',
          }}
        >
          {error}
        </div>
      </div>
    )
  }

  if (!device) return null

  return (
    <div>
      {/* Back button */}
      <Link
        href="/admin/devices"
        className="animate-fade-in-up"
        style={{
          color: 'var(--text-secondary)',
          textDecoration: 'none',
          display: 'inline-flex',
          alignItems: 'center',
          gap: 6,
          fontSize: 14,
          marginBottom: 24,
        }}
      >
        {'\u2190'} Back to Devices
      </Link>

      {/* MAC heading */}
      <h1
        className="animate-fade-in-up animate-delay-1"
        style={{
          fontFamily: "'JetBrains Mono', monospace",
          fontSize: 28,
          fontWeight: 600,
          margin: 0,
          marginBottom: 8,
          letterSpacing: '0.04em',
        }}
      >
        {device.macAddress}
      </h1>

      <div className="animate-fade-in-up animate-delay-1" style={{ marginBottom: 32 }}>
        <span className={`badge ${statusBadgeClass()}`}>
          {device.activation?.status ?? 'INACTIVE'}
        </span>
      </div>

      {/* Action messages */}
      {actionMsg && (
        <div
          style={{
            background: actionMsg.type === 'success' ? 'rgba(105, 240, 174, 0.1)' : 'rgba(255, 82, 82, 0.1)',
            border: `1px solid ${actionMsg.type === 'success' ? 'rgba(105, 240, 174, 0.25)' : 'rgba(255, 82, 82, 0.25)'}`,
            borderRadius: 10,
            padding: '10px 14px',
            marginBottom: 20,
            color: actionMsg.type === 'success' ? 'var(--success)' : 'var(--error)',
            fontSize: 14,
          }}
        >
          {actionMsg.text}
        </div>
      )}

      {/* Two-column layout */}
      <div
        className="animate-fade-in-up animate-delay-2"
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: 24,
          marginBottom: 32,
        }}
      >
        {/* Device Info Card */}
        <div className="glass-card" style={{ padding: 28 }}>
          <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0, marginBottom: 24, color: 'var(--text-secondary)' }}>
            Device Info
          </h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            <div>
              <div style={labelStyle}>First Seen</div>
              <div style={valueStyle}>{formatDateTime(device.createdAt)}</div>
            </div>
            <div>
              <div style={labelStyle}>Last Check-in</div>
              <div style={valueStyle}>{formatDateTime(device.lastCheckedIn)}</div>
            </div>
            <div>
              <div style={labelStyle}>Lock Status</div>
              <div style={valueStyle}>
                <span
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: 6,
                    color: device.isLocked ? 'var(--success)' : 'var(--text-disabled)',
                  }}
                >
                  {device.isLocked ? '\uD83D\uDD12 Locked' : '\uD83D\uDD13 Unlocked'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Activation Card */}
        <div className="glass-card" style={{ padding: 28 }}>
          <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0, marginBottom: 24, color: 'var(--text-secondary)' }}>
            Activation
          </h2>

          {device.activation ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
              <div>
                <div style={labelStyle}>Status</div>
                <div style={valueStyle}>
                  <span className={`badge ${statusBadgeClass()}`}>
                    {device.activation.status}
                  </span>
                </div>
              </div>
              <div>
                <div style={labelStyle}>Plan</div>
                <div style={{ ...valueStyle, fontWeight: 600 }}>{device.activation.plan}</div>
              </div>
              <div>
                <div style={labelStyle}>Activated At</div>
                <div style={valueStyle}>{formatDate(device.activation.activatedAt)}</div>
              </div>
              <div>
                <div style={labelStyle}>Expires At</div>
                <div style={valueStyle}>{formatDate(device.activation.expiresAt)}</div>
              </div>

              <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                <button
                  className="btn-primary"
                  onClick={() => setShowActivateForm(!showActivateForm)}
                  style={{ padding: '10px 20px', fontSize: 14 }}
                >
                  Re-Activate
                </button>
                {device.activation.status === 'ACTIVE' && (
                  <button
                    className="btn-danger"
                    onClick={handleRevoke}
                    disabled={actionLoading}
                    style={{ padding: '10px 20px', fontSize: 14, opacity: actionLoading ? 0.6 : 1 }}
                  >
                    {actionLoading ? 'Revoking...' : 'Revoke'}
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div>
              <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 20 }}>
                No activation record. This device has not been activated.
              </p>
              <button
                className="btn-primary"
                onClick={() => setShowActivateForm(!showActivateForm)}
                style={{ padding: '10px 20px', fontSize: 14 }}
              >
                Activate Device
              </button>
            </div>
          )}

          {/* Inline Activate Form */}
          {showActivateForm && (
            <div
              style={{
                marginTop: 20,
                padding: 20,
                background: 'var(--bg-base)',
                borderRadius: 12,
                border: '1px solid var(--border)',
              }}
            >
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: 'var(--text-secondary)', marginBottom: 6 }}>
                  Plan
                </label>
                <select
                  className="input-field"
                  value={plan}
                  onChange={(e) => setPlan(e.target.value)}
                  style={{ cursor: 'pointer' }}
                >
                  <option value="MANUAL">Manual</option>
                  <option value="TRIAL">Trial</option>
                  <option value="YEARLY">Yearly</option>
                  <option value="LIFETIME">Lifetime</option>
                </select>
              </div>
              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: 'var(--text-secondary)', marginBottom: 6 }}>
                  Expiry Date (optional)
                </label>
                <input
                  type="date"
                  className="input-field"
                  value={expiresAt}
                  onChange={(e) => setExpiresAt(e.target.value)}
                />
              </div>
              <div style={{ display: 'flex', gap: 12 }}>
                <button
                  className="btn-primary"
                  onClick={handleActivate}
                  disabled={actionLoading}
                  style={{ padding: '10px 20px', fontSize: 14, opacity: actionLoading ? 0.6 : 1 }}
                >
                  {actionLoading ? 'Activating...' : 'Confirm Activate'}
                </button>
                <button
                  className="btn-secondary"
                  onClick={() => setShowActivateForm(false)}
                  style={{ padding: '10px 20px', fontSize: 14 }}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Playlists Section */}
      <div className="glass-card animate-fade-in-up animate-delay-3" style={{ padding: 28 }}>
        <h2 style={{ fontSize: 16, fontWeight: 600, margin: 0, marginBottom: 20, color: 'var(--text-secondary)' }}>
          Playlists ({device.playlists.length})
        </h2>

        {device.playlists.length === 0 ? (
          <p style={{ color: 'var(--text-disabled)', fontSize: 14 }}>
            No playlists configured for this device.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {device.playlists.map((pl) => (
              <div
                key={pl.id}
                className="glass-card-sm"
                style={{
                  padding: '16px 20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 4 }}>{pl.name}</div>
                  <div style={{ fontSize: 13, color: 'var(--text-secondary)', display: 'flex', gap: 16 }}>
                    <span>Type: {pl.type === 'XTREAM' ? 'Xtream' : 'M3U URL'}</span>
                    {pl.type === 'XTREAM' && pl.serverUrl && (
                      <span
                        style={{
                          fontFamily: "'JetBrains Mono', monospace",
                          fontSize: 12,
                        }}
                      >
                        {pl.serverUrl}
                      </span>
                    )}
                    {pl.type === 'M3U_URL' && pl.m3uUrl && (
                      <span
                        style={{
                          fontFamily: "'JetBrains Mono', monospace",
                          fontSize: 12,
                          maxWidth: 300,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {pl.m3uUrl}
                      </span>
                    )}
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontSize: 12, color: 'var(--text-disabled)' }}>
                    #{pl.sortOrder}
                  </span>
                  <span className={`badge ${pl.isActive ? 'badge-active' : 'badge-inactive'}`}>
                    {pl.isActive ? 'Active' : 'Inactive'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
