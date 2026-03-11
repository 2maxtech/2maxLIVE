import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'
import { MAX_PLAYLISTS_PER_DEVICE } from '@/lib/constants'

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
      include: { activation: true },
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    if (device.activation?.status !== 'ACTIVE') {
      return NextResponse.json(
        { error: 'Device is not activated' },
        { status: 403 }
      )
    }

    const playlists = await prisma.playlist.findMany({
      where: { deviceId: device.id },
      orderBy: { sortOrder: 'asc' },
    })

    return NextResponse.json(playlists)
  } catch (error) {
    console.error('Playlist GET error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    const { macAddress, name, type, serverUrl, username, password, m3uUrl, sortOrder } = body

    if (!macAddress || !name || !type) {
      return NextResponse.json(
        { error: 'macAddress, name, and type are required' },
        { status: 400 }
      )
    }

    const normalizedMac = macAddress.toUpperCase()

    const device = await prisma.device.findUnique({
      where: { macAddress: normalizedMac },
      include: { activation: true, playlists: true },
    })

    if (!device) {
      return NextResponse.json(
        { error: 'Device not found' },
        { status: 404 }
      )
    }

    if (device.activation?.status !== 'ACTIVE') {
      return NextResponse.json(
        { error: 'Device is not activated' },
        { status: 403 }
      )
    }

    if (device.playlists.length >= MAX_PLAYLISTS_PER_DEVICE) {
      return NextResponse.json(
        { error: `Maximum of ${MAX_PLAYLISTS_PER_DEVICE} playlists per device reached` },
        { status: 400 }
      )
    }

    // Auto-assign sortOrder if not provided
    let assignedSortOrder = sortOrder
    if (assignedSortOrder === undefined || assignedSortOrder === null) {
      const usedOrders = device.playlists.map((p) => p.sortOrder)
      for (let i = 0; i < MAX_PLAYLISTS_PER_DEVICE; i++) {
        if (!usedOrders.includes(i)) {
          assignedSortOrder = i
          break
        }
      }
    }

    const playlist = await prisma.playlist.create({
      data: {
        deviceId: device.id,
        name,
        type,
        serverUrl: serverUrl ?? null,
        username: username ?? null,
        password: password ?? null,
        m3uUrl: m3uUrl ?? null,
        sortOrder: assignedSortOrder ?? 0,
      },
    })

    return NextResponse.json(playlist, { status: 201 })
  } catch (error) {
    console.error('Playlist POST error:', error)
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    )
  }
}
