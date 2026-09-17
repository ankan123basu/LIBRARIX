/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        paper: '#F7F5EF',
        'paper-2': '#FFFFFF',
        ink: '#0B0B0B',
        stamp: '#E23A2E',
        highlight: '#FFE14D',
        mint: '#7DE8A8',
        sky: '#9CCBFF',
        blush: '#FFA8CE',
        lilac: '#CDB8FF',
        shadow: '#0B0B0B',
      },
      borderWidth: {
        3: '3px',
      },
      boxShadow: {
        brutal: '6px 6px 0 0 #0B0B0B',
        'brutal-sm': '3px 3px 0 0 #0B0B0B',
        'brutal-lg': '8px 8px 0 0 #0B0B0B',
        'brutal-hover': '4px 4px 0 0 #0B0B0B',
      },
      fontFamily: {
        display: ['var(--font-archivo)', 'sans-serif'],
        body: ['var(--font-space)', 'sans-serif'],
        mono: ['var(--font-mono)', 'monospace'],
      },
    },
  },
  plugins: [],
};
