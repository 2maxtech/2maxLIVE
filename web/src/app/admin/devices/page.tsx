'use client'

import { useEffect, useState, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import DeviceTable from '@/components/DeviceTable'

interface DeviceRow {
  id: string
  macAddress: string
  createdAt: string
  lastCheckedIn: string | null
  isLocked: boolean
  activation: {
    status: string
    plan: string
    expiresAt: string | null
  } | null
  playlistCount: number
}

export default function AdminDevicesPage() {
  const router = useRouter()
  const [devices, setDevices] = useState<DeviceRow[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchDevices = useCallback(async (token: string, searchTerm: string, pageNum: number) => {
    setLoading(true)
    setError('')
    try {
      const params = new URLSearchParams()
      if (searchTerm) params.set('search', searchTerm)
      params.set('page', String(pageNum))
      params.set('limit', '20')

      const res = await fetch(`/api/admin/devices?${params}`, {
        headers: { Authorization: `Bearer ${token}` },
      })

      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }

      if (!res.ok) throw new Error('Failed to load devices')

      const data = await res.json()
      setDevices(data.devices)
      setTotal(data.total)
      setPage(data.page)
      setTotalPages(data.totalPages)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load devices')
    } finally {
      setLoading(false)
    }
  }, [router])

  useEffect(() => {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }
    fetchDevices(token, search, page)
  }, [router, search, page, fetchDevices])

  const handleSearch = () => {
    setPage(1)
    setSearch(searchInput)
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch()
  }

  return (
    <div>
      <div
        className="animate-fade-in-up"
        style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 32 }}
      >
        <div>
          <h1 className="glow-text" style={{ fontSize: 28, fontWeight: 700, margin: 0 }}>
            Devices
          </h1>
          {!loading && (
            <p style={{ margin: 0, marginTop: 4, color: 'var(--text-secondary)', fontSize: 14 }}>
              {total} device{total !== 1 ? 's' : ''} total
            </p>
          )}
        </div>
      </div>

      {/* Search */}
      <div className="animate-fade-in-up animate-delay-1" style={{ marginBottom: 24, display: 'flex', gap: 12, maxWidth: 500 }}>
        <input
          type="text"
          className="input-field input-mono"
          placeholder="Search by MAC address..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          onKeyDown={handleKeyDown}
          style={{ flex: 1 }}
        />
        <button className="btn-secondary" onClick={handleSearch} style={{ padding: '12px 24px', whiteSpace: 'nowrap' }}>
          Search
        </button>
      </div>

      {/* Table */}
      <div className="glass-card animate-fade-in-up animate-delay-2" style={{ overflow: 'hidden' }}>
        {loading ? (
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
            Loading devices...
            <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
          </div>
        ) : error ? (
          <div style={{ padding: 40, textAlign: 'center', color: 'var(--error)' }}>
            {error}
          </div>
        ) : (
          <DeviceTable devices={devices} />
        )}
      </div>

      {/* Pagination */}
      {!loading && totalPages > 1 && (
        <div
          className="animate-fade-in-up animate-delay-3"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            marginTop: 24,
          }}
        >
          <button
            className="btn-secondary"
            onClick={() => setPage(Math.max(1, page - 1))}
            disabled={page <= 1}
            style={{ padding: '8px 16px', fontSize: 14, opacity: page <= 1 ? 0.4 : 1 }}
          >
            Previous
          </button>

          {Array.from({ length: totalPages }, (_, i) => i + 1)
            .filter((p) => {
              // Show first, last, and pages near current
              return p === 1 || p === totalPages || Math.abs(p - page) <= 2
            })
            .reduce<(number | 'ellipsis')[]>((acc, p, i, arr) => {
              if (i > 0 && p - (arr[i - 1] as number) > 1) acc.push('ellipsis')
              acc.push(p)
              return acc
            }, [])
            .map((item, i) =>
              item === 'ellipsis' ? (
                <span key={`e${i}`} style={{ color: 'var(--text-disabled)', padding: '0 4px' }}>
                  ...
                </span>
              ) : (
                <button
                  key={item}
                  onClick={() => setPage(item)}
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: 8,
                    border: page === item ? '1px solid var(--primary)' : '1px solid var(--border)',
                    background: page === item ? 'var(--primary)' : 'var(--bg-elevated)',
                    color: page === item ? 'white' : 'var(--text-secondary)',
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    fontSize: 14,
                    fontWeight: page === item ? 600 : 400,
                  }}
                >
                  {item}
                </button>
              )
            )}

          <button
            className="btn-secondary"
            onClick={() => setPage(Math.min(totalPages, page + 1))}
            disabled={page >= totalPages}
            style={{ padding: '8px 16px', fontSize: 14, opacity: page >= totalPages ? 0.4 : 1 }}
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
