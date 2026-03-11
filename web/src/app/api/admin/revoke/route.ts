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
    const { macAddress } = body

    if (!macAddress) {
      return NextResponse.json(
        { error: 'MAC address is required' },
        { status: 400 }
      )
    }

    const normalizedMac = macAddress.toUpperCase()

    const device = await prisma.device.findUnique({
      where: { macAddress: normalizedMac },
      include: { activation: true },
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    if (!device.activation) {
      return NextResponse.json(
        { error: 'Device has no activation record' },
        { status: 400 }
      )
    }

    await prisma.activation.update({
      where: { id: device.activation.id },
      data: { status: 'REVOKED' },
    })

    return NextResponse.json({ success: true })
  } catch (error) {
    console.error('Admin revoke error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
