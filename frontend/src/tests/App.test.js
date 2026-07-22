import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

// axios v1 ships as ESM which CRA's Jest config does not transform. The app's
// module graph pulls in axios via ApiService, so we replace it with a light
// stub that satisfies the `axios.create(...).interceptors.*.use(...)` calls
// made at import time. The landing page makes no network requests anyway.
jest.mock("axios", () => {
  const client = {
    interceptors: {
      request: { use: jest.fn() },
      response: { use: jest.fn() },
    },
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  };
  return { __esModule: true, default: { ...client, create: () => client } };
});

// eslint-disable-next-line import/first
import App from "../Pages/App";

test("renders the landing hero", () => {
  render(<App />, { wrapper: MemoryRouter });
  expect(screen.getAllByText(/one activity at a time/i).length).toBeGreaterThan(0);
});

test("shows the primary call to action for signed-out visitors", () => {
  render(<App />, { wrapper: MemoryRouter });
  expect(screen.getAllByText(/get started free/i).length).toBeGreaterThan(0);
});
