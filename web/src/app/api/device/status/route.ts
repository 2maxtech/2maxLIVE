import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'

export async function GET(request: NextRequest) {
  try {
    const mac = request.nextUrl.searchParams.get('mac')

    if (!mac) {
      return NextResponse.json(
        { error: 'MAC address is required' },
        { status: 400 }
      )
    }

    const normalizedMac = mac.toUpperCase()

    const device = await prisma.device.findUnique({
      where: { macAddress: normalizedMac },
      include: {
        activation: true,
        playlists: true,
      },
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    // Update lastCheckedIn
    await prisma.device.update({
      where: { id: device.id },
      data: { lastCheckedIn: new Date() },
    })

    // Auto-expire if needed
    if (
      device.activation &&
      device.activation.status === 'ACTIVE' &&
      device.activation.expiresAt &&
      device.activation.expiresAt < new Date()
    ) {
      await prisma.activation.update({
        where: { id: device.activation.id },
        data: { status: 'EXPIRED' },
      })
      device.activation.status = 'EXPIRED'
    }

    const activated = device.activation?.status === 'ACTIVE'

    return NextResponse.json({
      activated,
      plan: device.activation?.plan ?? null,
      expiresAt: device.activation?.expiresAt ?? null,
      playlistCount: device.playlists.length,
      isLocked: device.isLocked,
    })
  } catch (error) {
    console.error('Device status error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
