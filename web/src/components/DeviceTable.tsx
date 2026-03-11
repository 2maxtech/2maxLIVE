'use client'

import Link from 'next/link'
import { useState, useMemo } from 'react'

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

interface DeviceTableProps {
  devices: DeviceRow[]
}

type SortKey = 'macAddress' | 'status' | 'plan' | 'playlists' | 'createdAt' | 'lastCheckedIn'
type SortDir = 'asc' | 'desc'

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '\u2014'
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

function getStatusBadge(activation: DeviceRow['activation']) {
  if (!activation) {
    return <span className="badge badge-inactive">Inactive</span>
  }
  const statusMap: Record<string, string> = {
    ACTIVE: 'badge-active',
    EXPIRED: 'badge-expired',
    REVOKED: 'badge-revoked',
  }
  const cls = statusMap[activation.status] || 'badge-inactive'
  return <span className={`badge ${cls}`}>{activation.status}</span>
}

export default function DeviceTable({ devices }: DeviceTableProps) {
  const [sortKey, setSortKey] = useState<SortKey>('createdAt')
  const [sortDir, setSortDir] = useState<SortDir>('desc')

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc')
    } else {
      setSortKey(key)
      setSortDir('asc')
    }
  }

  const sorted = useMemo(() => {
    const copy = [...devices]
    copy.sort((a, b) => {
      let aVal: string | number = ''
      let bVal: string | number = ''

      switch (sortKey) {
        case 'macAddress':
          aVal = a.macAddress
          bVal = b.macAddress
          break
        case 'status':
          aVal = a.activation?.status ?? ''
          bVal = b.activation?.status ?? ''
          break
        case 'plan':
          aVal = a.activation?.plan ?? ''
          bVal = b.activation?.plan ?? ''
          break
        case 'playlists':
          aVal = a.playlistCount
          bVal = b.playlistCount
          break
        case 'createdAt':
          aVal = a.createdAt
          bVal = b.createdAt
          break
        case 'lastCheckedIn':
          aVal = a.lastCheckedIn ?? ''
          bVal = b.lastCheckedIn ?? ''
          break
      }

      if (typeof aVal === 'number' && typeof bVal === 'number') {
        return sortDir === 'asc' ? aVal - bVal : bVal - aVal
      }
      const cmp = String(aVal).localeCompare(String(bVal))
      return sortDir === 'asc' ? cmp : -cmp
    })
    return copy
  }, [devices, sortKey, sortDir])

  const SortIcon = ({ column }: { column: SortKey }) => {
    if (sortKey !== column) {
      return <span style={{ opacity: 0.3, marginLeft: 4 }}>{'\u2195'}</span>
    }
    return <span style={{ marginLeft: 4, color: 'var(--primary)' }}>{sortDir === 'asc' ? '\u2191' : '\u2193'}</span>
  }

  const thStyle: React.CSSProperties = {
    padding: '12px 16px',
    textAlign: 'left',
    fontSize: 12,
    fontWeight: 600,
    color: 'var(--text-secondary)',
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
    cursor: 'pointer',
    userSelect: 'none',
    whiteSpace: 'nowrap',
    borderBottom: '1px solid var(--border)',
  }

  const tdStyle: React.CSSProperties = {
    padding: '14px 16px',
    fontSize: 14,
    borderBottom: '1px solid var(--border)',
  }

  if (devices.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '48px 0', color: 'var(--text-secondary)' }}>
        No devices found.
      </div>
    )
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={thStyle} onClick={() => handleSort('macAddress')}>
              MAC Address <SortIcon column="macAddress" />
            </th>
            <th style={thStyle} onClick={() => handleSort('status')}>
              Status <SortIcon column="status" />
            </th>
            <th style={thStyle} onClick={() => handleSort('plan')}>
              Plan <SortIcon column="plan" />
            </th>
            <th style={thStyle} onClick={() => handleSort('playlists')}>
              Playlists <SortIcon column="playlists" />
            </th>
            <th style={thStyle} onClick={() => handleSort('createdAt')}>
              First Seen <SortIcon column="createdAt" />
            </th>
            <th style={thStyle} onClick={() => handleSort('lastCheckedIn')}>
              Last Check-in <SortIcon column="lastCheckedIn" />
            </th>
            <th style={{ ...thStyle, cursor: 'default' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((device) => (
            <tr key={device.id} className="table-row">
              <td style={{ ...tdStyle, fontFamily: "'JetBrains Mono', monospace", fontSize: 13, letterSpacing: '0.04em' }}>
                {device.macAddress}
              </td>
              <td style={tdStyle}>{getStatusBadge(device.activation)}</td>
              <td style={tdStyle}>
                <span style={{ color: 'var(--text-secondary)' }}>
                  {device.activation?.plan ?? '\u2014'}
                </span>
              </td>
              <td style={tdStyle}>{device.playlistCount}</td>
              <td style={{ ...tdStyle, color: 'var(--text-secondary)' }}>{formatDate(device.createdAt)}</td>
              <td style={{ ...tdStyle, color: 'var(--text-secondary)' }}>{formatDate(device.lastCheckedIn)}</td>
              <td style={tdStyle}>
                <Link
                  href={`/admin/devices/${encodeURIComponent(device.macAddress)}`}
                  style={{
                    color: 'var(--primary-light)',
                    textDecoration: 'none',
                    fontSize: 13,
                    fontWeight: 500,
                  }}
                >
                  View
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
