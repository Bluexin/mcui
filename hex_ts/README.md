# themes submodule

This module is a lightweight TypeScript → Lua project meant for authoring MCUI themes using the ambient/runtime typings
provided by the `common` module and the KSP‑generated typings.

It uses TypeScriptToLua (TSTL) and produces `.lua` files in `dist/`.

## Prerequisites

- Node.js 18+ and npm
- Run a build of the `common` module first if you need fresh generated typings in
  `common/build/generated/ksp/main/resources/typings`.

## Typings

The module is configured (tsconfig.json) to include:

- Ambient globals: `../common/src/main/resources/assets/mcui/library/**/*.d.ts`
- Generated module typings: `../common/build/generated/ksp/main/resources/typings/**/*.d.ts`

This gives you IntelliSense for `settings`, `theme`, `support` globals and for generated types such as `Setting`,
`Widget`, etc.

## Commands

- npm install (first run):

  npm install

- Build TS → Lua (outputs to ./dist):

  npm run build

- Via Gradle (from repo root):

  ./gradlew :themes:build

The Gradle task will run `npm install` (or `npm ci` if a lockfile exists) and then `npm run build`.

## Output

- Compiled Lua files are placed in `dist/`.
- An optional Gradle artifact task `distZip` zips `dist/**/*.lua` into `build/luaDist/themes-lua.zip`.

## Example

A sample script lives at `src/example/theme.ts` to validate the pipeline. You can remove it when adding real themes.
