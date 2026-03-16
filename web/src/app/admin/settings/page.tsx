'use client'

import { useEffect, useState, FormEvent } from 'react'
import { useRouter } from 'next/navigation'

export default function AdminSettingsPage() {
  const router = useRouter()
  const [downloadUrl, setDownloadUrl] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  useEffect(() => {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }
    fetchSettings(token)
  }, [router])

  const fetchSettings = async (token: string) => {
    try {
      const res = await fetch('/api/admin/settings', {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }
      if (!res.ok) throw new Error('Failed to load settings')
      const data = await res.json()
      setDownloadUrl(data.download_url || '')
    } catch {
      setMessage({ type: 'error', text: 'Failed to load settings' })
    } finally {
      setLoading(false)
    }
  }

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setMessage(null)

    const token = localStorage.getItem('admin_token')
    if (!token) {
      router.push('/admin/login')
      return
    }

    try {
      const res = await fetch('/api/admin/settings', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ key: 'download_url', value: downloadUrl }),
      })

      if (res.status === 401) {
        localStorage.removeItem('admin_token')
        router.push('/admin/login')
        return
      }

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'Failed to save')
      }

      setMessage({ type: 'success', text: 'Download URL saved successfully' })
    } catch (err) {
      setMessage({ type: 'error', text: err instanceof Error ? err.message : 'Failed to save' })
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div>
        <h1 className="glow-text animate-fade-in-up" style={{ fontSize: 28, fontWeight: 700, margin: 0, marginBottom: 32 }}>
          Settings
        </h1>
        <div className="glass-card" style={{ padding: 32, maxWidth: 600 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div className="spinner" style={{ width: 20, height: 20 }} />
            <span style={{ color: 'var(--text-secondary)' }}>Loading settings...</span>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div>
      <h1 className="glow-text animate-fade-in-up" style={{ fontSize: 28, fontWeight: 700, margin: 0, marginBottom: 32 }}>
        Settings
      </h1>

      {/* Download URL Setting */}
      <div className="glass-card animate-fade-in-up" style={{ padding: 32, maxWidth: 600 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: 10,
              background: 'rgba(124, 77, 255, 0.1)',
              border: '1px solid rgba(124, 77, 255, 0.2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--primary-light)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
          </div>
          <div>
            <h2 style={{ fontSize: 18, fontWeight: 600, margin: 0 }}>APK Download URL</h2>
            <p style={{ fontSize: 13, color: 'var(--text-secondary)', margin: 0, marginTop: 2 }}>
              The URL displayed on the website for Downloader app installation
            </p>
          </div>
        </div>

        <form onSubmit={handleSave}>
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
              Download URL
            </label>
            <input
              type="text"
              className="input-field"
              placeholder="https://ncloud.2max.tech/s/FDyiCYpwZEFmwKW"
              value={downloadUrl}
              onChange={(e) => {
                setDownloadUrl(e.target.value)
                setMessage(null)
              }}
            />
            <p style={{ fontSize: 12, color: 'var(--text-disabled)', marginTop: 6 }}>
              This URL will be shown to users on the landing page as the Downloader code
            </p>
          </div>

          {/* Preview */}
          {downloadUrl && (
            <div
              style={{
                background: 'var(--bg-deep)',
                border: '1px solid var(--border)',
                borderRadius: 12,
                padding: 16,
                marginBottom: 20,
                textAlign: 'center',
              }}
            >
              <p style={{ fontSize: 11, color: 'var(--text-disabled)', textTransform: 'uppercase', letterSpacing: '0.1em', margin: 0, marginBottom: 6 }}>
                Preview — shown on website
              </p>
              <p
                style={{
                  fontSize: 18,
                  fontFamily: 'var(--font-geist-mono), monospace',
                  fontWeight: 700,
                  color: 'var(--primary-light)',
                  margin: 0,
                  wordBreak: 'break-all',
                }}
              >
                {downloadUrl.replace(/^https?:\/\//, '')}
              </p>
            </div>
          )}

          {message && (
            <div
              style={{
                background: message.type === 'success' ? 'rgba(105, 240, 174, 0.1)' : 'rgba(255, 82, 82, 0.1)',
                border: `1px solid ${message.type === 'success' ? 'rgba(105, 240, 174, 0.25)' : 'rgba(255, 82, 82, 0.25)'}`,
                borderRadius: 10,
                padding: '10px 14px',
                marginBottom: 20,
                color: message.type === 'success' ? 'var(--success)' : 'var(--error)',
                fontSize: 14,
              }}
            >
              {message.text}
            </div>
          )}

          <button
            type="submit"
            className="btn-primary"
            disabled={saving}
            style={{ opacity: saving ? 0.7 : 1 }}
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </form>
      </div>
    </div>
  )
}
