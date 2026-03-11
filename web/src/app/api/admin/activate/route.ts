import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'
import { requireAdmin } from '@/lib/auth'

export async function POST(request: NextRequest) {
  try {
    await requireAdmin(request)
  } catch {
    return NextResponse.json(
      { error: 'Unauthorized' },
      { status: 401 }
    )
  }

  try {
    const body = await request.json()
    const { macAddress, plan, expiresAt } = body

    if (!macAddress) {
      return NextResponse.json(
        { error: 'MAC address is required' },
        { status: 400 }
      )
    }

    const normalizedMac = macAddress.toUpperCase()

    // Create device if not exists
    const device = await prisma.device.upsert({
      where: { macAddress: normalizedMac },
      update: {},
      create: { macAddress: normalizedMac },
    })

    // Create or update activation
    await prisma.activation.upsert({
      where: { deviceId: device.id },
      update: {
        status: 'ACTIVE',
        plan: plan ?? 'MANUAL',
        expiresAt: expiresAt ? new Date(expiresAt) : null,
      },
      create: {
        deviceId: device.id,
        status: 'ACTIVE',
        plan: plan ?? 'MANUAL',
        expiresAt: expiresAt ? new Date(expiresAt) : null,
      },
    })

    const updatedDevice = await prisma.device.findUnique({
      where: { id: device.id },
      include: { activation: true },
    })

    return NextResponse.json(updatedDevice)
  } catch (error) {
    console.error('Admin activate error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
