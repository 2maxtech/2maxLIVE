'use client'

import { Suspense } from 'react'
import DashboardContent from './DashboardContent'

export default function DashboardPage() {
  return (
    <Suspense fallback={<DashboardSkeleton />}>
      <DashboardContent />
    </Suspense>
  )
}

function DashboardSkeleton() {
  return (
    <div className="min-h-screen bg-bg-deep">
      <div className="max-w-4xl mx-auto px-6 py-8">
        <div className="h-12 bg-bg-surface rounded-xl animate-pulse mb-8" />
        <div className="space-y-6">
          <div className="h-48 bg-bg-surface rounded-2xl animate-pulse" />
          <div className="h-64 bg-bg-surface rounded-2xl animate-pulse" />
          <div className="h-32 bg-bg-surface rounded-2xl animate-pulse" />
        </div>
      </div>
    </div>
  )
}
