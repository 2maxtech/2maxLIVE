import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: '2maX player - Activate Your Device',
  description: 'Activate and manage your 2maX player Android TV streaming device. Add playlists, manage activation, and control your entertainment.',
  icons: {
    icon: '/logo.png',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen">
        {children}
      </body>
    </html>
  )
}
