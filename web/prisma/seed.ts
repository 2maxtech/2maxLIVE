import { PrismaClient } from '@prisma/client'
import bcrypt from 'bcryptjs'

const prisma = new PrismaClient()

async function main() {
  const email = process.env.ADMIN_EMAIL || 'admin@2max.tech'
  const password = process.env.ADMIN_PASSWORD || 'admin123'
  const hash = await bcrypt.hash(password, 12)

  await prisma.adminUser.upsert({
    where: { email },
    update: { passwordHash: hash },
    create: {
      email,
      passwordHash: hash,
      name: 'Admin',
    },
  })

  console.log(`Admin user seeded: ${email}`)

  // Seed default settings
  await prisma.setting.upsert({
    where: { key: 'download_url' },
    update: {},
    create: {
      key: 'download_url',
      value: 'https://ncloud.2max.tech/s/FDyiCYpwZEFmwKW',
    },
  })

  console.log('Default settings seeded')
}

main()
  .catch((e) => {
    console.error(e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })
