'use client'

import { usePathname } from 'next/navigation'
import AdminSidebar from '@/components/AdminSidebar'

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname()
  const isLoginPage = pathname === '/admin/login'

  if (isLoginPage) {
    return <>{children}</>
  }

  return (
    <div style={{ minHeight: '100vh' }}>
      <AdminSidebar />
      <main style={{ marginLeft: 260, padding: '32px 40px', minHeight: '100vh' }}>
        {children}
      </main>
    </div>
  )
}
