// API Configuration
export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1',
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
} as const;

// Chart Configuration
export const CHART_CONFIG = {
  DEFAULT_HEIGHT: 400,
  COLORS: {
    PRIMARY: '#1976d2',
    SECONDARY: '#dc004e',
    SUCCESS: '#2e7d32',
    WARNING: '#ed6c02',
    ERROR: '#d32f2f',
  },
  ANIMATION_DURATION: 300,
} as const;

// UI Configuration
export const UI_CONFIG = {
  DRAWER_WIDTH: 320,
  MODAL_MAX_WIDTH: 800,
  MODAL_MAX_HEIGHT: '90vh',
  TABLE_MAX_HEIGHT: 400,
  SPACING: {
    XS: 0.5,
    SM: 1,
    MD: 2,
    LG: 3,
    XL: 4,
  },
} as const;

// Time Configuration
export const TIME_CONFIG = {
  RANGES: {
    '1M': '1M',
    '3M': '3M',
    '6M': '6M',
    '1Y': '1Y',
  },
  GRANULARITY: {
    YEAR: 'year',
    MONTH: 'month',
    OTHER: 'other',
  },
} as const;

// Dimension Configuration
export const DIMENSION_CONFIG = {
  DEFAULT: 'time',
  COMMON: ['time', 'location'],
  MAX_BUTTONS: 4, // Show dropdown if more than this many dimensions
} as const;

// Chart Types
export const CHART_TYPES = [
  { label: 'Bar', value: 'bar' },
  { label: 'Line', value: 'line' },
] as const;

// View Modes
export const VIEW_MODES = {
  CHART: 'chart',
  TABLE: 'table',
} as const; 