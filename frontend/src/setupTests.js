// CRA loads this file (src/setupTests.js) before the test suite.
import "@testing-library/jest-dom";

// jsdom lacks these browser APIs that framer-motion (whileInView / layout)
// relies on. Provide no-op polyfills so component tests can render.
class MockObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
  takeRecords() {
    return [];
  }
}
global.IntersectionObserver = global.IntersectionObserver || MockObserver;
global.ResizeObserver = global.ResizeObserver || MockObserver;

if (!window.matchMedia) {
  window.matchMedia = (query) => ({
    matches: false,
    media: query,
    onchange: null,
    addEventListener() {},
    removeEventListener() {},
    addListener() {},
    removeListener() {},
    dispatchEvent() {
      return false;
    },
  });
}
