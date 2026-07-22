/** @type {import('tailwindcss').Config} */
const defaultTheme = require("tailwindcss/defaultTheme");

/**
 * Theme colors are backed by CSS custom properties (RGB triplets defined in
 * src/style/tailwind.css). Flipping [data-theme] on <html> reskins every
 * utility built from these tokens — no per-element dark: variants required.
 */
const withVar = (name) => `rgb(var(${name}) / <alpha-value>)`;

module.exports = {
  mode: "jit",
  content: ["./src/**/*.{html,js,jsx}"],
  darkMode: ["class", '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        bg: withVar("--mtm-bg-rgb"),
        surface: withVar("--mtm-surface-rgb"),
        "surface-2": withVar("--mtm-surface-2-rgb"),
        line: withVar("--mtm-border-rgb"),
        content: withVar("--mtm-content-rgb"),
        muted: withVar("--mtm-muted-rgb"),
        primary: {
          DEFAULT: withVar("--mtm-primary-rgb"),
          hover: withVar("--mtm-primary-hover-rgb"),
          soft: withVar("--mtm-primary-soft-rgb"),
        },
        accent: withVar("--mtm-accent-rgb"),
        ok: withVar("--mtm-success-rgb"),
        danger: withVar("--mtm-error-rgb"),
        warn: withVar("--mtm-warning-rgb"),
      },
      fontFamily: {
        sans: ["Inter", ...defaultTheme.fontFamily.sans],
        display: ["Sora", "Inter", ...defaultTheme.fontFamily.sans],
      },
      boxShadow: {
        card: "0 10px 30px -12px rgb(15 23 42 / 0.18)",
      },
    },
  },
  prefix: "mtm-",
  plugins: [],
  corePlugins: {
    preflight: true,
  },
};
