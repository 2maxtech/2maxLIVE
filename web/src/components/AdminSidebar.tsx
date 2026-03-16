'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'

const navItems = [
  {
    label: 'Dashboard',
    href: '/admin',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <rect x="14" y="14" width="7" height="7" rx="1" />
      </svg>
    ),
  },
  {
    label: 'Devices',
    href: '/admin/devices',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="3" width="20" height="14" rx="2" />
        <line x1="8" y1="21" x2="16" y2="21" />
        <line x1="12" y1="17" x2="12" y2="21" />
      </svg>
    ),
  },
  {
    label: 'Settings',
    href: '/admin/settings',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
      </svg>
    ),
  },
]

export default function AdminSidebar() {
  const pathname = usePathname()
  const router = useRouter()

  const handleLogout = () => {
    localStorage.removeItem('admin_token')
    router.push('/admin/login')
  }

  const isActive = (href: string) => {
    if (href === '/admin') return pathname === '/admin'
    return pathname.startsWith(href)
  }

  return (
    <aside
      className="fixed left-0 top-0 bottom-0 flex flex-col z-50"
      style={{
        width: 260,
        background: 'var(--bg-surface)',
        borderRight: '1px solid var(--border)',
      }}
    >
      {/* Logo */}
      <div className="px-6 py-6" style={{ borderBottom: '1px solid var(--border)' }}>
        <Link href="/admin" className="flex items-center gap-3 no-underline">
          <img src="/logo.png" alt="2maX player" style={{ height: 64, width: 'auto', maxWidth: 220 }} />
        </Link>
        <div style={{ fontSize: 11, color: 'var(--text-secondary)', letterSpacing: '0.08em', textTransform: 'uppercase' as const, marginTop: 6 }}>
          Admin Panel
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-4 px-3" style={{ overflowY: 'auto' }}>
        {navItems.map((item) => {
          const active = isActive(item.href)
          return (
            <Link
              key={item.href}
              href={item.href}
              className="flex items-center gap-3 px-4 py-3 rounded-lg mb-1 no-underline transition-all"
              style={{
                color: active ? 'var(--text-primary)' : 'var(--text-secondary)',
                background: active ? 'var(--bg-elevated)' : 'transparent',
                borderLeft: active ? '3px solid var(--primary)' : '3px solid transparent',
                fontWeight: active ? 600 : 400,
                fontSize: 15,
                textDecoration: 'none',
              }}
            >
              <span style={{ opacity: active ? 1 : 0.6 }}>{item.icon}</span>
              {item.label}
            </Link>
          )
        })}
      </nav>

      {/* Logout */}
      <div className="px-3 pb-6" style={{ borderTop: '1px solid var(--border)', paddingTop: 16 }}>
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 px-4 py-3 rounded-lg w-full transition-all"
          style={{
            color: 'var(--text-secondary)',
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            fontSize: 15,
            fontFamily: 'inherit',
            textAlign: 'left' as const,
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.color = 'var(--error)'
            e.currentTarget.style.background = 'rgba(255, 82, 82, 0.08)'
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.color = 'var(--text-secondary)'
            e.currentTarget.style.background = 'transparent'
          }}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          Logout
        </button>
      </div>
    </aside>
  )
}
