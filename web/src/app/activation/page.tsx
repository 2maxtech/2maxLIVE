'use client'

import { useState, useCallback } from 'react'
import Link from 'next/link'

const MAC_REGEX = /^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$/

function formatMacAddress(value: string): string {
  const hex = value.replace(/[^0-9A-Fa-f]/g, '').toUpperCase().slice(0, 12)
  const parts: string[] = []
  for (let i = 0; i < hex.length; i += 2) {
    parts.push(hex.slice(i, i + 2))
  }
  return parts.join(':')
}

interface RegisterResult {
  id: string
  macAddress: string
  activated: boolean
}

export default function ActivationPage() {
  const [mac, setMac] = useState('')
  const [disclaimer, setDisclaimer] = useState(false)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<RegisterResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  const isValidMac = MAC_REGEX.test(mac)
  const canSubmit = isValidMac && disclaimer && !loading

  const handleMacChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatMacAddress(e.target.value)
    setMac(formatted)
    setResult(null)
    setError(null)
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!canSubmit) return

    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const res = await fetch('/api/device/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ macAddress: mac }),
      })

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'Failed to check device status')
      }

      const data: RegisterResult = await res.json()
      setResult(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-bg-deep relative">
      {/* Background gradient */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-primary/[0.07] rounded-full blur-[120px]" />
      </div>

      <div className="relative z-10 max-w-xl mx-auto px-6 py-16">
        {/* Back link */}
        <Link
          href="/"
          className="inline-flex items-center gap-2 text-txt-secondary hover:text-txt-primary transition-colors mb-12"
        >
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12.5 15L7.5 10L12.5 5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          Back to Home
        </Link>

        {/* Header */}
        <div className="text-center mb-10 animate-fade-in-up">
          <h1 className="text-4xl font-bold text-txt-primary mb-3 glow-text">
            Activate Your Device
          </h1>
          <p className="text-txt-secondary text-lg">
            Enter the MAC address shown on your 2maX player app&apos;s activation screen
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="glass-card p-8 animate-fade-in-up animate-delay-1">
          <div className="space-y-6">
            {/* MAC Input */}
            <div>
              <label htmlFor="mac" className="block text-sm font-medium text-txt-secondary mb-2">
                MAC Address
              </label>
              <input
                id="mac"
                type="text"
                value={mac}
                onChange={handleMacChange}
                placeholder="XX:XX:XX:XX:XX:XX"
                className="input-field input-mono text-center text-xl"
                maxLength={17}
                autoComplete="off"
                spellCheck={false}
              />
              {mac.length > 0 && !isValidMac && (
                <p className="mt-2 text-sm text-error">
                  Enter a complete MAC address (e.g., AA:BB:CC:DD:EE:FF)
                </p>
              )}
            </div>

            {/* Disclaimer */}
            <label className="flex items-start gap-3 cursor-pointer group">
              <div className="relative mt-0.5">
                <input
                  type="checkbox"
                  checked={disclaimer}
                  onChange={(e) => setDisclaimer(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-5 h-5 rounded border border-white/20 bg-bg-elevated peer-checked:bg-primary peer-checked:border-primary transition-all flex items-center justify-center">
                  {disclaimer && (
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path d="M2.5 6L5 8.5L9.5 3.5" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  )}
                </div>
              </div>
              <span className="text-sm text-txt-secondary group-hover:text-txt-primary transition-colors">
                I understand that 2maX player is a media player and does not provide any content
              </span>
            </label>

            {/* Submit */}
            <button
              type="submit"
              disabled={!canSubmit}
              className="btn-primary w-full flex items-center justify-center gap-3 py-4 text-base disabled:opacity-40 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none"
            >
              {loading ? (
                <>
                  <div className="spinner" style={{ width: 20, height: 20, borderWidth: 2 }} />
                  Checking...
                </>
              ) : (
                <>
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <path d="M8 4L14 10L8 16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                  Check Status
                </>
              )}
            </button>
          </div>
        </form>

        {/* Error */}
        {error && (
          <div className="mt-6 glass-card p-6 border-error/30 animate-fade-in-up">
            <div className="flex items-start gap-3">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" className="text-error flex-shrink-0 mt-0.5">
                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="1.5"/>
                <path d="M12 8V12M12 16H12.01" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              </svg>
              <div>
                <h3 className="font-semibold text-error mb-1">Error</h3>
                <p className="text-sm text-txt-secondary">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* Result: Activated */}
        {result?.activated && (
          <div className="mt-6 glass-card p-8 animate-fade-in-up" style={{ borderColor: 'rgba(105, 240, 174, 0.3)' }}>
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-success/10 mb-2">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none" className="text-success">
                  <path d="M8 16L14 22L24 10" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <h2 className="text-2xl font-bold text-success">Device Activated!</h2>
              <p className="text-txt-secondary">
                Your device <span className="font-mono text-txt-primary">{result.macAddress}</span> is active and ready to use.
              </p>
              <Link
                href={`/dashboard?mac=${encodeURIComponent(result.macAddress)}`}
                className="btn-primary inline-flex mt-4"
              >
                Go to Dashboard
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M6 4L10 8L6 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </Link>
            </div>
          </div>
        )}

        {/* Result: Not Activated */}
        {result && !result.activated && (
          <div className="mt-6 glass-card p-8 animate-fade-in-up">
            <div className="text-center space-y-4">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-warning/10 mb-2">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none" className="text-warning">
                  <path d="M16 10V18M16 22H16.01" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
                  <circle cx="16" cy="16" r="13" stroke="currentColor" strokeWidth="2"/>
                </svg>
              </div>
              <h2 className="text-2xl font-bold text-txt-primary">Device Not Yet Activated</h2>
              <p className="text-txt-secondary">
                Contact your administrator to activate this device.
              </p>
              <div className="mt-4 p-4 bg-bg-elevated rounded-xl">
                <p className="text-xs text-txt-disabled mb-1 uppercase tracking-wider">MAC Address</p>
                <p className="font-mono text-lg text-txt-primary tracking-wider">{result.macAddress}</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
