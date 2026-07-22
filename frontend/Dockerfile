# syntax=docker/dockerfile:1

# ---- Build stage: produce the static CRA bundle ----
FROM node:18-alpine AS build
WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

COPY . .

# Where the browser reaches the backend. Overridable at build time.
ARG REACT_APP_API_BASE_URL=http://localhost:8080/mtm-dev/api/v1
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

RUN yarn build

# ---- Runtime stage: serve the bundle with nginx ----
FROM nginx:1.27-alpine AS runtime
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
