'use client'

interface DeviceStatusCardProps {
  status: string
  plan: string
  expiresAt: string | null
  activatedAt?: string
}

function getStatusBadge(status: string) {
  const s = status.toUpperCase()
  if (s === 'ACTIVE') return { className: 'badge badge-active', label: 'Active' }
  if (s === 'EXPIRED') return { className: 'badge badge-expired', label: 'Expired' }
  if (s === 'REVOKED') return { className: 'badge badge-revoked', label: 'Revoked' }
  return { className: 'badge badge-inactive', label: status }
}

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return 'No expiry'
  try {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  } catch {
    return dateStr
  }
}

export default function DeviceStatusCard({ status, plan, expiresAt, activatedAt }: DeviceStatusCardProps) {
  const badge = getStatusBadge(status)

  return (
    <div className="glass-card p-6">
      <h2 className="text-lg font-semibold text-txt-primary mb-5">Activation Status</h2>

      <div className="space-y-4">
        {/* Status */}
        <div className="flex items-center justify-between">
          <span className="text-sm text-txt-secondary">Status</span>
          <span className={badge.className}>{badge.label}</span>
        </div>

        {/* Plan */}
        <div className="flex items-center justify-between">
          <span className="text-sm text-txt-secondary">Plan</span>
          <span className="text-sm font-medium text-txt-primary">{plan || 'N/A'}</span>
        </div>

        {/* Expiry */}
        <div className="flex items-center justify-between">
          <span className="text-sm text-txt-secondary">Expires</span>
          <span className="text-sm text-txt-primary">{formatDate(expiresAt)}</span>
        </div>

        {/* Activated date */}
        {activatedAt && (
          <div className="flex items-center justify-between">
            <span className="text-sm text-txt-secondary">Activated</span>
            <span className="text-sm text-txt-primary">{formatDate(activatedAt)}</span>
          </div>
        )}
      </div>
    </div>
  )
}
