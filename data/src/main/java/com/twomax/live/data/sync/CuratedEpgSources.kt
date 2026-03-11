package com.twomax.live.data.sync

import com.twomax.live.core.model.PublicEpgSource

object CuratedEpgSources {
    val all = listOf(
        PublicEpgSource(label = "USA – DirecTV",        region = "US",    url = "https://iptv-org.github.io/epg/guides/us/directv.com.epg.xml", isEnabled = true),
        PublicEpgSource(label = "USA – TVTVus",         region = "US",    url = "https://iptv-org.github.io/epg/guides/us/tvtv.us.epg.xml", isEnabled = true),
        PublicEpgSource(label = "UK – BBC / Sky",       region = "UK",    url = "https://iptv-org.github.io/epg/guides/gb/bbc.co.uk.epg.xml", isEnabled = true),
        PublicEpgSource(label = "Canada – TVPassport",  region = "CA",    url = "https://iptv-org.github.io/epg/guides/ca/tvpassport.com.epg.xml", isEnabled = true),
        PublicEpgSource(label = "France",               region = "FR",    url = "https://iptv-org.github.io/epg/guides/fr/telecablesat.fr.epg.xml", isEnabled = true),
        PublicEpgSource(label = "Germany",              region = "DE",    url = "https://iptv-org.github.io/epg/guides/de/sky.de.epg.xml", isEnabled = true),
        PublicEpgSource(label = "Spain",                region = "ES",    url = "https://iptv-org.github.io/epg/guides/es/movistar.es.epg.xml", isEnabled = true),
        PublicEpgSource(label = "India – Airtel",       region = "IN",    url = "https://iptv-org.github.io/epg/guides/in/airtel.epg.xml", isEnabled = true),
        PublicEpgSource(label = "UAE / Middle East",    region = "AE",    url = "https://iptv-org.github.io/epg/guides/ae/du.ae.epg.xml", isEnabled = true),
        PublicEpgSource(label = "Australia",            region = "AU",    url = "https://iptv-org.github.io/epg/guides/au/freeview.com.au.epg.xml", isEnabled = true),
        PublicEpgSource(label = "Pluto TV (Global)",    region = "Mixed", url = "https://i.mjh.nz/PlutoTV/all.xml", isEnabled = true),
        PublicEpgSource(label = "Samsung TV+ (Global)", region = "Mixed", url = "https://i.mjh.nz/SamsungTVPlus/all.xml", isEnabled = true),
    )
}
