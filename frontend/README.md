# Frontend

React + Vite + TypeScript SPA — Spring Boot modular monolith backend'ini tüketir.

## Yığın

- **Framework**: React 19 + Vite + TypeScript
- **Data fetching / state**: TanStack Query + Zustand
- **Auth**: oidc-client-ts + react-oidc-context (Keycloak, Authorization Code + PKCE)
- **UI kit**: Tailwind CSS v4 + shadcn/ui (`@/components/ui`)
- **Form/validasyon**: React Hook Form + Zod
- **Test**: Vitest + React Testing Library + Playwright (e2e)

## Komutlar

```
npm run dev       # dev server
npm run build     # typecheck + production build
npm run lint      # oxlint
```

## shadcn/ui bileşeni ekleme

```
npx shadcn@latest add <component>
```

> Not: Bu ortamda (Windows) shadcn CLI'nin `init` komutu path-alias çözümlemesinde bir hataya (`@` adında literal klasör oluşturma) düşüyor; `add` komutu ise sorunsuz çalışıyor. `components.json` ve tema CSS değişkenleri (`src/index.css`) zaten elle doğru şekilde kuruldu.
