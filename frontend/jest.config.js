const nextJest = require('next/jest')

const createJestConfig = nextJest({
  dir: './',
})

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: 'jest-environment-jsdom',
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/*.stories.{ts,tsx}',
    '!src/**/__tests__/**',
    '!src/app/**/layout.tsx',
    '!src/app/**/loading.tsx', 
    '!src/app/**/error.tsx',
    '!src/app/**/not-found.tsx',
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80,
    },
  },
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    '^next-intl$': '<rootDir>/__mocks__/next-intl.js',
    '^next-intl/(.*)$': '<rootDir>/__mocks__/next-intl.js',
  },
}

module.exports = createJestConfig(customJestConfig)