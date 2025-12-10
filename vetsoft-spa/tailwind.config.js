/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{html,js,cljs,clj}",
        "./public/index.html"
    ],
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
            colors: {
                primary: '#2563eb',
                primaryDark: '#1e40af',
                secondary: '#64748b',
            }
        }
    },
    plugins: [],
}
