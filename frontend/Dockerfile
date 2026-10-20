ARG NODE_IMAGE=node:24.22.0-alpine
ARG NGINX_IMAGE=nginx:1.30.6-alpine-slim

FROM ${NODE_IMAGE} AS builder
WORKDIR /application

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
ARG VITE_API_BASE_URL=http://localhost:8080
ARG VITE_DEMO_MODE=false
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_DEMO_MODE=${VITE_DEMO_MODE}
RUN npm run build

FROM ${NGINX_IMAGE} AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /application/dist /usr/share/nginx/html

EXPOSE 80
