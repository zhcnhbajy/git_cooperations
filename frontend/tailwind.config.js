/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eef7ff',
          100: '#d9edff',
          200: '#bce0ff',
          300: '#8ecdff',
          400: '#59b1ff',
          500: '#3395ff',
          600: '#1b78f5',
          700: '#1461e1',
          800: '#184fb6',
          900: '#19448f'
        }
      }
    }
  },
  plugins: []
}
