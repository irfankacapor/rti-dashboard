export const DIMENSION_TYPES = {
  TIME: 'time',
  LOCATION: 'location', 
  INDICATOR_NAME: 'indicator_name',
  INDICATOR_VALUE: 'indicator_value',
  SOURCE: 'source',
  UNIT: 'unit',
  GOAL: 'goal',
  ADDITIONAL: 'additional'
} as const;

export type DimensionType = typeof DIMENSION_TYPES[keyof typeof DIMENSION_TYPES];

export interface Dimension {
  type: DimensionType;     // Now properly typed instead of string
  displayName: string;     // "Time", "Location", "Sector", etc.
  values: string[];        // Available values for this dimension
}