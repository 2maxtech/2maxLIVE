import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        bg: {
          deep: '#0A0A16',
          base: '#0D0D1A',
          surface: '#16162A',
          elevated: '#1E1E3A',
          highest: '#2A2A4A',
        },
        primary: {
          DEFAULT: '#7C4DFF',
          light: '#B388FF',
          dark: '#651FFF',
        },
        accent: '#00E5FF',
        txt: {
          primary: '#EEEEFF',
          secondary: '#9999BB',
          disabled: '#666688',
        },
        success: '#69F0AE',
        error: '#FF5252',
        warning: '#FFD740',
      },
      fontFamily: {
        sans: ['Outfit', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      borderRadius: {
        '2xl': '16px',
        '3xl': '24px',
      },
    },
  },
  plugins: [],
};
export default config;
