'use client'

import { useEffect, useState, FormEvent } from 'react'
import { useRouter } from 'next/navigation'

interface Stats {
  totalDevices: number
  active: number
  expired: number
  revoked: number
  totalPlaylists: number
}

export default function AdminDashboardPage() {
  const router = useRouter()
  const [stats, setStats] = useState<Stats | null>(null)
  const [statsLoading, setStatsLoading] = useState(true)
  const [statsError, setStatsError] = useState('')

  // Quick activate form
  const [mac, setMac] = useState('')
  const [plan, setPlan] = useState('MANUAL')
  const [expiresAt, setExpiresAt] = useState('')
  const [activating, setActivating] = useState(false)
  const [activateMsg, setActivateMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  useEffect(() => {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }
    fetchStats(token)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [router])

  const fetchStats = async (token: string) => {
    try {
      const res = await fetch('/api/admin/stats', {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }
      if (!res.ok) throw new Error('Failed to load stats')
      const data = await res.json()
      setStats(data)
    } catch (err: unknown) {
      setStatsError(err instanceof Error ? err.message : 'Failed to load stats')
    } finally {
      setStatsLoading(false)
    }
  }

  const handleActivate = async (e: FormEvent) => {
    e.preventDefault()
    setActivateMsg(null)
    setActivating(true)

    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }

    try {
      const body: Record<string, string> = { macAddress: mac, plan }
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
        setActivateMsg({ type: 'error', text: data.error || 'Activation failed' })
        return
      }

      setActivateMsg({
        type: 'success',
        text: `Device ${data.macAddress} activated with plan ${data.activation?.plan ?? plan}`,
      })
      setMac('')
      setExpiresAt('')
      // Refresh stats
      fetchStats(token)
    } catch {
      setActivateMsg({ type: 'error', text: 'Network error. Please try again.' })
    } finally {
      setActivating(false)
    }
  }

  const statCards = stats
    ? [
        {
          label: 'Total Devices',
          value: stats.totalDevices,
          color: 'var(--text-primary)',
          icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--primary-light)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="3" width="20" height="14" rx="2" />
              <line x1="8" y1="21" x2="16" y2="21" />
              <line x1="12" y1="17" x2="12" y2="21" />
            </svg>
          ),
        },
        {
          label: 'Active',
          value: stats.active,
          color: 'var(--success)',
          icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--success)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
          ),
        },
        {
          label: 'Expired',
          value: stats.expired,
          color: 'var(--warning)',
          icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--warning)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          ),
        },
        {
          label: 'Revoked',
          value: stats.revoked,
          color: 'var(--error)',
          icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--error)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10" />
              <line x1="15" y1="9" x2="9" y2="15" />
              <line x1="9" y1="9" x2="15" y2="15" />
            </svg>
          ),
        },
        {
          label: 'Total Playlists',
          value: stats.totalPlaylists,
          color: 'var(--accent)',
          icon: (
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--accent)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="8" y1="6" x2="21" y2="6" />
              <line x1="8" y1="12" x2="21" y2="12" />
              <line x1="8" y1="18" x2="21" y2="18" />
              <line x1="3" y1="6" x2="3.01" y2="6" />
              <line x1="3" y1="12" x2="3.01" y2="12" />
              <line x1="3" y1="18" x2="3.01" y2="18" />
            </svg>
          ),
        },
      ]
    : []

  return (
    <div>
      <h1
        className="glow-text animate-fade-in-up"
        style={{ fontSize: 28, fontWeight: 700, margin: 0, marginBottom: 32 }}
      >
        Dashboard
      </h1>

      {/* Stats Cards */}
      {statsLoading ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16, marginBottom: 40 }}>
          {[1, 2, 3, 4, 5].map((i) => (
            <div
              key={i}
              className="glass-card-sm"
              style={{ padding: 24, height: 100 }}
            >
              <div
                style={{
                  width: '60%',
                  height: 12,
                  borderRadius: 6,
                  background: 'var(--bg-elevated)',
                  marginBottom: 12,
                }}
              />
              <div
                style={{
                  width: '40%',
                  height: 28,
                  borderRadius: 6,
                  background: 'var(--bg-elevated)',
                }}
              />
            </div>
          ))}
        </div>
      ) : statsError ? (
        <div
          style={{
            background: 'rgba(255, 82, 82, 0.1)',
            border: '1px solid rgba(255, 82, 82, 0.25)',
            borderRadius: 12,
            padding: '16px 20px',
            marginBottom: 40,
            color: 'var(--error)',
          }}
        >
          {statsError}
        </div>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
            gap: 16,
            marginBottom: 40,
          }}
        >
          {statCards.map((card, i) => (
            <div
              key={card.label}
              className={`glass-card-sm animate-fade-in-up animate-delay-${i + 1}`}
              style={{ padding: 24 }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginBottom: 12,
                }}
              >
                <span style={{ fontSize: 13, color: 'var(--text-secondary)', fontWeight: 500 }}>
                  {card.label}
                </span>
                {card.icon}
              </div>
              <div style={{ fontSize: 32, fontWeight: 700, color: card.color }}>
                {card.value}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Quick Activate */}
      <div className="glass-card animate-fade-in-up animate-delay-3" style={{ padding: 32, maxWidth: 600 }}>
        <h2 style={{ fontSize: 20, fontWeight: 600, margin: 0, marginBottom: 24 }}>
          Quick Activate
        </h2>

        <form onSubmit={handleActivate}>
          <div style={{ marginBottom: 20 }}>
            <label
              style={{
                display: 'block',
                fontSize: 13,
                fontWeight: 500,
                color: 'var(--text-secondary)',
                marginBottom: 8,
              }}
            >
              MAC Address
            </label>
            <input
              type="text"
              className="input-field input-mono"
              placeholder="AA:BB:CC:DD:EE:FF"
              value={mac}
              onChange={(e) => setMac(e.target.value)}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 20 }}>
            <div>
              <label
                style={{
                  display: 'block',
                  fontSize: 13,
                  fontWeight: 500,
                  color: 'var(--text-secondary)',
                  marginBottom: 8,
                }}
              >
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

            <div>
              <label
                style={{
                  display: 'block',
                  fontSize: 13,
                  fontWeight: 500,
                  color: 'var(--text-secondary)',
                  marginBottom: 8,
                }}
              >
                Expiry Date (optional)
              </label>
              <input
                type="date"
                className="input-field"
                value={expiresAt}
                onChange={(e) => setExpiresAt(e.target.value)}
              />
            </div>
          </div>

          {activateMsg && (
            <div
              style={{
                background: activateMsg.type === 'success' ? 'rgba(105, 240, 174, 0.1)' : 'rgba(255, 82, 82, 0.1)',
                border: `1px solid ${activateMsg.type === 'success' ? 'rgba(105, 240, 174, 0.25)' : 'rgba(255, 82, 82, 0.25)'}`,
                borderRadius: 10,
                padding: '10px 14px',
                marginBottom: 20,
                color: activateMsg.type === 'success' ? 'var(--success)' : 'var(--error)',
                fontSize: 14,
              }}
            >
              {activateMsg.text}
            </div>
          )}

          <button
            type="submit"
            className="btn-primary"
            disabled={activating}
            style={{ opacity: activating ? 0.7 : 1 }}
          >
            {activating ? 'Activating...' : 'Activate'}
          </button>
        </form>
      </div>
    </div>
  )
}
