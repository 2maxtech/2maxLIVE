'use client'

import { useState, useEffect, useCallback } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import DeviceStatusCard from '@/components/DeviceStatusCard'
import PlaylistEditor from '@/components/PlaylistEditor'
import type { PlaylistResponse } from '@/types'

interface DeviceStatus {
  activated: boolean
  plan: string | null
  expiresAt: string | null
  playlistCount: number
  isLocked: boolean
  status?: string
  activatedAt?: string
}

const MAX_PLAYLISTS = 3

export default function DashboardContent() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const mac = searchParams.get('mac')

  const [deviceStatus, setDeviceStatus] = useState<DeviceStatus | null>(null)
  const [playlists, setPlaylists] = useState<PlaylistResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Playlist editor state
  const [editingSlot, setEditingSlot] = useState<number | null>(null)
  const [editingPlaylist, setEditingPlaylist] = useState<PlaylistResponse | null>(null)
  const [deletingId, setDeletingId] = useState<string | null>(null)

  // Device lock state
  const [lockPin, setLockPin] = useState('')
  const [lockLoading, setLockLoading] = useState(false)
  const [lockMessage, setLockMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  // Redirect if no mac
  useEffect(() => {
    if (!mac) {
      router.replace('/activation')
    }
  }, [mac, router])

  const fetchDeviceStatus = useCallback(async () => {
    if (!mac) return
    try {
      const res = await fetch(`/api/device/status?mac=${encodeURIComponent(mac)}`)
      if (res.status === 404) {
        setError('Device not found. Please check your MAC address.')
        return
      }
      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'Failed to fetch device status')
      }
      const data = await res.json()
      // Derive status from activated flag
      const status = data.activated ? 'ACTIVE' : (data.status || 'INACTIVE')
      setDeviceStatus({ ...data, status })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load device status')
    }
  }, [mac])

  const fetchPlaylists = useCallback(async () => {
    if (!mac) return
    try {
      const res = await fetch(`/api/playlist?mac=${encodeURIComponent(mac)}`)
      if (res.ok) {
        const data = await res.json()
        setPlaylists(data)
      }
      // 403 means not activated - playlists just stay empty
    } catch {
      // silently fail for playlists
    }
  }, [mac])

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    await fetchDeviceStatus()
    await fetchPlaylists()
    setLoading(false)
  }, [fetchDeviceStatus, fetchPlaylists])

  useEffect(() => {
    if (mac) loadAll()
  }, [mac, loadAll])

  const handleDeletePlaylist = async (id: string) => {
    try {
      const res = await fetch(`/api/playlist/${id}`, { method: 'DELETE' })
      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'Failed to delete playlist')
      }
      setDeletingId(null)
      await fetchPlaylists()
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to delete playlist')
    }
  }

  const handleLockDevice = async (e: React.FormEvent) => {
    e.preventDefault()
    if (lockPin.length !== 4 || !mac) return

    setLockLoading(true)
    setLockMessage(null)

    try {
      const res = await fetch('/api/device/lock', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ macAddress: mac, pin: lockPin }),
      })

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'Failed to lock device')
      }

      setLockMessage({ type: 'success', text: 'Device locked successfully' })
      setLockPin('')
      await fetchDeviceStatus()
    } catch (err) {
      setLockMessage({ type: 'error', text: err instanceof Error ? err.message : 'Failed to lock device' })
    } finally {
      setLockLoading(false)
    }
  }

  if (!mac) return null

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-bg-deep flex items-center justify-center">
        <div className="text-center space-y-4">
          <div className="spinner mx-auto" style={{ width: 40, height: 40 }} />
          <p className="text-txt-secondary">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  // Error / not found / not activated
  if (error || !deviceStatus || !deviceStatus.activated) {
    return (
      <div className="min-h-screen bg-bg-deep">
        <div className="max-w-xl mx-auto px-6 py-16 text-center">
          <div className="glass-card p-8">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-error/10 mb-4">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none" className="text-error">
                <circle cx="16" cy="16" r="13" stroke="currentColor" strokeWidth="2"/>
                <path d="M16 10V18M16 22H16.01" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-txt-primary mb-3">
              {error || 'Device Not Activated'}
            </h1>
            <p className="text-txt-secondary mb-6">
              {error
                ? 'There was a problem loading your device information.'
                : 'This device is not currently activated. Contact your administrator.'}
            </p>
            <Link href="/activation" className="btn-primary inline-flex">
              Back to Activation
            </Link>
          </div>
        </div>
      </div>
    )
  }

  // Build playlist slots
  const playlistSlots: (PlaylistResponse | null)[] = []
  for (let i = 0; i < MAX_PLAYLISTS; i++) {
    const existing = playlists.find((p) => p.sortOrder === i)
    playlistSlots.push(existing || null)
  }

  return (
    <div className="min-h-screen bg-bg-deep relative">
      {/* Background gradient */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-0 right-0 w-[600px] h-[400px] bg-primary/[0.05] rounded-full blur-[120px]" />
        <div className="absolute bottom-0 left-0 w-[400px] h-[300px] bg-accent/[0.03] rounded-full blur-[100px]" />
      </div>

      {/* Top Bar */}
      <header className="relative z-10 border-b border-white/[0.06] bg-bg-deep/80 backdrop-blur-lg">
        <div className="max-w-4xl mx-auto px-6 py-4 flex items-center justify-between">
          <Link href="/" className="text-xl font-bold text-txt-primary hover:text-primary-light transition-colors">
            2maX player
          </Link>
          <div className="flex items-center gap-4">
            <span className="text-sm text-txt-secondary hidden sm:block">
              Device: <span className="font-mono text-txt-primary">{mac}</span>
            </span>
            <Link
              href="/activation"
              className="text-sm text-primary-light hover:text-primary transition-colors"
            >
              Change Device
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="relative z-10 max-w-4xl mx-auto px-6 py-8 space-y-6">

        {/* A. Activation Status Card */}
        <div className="animate-fade-in-up">
          <DeviceStatusCard
            status={deviceStatus.status || 'ACTIVE'}
            plan={deviceStatus.plan || 'Standard'}
            expiresAt={deviceStatus.expiresAt}
            activatedAt={deviceStatus.activatedAt}
          />
        </div>

        {/* B. Playlist Manager */}
        <div className="glass-card p-6 animate-fade-in-up animate-delay-1">
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-semibold text-txt-primary">Your Playlists</h2>
            <span className="text-sm text-txt-secondary">
              {playlists.length}/{MAX_PLAYLISTS} slots used
            </span>
          </div>

          <div className="space-y-4">
            {playlistSlots.map((playlist, index) => {
              // Show editor for this slot
              if (editingSlot === index) {
                return (
                  <PlaylistEditor
                    key={`editor-${index}`}
                    macAddress={mac}
                    playlist={editingPlaylist}
                    sortOrder={index}
                    onSave={() => {
                      setEditingSlot(null)
                      setEditingPlaylist(null)
                      fetchPlaylists()
                    }}
                    onCancel={() => {
                      setEditingSlot(null)
                      setEditingPlaylist(null)
                    }}
                  />
                )
              }

              // Filled slot
              if (playlist) {
                return (
                  <div key={playlist.id} className="glass-card-sm p-4">
                    {/* Delete confirmation overlay */}
                    {deletingId === playlist.id ? (
                      <div className="space-y-3">
                        <p className="text-sm text-txt-primary">
                          Delete <span className="font-semibold">{playlist.name}</span>?
                        </p>
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleDeletePlaylist(playlist.id)}
                            className="btn-danger py-2 px-4 text-sm"
                          >
                            Yes, Delete
                          </button>
                          <button
                            onClick={() => setDeletingId(null)}
                            className="btn-secondary py-2 px-4 text-sm"
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    ) : (
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 min-w-0">
                          <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                            <span className="text-sm font-bold text-primary-light">{index + 1}</span>
                          </div>
                          <div className="min-w-0">
                            <p className="text-sm font-medium text-txt-primary truncate">{playlist.name}</p>
                            <span className={playlist.type === 'XTREAM' ? 'badge-xtream mt-1' : 'badge-m3u mt-1'}>
                              {playlist.type === 'XTREAM' ? 'Xtream' : 'M3U'}
                            </span>
                          </div>
                        </div>
                        <div className="flex items-center gap-2 flex-shrink-0">
                          <button
                            onClick={() => {
                              setEditingSlot(index)
                              setEditingPlaylist(playlist)
                            }}
                            className="p-2 rounded-lg text-txt-secondary hover:text-primary-light hover:bg-primary/10 transition-all"
                            title="Edit"
                          >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                              <path d="M11.333 2.00004C11.5081 1.82494 11.7169 1.68605 11.9471 1.59129C12.1773 1.49653 12.4241 1.44775 12.6733 1.44775C12.9226 1.44775 13.1693 1.49653 13.3996 1.59129C13.6298 1.68605 13.8386 1.82494 14.0137 2.00004C14.1887 2.17513 14.3276 2.38398 14.4224 2.61417C14.5172 2.84436 14.5659 3.09113 14.5659 3.34037C14.5659 3.58961 14.5172 3.83638 14.4224 4.06657C14.3276 4.29676 14.1887 4.50561 14.0137 4.6807L5.00033 13.6941L1.33366 14.6667L2.30633 11.0001L11.333 2.00004Z" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                          </button>
                          <button
                            onClick={() => setDeletingId(playlist.id)}
                            className="p-2 rounded-lg text-txt-secondary hover:text-error hover:bg-error/10 transition-all"
                            title="Delete"
                          >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                              <path d="M2 4H14M5.333 4V2.667C5.333 2.313 5.474 1.974 5.724 1.724C5.974 1.474 6.313 1.333 6.667 1.333H9.333C9.687 1.333 10.026 1.474 10.276 1.724C10.526 1.974 10.667 2.313 10.667 2.667V4M12.667 4V13.333C12.667 13.687 12.526 14.026 12.276 14.276C12.026 14.526 11.687 14.667 11.333 14.667H4.667C4.313 14.667 3.974 14.526 3.724 14.276C3.474 14.026 3.333 13.687 3.333 13.333V4H12.667Z" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )
              }

              // Empty slot
              return (
                <button
                  key={`empty-${index}`}
                  onClick={() => {
                    setEditingSlot(index)
                    setEditingPlaylist(null)
                  }}
                  className="w-full p-4 rounded-xl border-2 border-dashed border-white/[0.08] hover:border-primary/30 text-txt-disabled hover:text-txt-secondary transition-all flex items-center justify-center gap-2 group"
                >
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none" className="group-hover:text-primary-light transition-colors">
                    <path d="M10 4V16M4 10H16" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                  </svg>
                  <span className="text-sm font-medium">Add Playlist</span>
                </button>
              )
            })}
          </div>
        </div>

        {/* C. Device Lock */}
        <div className="glass-card p-6 animate-fade-in-up animate-delay-2">
          <h2 className="text-lg font-semibold text-txt-primary mb-4">Device Security</h2>

          <div className="flex items-center gap-3 mb-4">
            <div className={`w-3 h-3 rounded-full ${deviceStatus.isLocked ? 'bg-success' : 'bg-txt-disabled'}`} />
            <span className="text-sm text-txt-secondary">
              {deviceStatus.isLocked ? 'Device is locked with a PIN' : 'Device is unlocked'}
            </span>
          </div>

          <form onSubmit={handleLockDevice} className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1 max-w-[200px]">
              <input
                type="text"
                inputMode="numeric"
                value={lockPin}
                onChange={(e) => {
                  const val = e.target.value.replace(/\D/g, '').slice(0, 4)
                  setLockPin(val)
                  setLockMessage(null)
                }}
                placeholder="4-digit PIN"
                className="input-field input-mono text-center tracking-[0.5em]"
                maxLength={4}
              />
            </div>
            <button
              type="submit"
              disabled={lockPin.length !== 4 || lockLoading}
              className="btn-primary py-3 px-6 disabled:opacity-40 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none"
            >
              {lockLoading ? (
                <span className="flex items-center gap-2">
                  <div className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} />
                  Locking...
                </span>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <rect x="3.333" y="7.333" width="9.333" height="7.333" rx="1.333" stroke="currentColor" strokeWidth="1.2"/>
                    <path d="M5.333 7.333V5.333C5.333 3.86 6.527 2.667 8 2.667C9.473 2.667 10.667 3.86 10.667 5.333V7.333" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
                  </svg>
                  Lock Device
                </>
              )}
            </button>
          </form>

          {lockMessage && (
            <p className={`mt-3 text-sm ${lockMessage.type === 'success' ? 'text-success' : 'text-error'}`}>
              {lockMessage.text}
            </p>
          )}
        </div>

        {/* D. Notice */}
        <div className="flex items-center gap-3 px-5 py-3 rounded-xl bg-accent/[0.06] border border-accent/10 animate-fade-in-up animate-delay-3">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" className="text-accent flex-shrink-0">
            <circle cx="9" cy="9" r="8" stroke="currentColor" strokeWidth="1.2"/>
            <path d="M9 6V10M9 12.5H9.007" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          <p className="text-sm text-txt-secondary">
            Restart your 2maX player app to apply changes
          </p>
        </div>
      </main>
    </div>
  )
}
