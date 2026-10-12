/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Custom Smart Weather palette
        'weather-navy': '#425B9A', // primary text, headings, buttons, navbar/footer
        'weather-sky': '#76C0EC', // accents, cool temps, active states, card borders
        'weather-cream': '#FFF6DC', // main app background
        'weather-rose': '#FF95A5', // warm temps, alerts, button hover pop
        // Tinted derivatives for surfaces
        'weather-navy-dark': '#33487D',
        'weather-cream-dark': '#F5EAD0',
      },
      boxShadow: {
        card: '0 10px 30px -12px rgba(66, 91, 154, 0.25)',
        pop: '0 6px 18px -6px rgba(255, 149, 165, 0.55)',
      },
      fontFamily: {
        sans: ['"Segoe UI"', 'system-ui', '-apple-system', 'Roboto', '"Helvetica Neue"', 'Arial', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
