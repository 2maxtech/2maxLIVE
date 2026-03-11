'use client'

import { useState } from 'react'

interface PlaylistEditorProps {
  macAddress: string
  playlist?: {
    id: string
    name: string
    type: string
    serverUrl?: string | null
    username?: string | null
    password?: string | null
    m3uUrl?: string | null
    sortOrder: number
    isActive: boolean
  } | null
  sortOrder: number
  onSave: () => void
  onCancel: () => void
}

export default function PlaylistEditor({ macAddress, playlist, sortOrder, onSave, onCancel }: PlaylistEditorProps) {
  const isEditing = !!playlist

  const [name, setName] = useState(playlist?.name ?? '')
  const [type, setType] = useState<'XTREAM' | 'M3U_URL'>((playlist?.type as 'XTREAM' | 'M3U_URL') ?? 'XTREAM')
  const [serverUrl, setServerUrl] = useState(playlist?.serverUrl ?? '')
  const [username, setUsername] = useState(playlist?.username ?? '')
  const [password, setPassword] = useState(playlist?.password ?? '')
  const [m3uUrl, setM3uUrl] = useState(playlist?.m3uUrl ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isValid = name.trim().length > 0 && (
    type === 'XTREAM'
      ? serverUrl.trim().length > 0 && username.trim().length > 0 && password.trim().length > 0
      : m3uUrl.trim().length > 0
  )

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!isValid || saving) return

    setSaving(true)
    setError(null)

    try {
      const payload: Record<string, unknown> = {
        name: name.trim(),
        type,
      }

      if (type === 'XTREAM') {
        payload.serverUrl = serverUrl.trim()
        payload.username = username.trim()
        payload.password = password.trim()
        payload.m3uUrl = null
      } else {
        payload.m3uUrl = m3uUrl.trim()
        payload.serverUrl = null
        payload.username = null
        payload.password = null
      }

      let res: Response

      if (isEditing) {
        res = await fetch(`/api/playlist/${playlist.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        })
      } else {
        payload.macAddress = macAddress
        payload.sortOrder = sortOrder
        res = await fetch('/api/playlist', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        })
      }

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'Failed to save playlist')
      }

      onSave()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="glass-card-sm p-5">
      <h3 className="text-base font-semibold text-txt-primary mb-4">
        {isEditing ? 'Edit Playlist' : 'Add New Playlist'}
      </h3>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name */}
        <div>
          <label className="block text-sm text-txt-secondary mb-1.5">Playlist Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="My Playlist"
            className="input-field"
          />
        </div>

        {/* Type Toggle */}
        <div>
          <label className="block text-sm text-txt-secondary mb-1.5">Type</label>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setType('XTREAM')}
              className={`flex-1 py-2.5 px-4 rounded-lg text-sm font-medium transition-all ${
                type === 'XTREAM'
                  ? 'bg-primary/20 text-primary-light border border-primary/30'
                  : 'bg-bg-elevated text-txt-secondary border border-white/[0.06] hover:text-txt-primary'
              }`}
            >
              Xtream Codes
            </button>
            <button
              type="button"
              onClick={() => setType('M3U_URL')}
              className={`flex-1 py-2.5 px-4 rounded-lg text-sm font-medium transition-all ${
                type === 'M3U_URL'
                  ? 'bg-primary/20 text-primary-light border border-primary/30'
                  : 'bg-bg-elevated text-txt-secondary border border-white/[0.06] hover:text-txt-primary'
              }`}
            >
              M3U URL
            </button>
          </div>
        </div>

        {/* Xtream Fields */}
        {type === 'XTREAM' && (
          <div className="space-y-3">
            <div>
              <label className="block text-sm text-txt-secondary mb-1.5">Server URL</label>
              <input
                type="url"
                value={serverUrl}
                onChange={(e) => setServerUrl(e.target.value)}
                placeholder="http://example.com:8080"
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm text-txt-secondary mb-1.5">Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="username"
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm text-txt-secondary mb-1.5">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="password"
                className="input-field"
              />
            </div>
          </div>
        )}

        {/* M3U Field */}
        {type === 'M3U_URL' && (
          <div>
            <label className="block text-sm text-txt-secondary mb-1.5">M3U URL</label>
            <input
              type="url"
              value={m3uUrl}
              onChange={(e) => setM3uUrl(e.target.value)}
              placeholder="http://example.com/playlist.m3u"
              className="input-field"
            />
          </div>
        )}

        {/* Error */}
        {error && (
          <p className="text-sm text-error bg-error/10 rounded-lg px-3 py-2">{error}</p>
        )}

        {/* Actions */}
        <div className="flex gap-3 pt-1">
          <button
            type="submit"
            disabled={!isValid || saving}
            className="btn-primary flex-1 py-3 disabled:opacity-40 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none"
          >
            {saving ? (
              <span className="flex items-center justify-center gap-2">
                <div className="spinner" style={{ width: 16, height: 16, borderWidth: 2 }} />
                Saving...
              </span>
            ) : (
              isEditing ? 'Update Playlist' : 'Add Playlist'
            )}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary py-3 px-6"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
