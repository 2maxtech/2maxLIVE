import { NextResponse } from 'next/server'
import { prisma } from '@/lib/prisma'

const PUBLIC_KEYS = ['download_url']

export async function GET() {
  try {
    const settings = await prisma.setting.findMany({
      where: { key: { in: PUBLIC_KEYS } },
    })
    const map: Record<string, string> = {}
    for (const s of settings) {
      map[s.key] = s.value
    }
    return NextResponse.json(map)
  } catch (error) {
    console.error('Public settings error:', error)
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 })
  }
}
