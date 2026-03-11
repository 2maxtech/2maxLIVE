import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'

const PIN_REGEX = /^\d{4}$/

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { macAddress, pin } = body

    if (!macAddress) {
      return NextResponse.json(
        { error: 'MAC address is required' },
        { status: 400 }
      )
    }

    if (!pin || !PIN_REGEX.test(pin)) {
      return NextResponse.json(
        { error: 'Pin must be exactly 4 digits' },
        { status: 400 }
      )
    }

    const normalizedMac = macAddress.toUpperCase()

    const device = await prisma.device.findUnique({
      where: { macAddress: normalizedMac },
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    await prisma.device.update({
      where: { id: device.id },
      data: { isLocked: true, lockPin: pin },
    })

    return NextResponse.json({ locked: true })
  } catch (error) {
    console.error('Device lock error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
