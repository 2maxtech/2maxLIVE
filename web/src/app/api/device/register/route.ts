import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'

const MAC_REGEX = /^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$/

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { macAddress } = body

    if (!macAddress || !MAC_REGEX.test(macAddress)) {
      return NextResponse.json(
        { error: 'Invalid MAC address format. Expected format: XX:XX:XX:XX:XX:XX' },
        { status: 400 }
      )
    }

    const normalizedMac = macAddress.toUpperCase()

    const device = await prisma.device.upsert({
      where: { macAddress: normalizedMac },
      update: { lastCheckedIn: new Date() },
      create: { macAddress: normalizedMac, lastCheckedIn: new Date() },
      include: { activation: true },
    })

    // Auto-expire if activation exists and is past expiresAt
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
      id: device.id,
      macAddress: device.macAddress,
      activated,
    })
  } catch (error) {
    console.error('Device registration error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
