import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'
import { requireAdmin } from '@/lib/auth'

export async function GET(request: NextRequest) {
  try {
    await requireAdmin(request)
  } catch {
    return NextResponse.json(
      { error: 'Unauthorized' },
      { status: 401 }
    )
  }

  try {
    const [totalDevices, active, expired, revoked, totalPlaylists] = await Promise.all([
      prisma.device.count(),
      prisma.activation.count({ where: { status: 'ACTIVE' } }),
      prisma.activation.count({ where: { status: 'EXPIRED' } }),
      prisma.activation.count({ where: { status: 'REVOKED' } }),
      prisma.playlist.count(),
    ])

    return NextResponse.json({
      totalDevices,
      active,
      expired,
      revoked,
      totalPlaylists,
    })
  } catch (error) {
    console.error('Admin stats error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
