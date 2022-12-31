/** @type {import('tailwindcss').Config} */
const defaultTheme = require("tailwindcss/defaultTheme");

module.exports = {
  mode: "jit",
  content: ["./src/**/*.{html,js}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Bangers", ...defaultTheme.fontFamily.sans],
        bal: "Balsamiq Sans",
        lex: "Lexend",
        spel: "Special Elite",
        code: "Nanum Gothic Coding",
      },
    },
  },
  prefix: "mtm-",
  plugins: [],
  corePlugins: {
    preflight: true,
  },
};
