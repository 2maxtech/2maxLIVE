export interface DeviceResponse {
  id: string
  macAddress: string
  activated: boolean
  plan?: string
  expiresAt?: string | null
  playlistCount?: number
  isLocked?: boolean
}

export interface PlaylistResponse {
  id: string
  name: string
  type: 'XTREAM' | 'M3U_URL'
  serverUrl?: string | null
  username?: string | null
  password?: string | null
  m3uUrl?: string | null
  sortOrder: number
  isActive: boolean
}

export interface PlaylistInput {
  macAddress: string
  name: string
  type: 'XTREAM' | 'M3U_URL'
  serverUrl?: string
  username?: string
  password?: string
  m3uUrl?: string
  sortOrder?: number
}

export interface AdminStats {
  totalDevices: number
  active: number
  expired: number
  revoked: number
  totalPlaylists: number
}

export interface AdminDeviceListItem {
  id: string
  macAddress: string
  firstSeen: string
  lastCheckedIn: string | null
  isLocked: boolean
  activation: {
    plan: string
    status: string
    activatedAt: string
    expiresAt: string | null
  } | null
  _count: {
    playlists: number
  }
}
