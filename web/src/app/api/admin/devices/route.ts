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
    const search = request.nextUrl.searchParams.get('search') ?? ''
    const page = Math.max(1, parseInt(request.nextUrl.searchParams.get('page') ?? '1', 10))
    const limit = Math.max(1, Math.min(100, parseInt(request.nextUrl.searchParams.get('limit') ?? '20', 10)))
    const skip = (page - 1) * limit

    const where = search
      ? { macAddress: { contains: search.toUpperCase() } }
      : {}

    const [devices, total] = await Promise.all([
      prisma.device.findMany({
        where,
        include: {
          activation: true,
          playlists: { select: { id: true } },
        },
        orderBy: { firstSeen: 'desc' },
        skip,
        take: limit,
      }),
      prisma.device.count({ where }),
    ])

    const data = devices.map((device) => ({
      id: device.id,
      macAddress: device.macAddress,
      isLocked: device.isLocked,
      lastCheckedIn: device.lastCheckedIn,
      firstSeen: device.firstSeen,
      activation: device.activation
        ? {
            status: device.activation.status,
            plan: device.activation.plan,
            expiresAt: device.activation.expiresAt,
          }
        : null,
      playlistCount: device.playlists.length,
    }))

    return NextResponse.json({
      devices: data,
      total,
      page,
      limit,
      totalPages: Math.ceil(total / limit),
    })
  } catch (error) {
    console.error('Admin devices list error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
