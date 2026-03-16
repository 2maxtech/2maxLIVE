import { NextRequest, NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'
import { requireAdmin } from '@/lib/auth'

export async function GET(request: NextRequest) {
  try {
    await requireAdmin(request)
  } catch {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })
  }

  try {
    const settings = await prisma.setting.findMany()
    const map: Record<string, string> = {}
    for (const s of settings) {
      map[s.key] = s.value
    }
    return NextResponse.json(map)
  } catch (error) {
    console.error('Settings GET error:', error)
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 })
  }
}

export async function PUT(request: NextRequest) {
  try {
    await requireAdmin(request)
  } catch {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })
  }

  try {
    const body = await request.json()
    const { key, value } = body

    if (!key || typeof key !== 'string') {
      return NextResponse.json({ error: 'Key is required' }, { status: 400 })
    }

    const setting = await prisma.setting.upsert({
      where: { key },
      update: { value: value || '' },
      create: { key, value: value || '' },
    })

    return NextResponse.json(setting)
  } catch (error) {
    console.error('Settings PUT error:', error)
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 })
  }
}
