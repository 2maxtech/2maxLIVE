import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'
import { requireAdmin } from '@/lib/auth'

export async function GET(
  request: NextRequest,
  { params }: { params: { mac: string } }
) {
  try {
    await requireAdmin(request)
  } catch {
    return NextResponse.json(
      { error: 'Unauthorized' },
      { status: 401 }
    )
  }

  try {
    const { mac } = params
    const normalizedMac = mac.toUpperCase()

    const device = await prisma.device.findUnique({
      where: { macAddress: normalizedMac },
      include: {
        activation: true,
        playlists: {
          orderBy: { sortOrder: 'asc' },
        },
      },
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    return NextResponse.json(device)
  } catch (error) {
    console.error('Admin device detail error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
