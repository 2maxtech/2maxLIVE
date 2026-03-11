import Link from 'next/link'

export default function Home() {
  return (
    <div className="relative min-h-screen overflow-hidden">
      {/* Background gradient orbs */}
      <div className="fixed inset-0 pointer-events-none z-0">
        <div className="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] rounded-full bg-primary/20 blur-[120px]" />
        <div className="absolute top-[10%] right-[-15%] w-[500px] h-[500px] rounded-full bg-primary-dark/15 blur-[100px]" />
        <div className="absolute bottom-[-10%] left-[30%] w-[400px] h-[400px] rounded-full bg-accent/10 blur-[100px]" />
      </div>

      {/* Content wrapper */}
      <div className="relative z-10">
        {/* Navigation */}
        <nav className="flex items-center justify-between px-6 md:px-12 py-5">
          <div className="flex items-center gap-3">
            <img src="/logo.png" alt="2maX player" className="h-24 w-auto" />
          </div>
          <Link
            href="/activation"
            className="btn-primary text-sm px-6 py-2.5 inline-block no-underline"
          >
            Activate Device
          </Link>
        </nav>

        {/* Hero Section */}
        <section className="flex flex-col items-center text-center px-6 pt-20 pb-28 md:pt-32 md:pb-36">
          <div className="animate-fade-in-up">
            <span className="badge badge-active mb-6 inline-flex">
              <span className="w-2 h-2 rounded-full bg-success animate-pulse" />
              Now Available
            </span>
          </div>

          <h1 className="text-5xl md:text-7xl lg:text-8xl font-extrabold leading-[1.05] tracking-tight max-w-5xl animate-fade-in-up animate-delay-1">
            Your Ultimate{' '}
            <span className="bg-gradient-to-r from-primary-light via-primary to-accent bg-clip-text text-transparent">
              Streaming
            </span>{' '}
            Experience
          </h1>

          <p className="mt-6 text-lg md:text-xl text-txt-secondary max-w-2xl leading-relaxed animate-fade-in-up animate-delay-2">
            2maX player is a premium Android TV media player that lets you manage
            your IPTV playlists, enjoy EPG guides, and stream your content with
            a beautiful, intuitive interface.
          </p>

          <div className="mt-10 flex flex-col sm:flex-row gap-4 animate-fade-in-up animate-delay-3">
            <Link
              href="/activation"
              className="btn-primary text-base px-10 py-4 glow-purple inline-block no-underline"
              style={{ animation: 'pulse-glow 3s ease-in-out infinite' }}
            >
              Activate Your Device
            </Link>
            <a
              href="#how-it-works"
              className="btn-secondary text-base px-10 py-4 inline-block no-underline text-center"
            >
              Learn More
            </a>
          </div>
        </section>

        {/* How It Works */}
        <section id="how-it-works" className="px-6 md:px-12 py-20 max-w-6xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-4 animate-fade-in-up">
            How It Works
          </h2>
          <p className="text-txt-secondary text-center mb-16 text-lg animate-fade-in-up animate-delay-1">
            Get started in three simple steps
          </p>

          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                step: '01',
                title: 'Install 2maX player',
                description:
                  'Download and install the 2maX player app on your Android TV device from the app store.',
                icon: (
                  <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" />
                  </svg>
                ),
              },
              {
                step: '02',
                title: 'Find Your MAC Address',
                description:
                  'Open the app and navigate to the activation screen. Your unique device MAC address will be displayed.',
                icon: (
                  <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M7.5 3.75H6A2.25 2.25 0 003.75 6v1.5M16.5 3.75H18A2.25 2.25 0 0120.25 6v1.5m0 9V18A2.25 2.25 0 0118 20.25h-1.5m-9 0H6A2.25 2.25 0 013.75 18v-1.5M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                ),
              },
              {
                step: '03',
                title: 'Activate Online',
                description:
                  'Enter your MAC address on this website to activate your device and start managing your playlists.',
                icon: (
                  <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                ),
              },
            ].map((item, i) => (
              <div
                key={item.step}
                className={`glass-card p-8 relative group hover:border-primary/30 transition-all duration-300 animate-fade-in-up animate-delay-${i + 2}`}
              >
                <div className="absolute -top-4 -left-2 text-6xl font-black text-primary/10 select-none">
                  {item.step}
                </div>
                <div className="w-14 h-14 rounded-2xl bg-primary/10 border border-primary/20 flex items-center justify-center text-primary mb-5 group-hover:bg-primary/20 transition-colors">
                  {item.icon}
                </div>
                <h3 className="text-xl font-semibold mb-3">{item.title}</h3>
                <p className="text-txt-secondary leading-relaxed">{item.description}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Features */}
        <section className="px-6 md:px-12 py-20 max-w-6xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-4 animate-fade-in-up">
            Powerful Features
          </h2>
          <p className="text-txt-secondary text-center mb-16 text-lg animate-fade-in-up animate-delay-1">
            Everything you need for the best streaming experience
          </p>

          <div className="grid sm:grid-cols-2 gap-6">
            {[
              {
                title: 'Xtream Codes Support',
                description:
                  'Connect to your Xtream Codes compatible services with full support for live TV, VOD, and series.',
                color: 'from-primary to-primary-dark',
                icon: (
                  <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M8.288 15.038a5.25 5.25 0 017.424 0M5.106 11.856c3.807-3.808 9.98-3.808 13.788 0M1.924 8.674c5.565-5.565 14.587-5.565 20.152 0M12.53 18.22l-.53.53-.53-.53a.75.75 0 011.06 0z" />
                  </svg>
                ),
              },
              {
                title: 'M3U Playlist Support',
                description:
                  'Import and manage M3U/M3U8 playlist files with automatic parsing and channel organization.',
                color: 'from-accent to-cyan-600',
                icon: (
                  <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.375 19.5h17.25m-17.25 0a1.125 1.125 0 01-1.125-1.125M3.375 19.5h7.5c.621 0 1.125-.504 1.125-1.125m-9.75 0V5.625m0 12.75v-1.5c0-.621.504-1.125 1.125-1.125m18.375 2.625V5.625m0 12.75c0 .621-.504 1.125-1.125 1.125m1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125m0 3.75h-7.5A1.125 1.125 0 0112 18.375m9.75-12.75c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125m19.5 0v1.5c0 .621-.504 1.125-1.125 1.125M2.25 5.625v1.5c0 .621.504 1.125 1.125 1.125m0 0h17.25m-17.25 0h7.5c.621 0 1.125.504 1.125 1.125M3.375 8.25c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125m17.25-3.75h-7.5c-.621 0-1.125.504-1.125 1.125m8.625-1.125c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125m-17.25 0h7.5m-7.5 0c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125M12 10.875v-1.5m0 1.5c0 .621-.504 1.125-1.125 1.125M12 10.875c0 .621.504 1.125 1.125 1.125m-2.25 0c.621 0 1.125.504 1.125 1.125M13.125 12h7.5m-7.5 0c-.621 0-1.125.504-1.125 1.125M20.625 12c.621 0 1.125.504 1.125 1.125v1.5c0 .621-.504 1.125-1.125 1.125m-17.25 0h7.5M12 14.625v-1.5m0 1.5c0 .621-.504 1.125-1.125 1.125M12 14.625c0 .621.504 1.125 1.125 1.125m-2.25 0c.621 0 1.125.504 1.125 1.125m0 0v1.5c0 .621-.504 1.125-1.125 1.125M3.375 15.75h7.5" />
                  </svg>
                ),
              },
              {
                title: 'EPG Program Guide',
                description:
                  'Full electronic program guide with current and upcoming shows, timeline view, and program details.',
                color: 'from-emerald-500 to-green-600',
                icon: (
                  <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                  </svg>
                ),
              },
              {
                title: 'Multi-Playlist Management',
                description:
                  'Add, organize, and switch between multiple playlists. Manage all your content sources in one place.',
                color: 'from-amber-500 to-orange-600',
                icon: (
                  <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
                  </svg>
                ),
              },
            ].map((feature, i) => (
              <div
                key={feature.title}
                className={`glass-card p-8 group hover:border-primary/30 transition-all duration-300 animate-fade-in-up animate-delay-${i + 2}`}
              >
                <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${feature.color} flex items-center justify-center text-white mb-5 group-hover:scale-110 transition-transform duration-300`}>
                  {feature.icon}
                </div>
                <h3 className="text-xl font-semibold mb-3">{feature.title}</h3>
                <p className="text-txt-secondary leading-relaxed">{feature.description}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Legal Disclaimer */}
        <section className="px-6 md:px-12 py-16 max-w-4xl mx-auto">
          <div className="glass-card-sm p-8 text-center border-warning/20">
            <div className="w-12 h-12 rounded-full bg-warning/10 border border-warning/20 flex items-center justify-center mx-auto mb-4">
              <svg className="w-6 h-6 text-warning" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
              </svg>
            </div>
            <p className="text-txt-secondary text-sm leading-relaxed max-w-2xl mx-auto">
              <strong className="text-txt-primary">Legal Disclaimer:</strong>{' '}
              2maX player is a media player application. No content is provided,
              hosted, or distributed. Users are responsible for the legality of
              their content sources.
            </p>
          </div>
        </section>

        {/* Footer */}
        <footer className="px-6 md:px-12 py-10 border-t border-white/5">
          <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <img src="/logo.png" alt="2maX player" className="h-16 w-auto" />
            </div>
            <p className="text-txt-disabled text-sm">
              &copy; {new Date().getFullYear()} 2maX player. All rights reserved.
            </p>
          </div>
        </footer>
      </div>
    </div>
  )
}
