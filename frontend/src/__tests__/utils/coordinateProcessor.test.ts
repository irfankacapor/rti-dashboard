import {
  generateDataTuples,
  extractUniqueValues,
  validateDimensionMappings,
  generateTuplePreview
} from '@/utils/coordinateProcessor';
import { DimensionMapping, CellSelection } from '@/types/csvProcessing';

// Austrian federal states example data
const austrianCsvData = [
  ['Year', 'Bundesland', 'KMU mit zumindest grundlegender Digitalisierungsintensität', 'Unternehmen mit elektronischem Informationsaustausch'],
  ['2023', 'Burgenland', '49', '32'],
  ['2023', 'Kärnten', '54', '31'],
  ['2023', 'Niederösterreich', '51', '41'],
  ['2023', 'Oberösterreich', '53', '38'],
  ['2023', 'Salzburg', '56', '42'],
  ['2023', 'Steiermark', '52', '39'],
  ['2023', 'Tirol', '55', '40'],
  ['2023', 'Vorarlberg', '58', '45'],
  ['2023', 'Wien', '60', '48']
];

// Test data for additional dimension testing (includes Gender column)
const austrianCsvDataWithGender = [
  ['Year', 'Bundesland', 'KMU mit zumindest grundlegender Digitalisierungsintensität', 'Unternehmen mit elektronischem Informationsaustausch', 'Gender'],
  ['2023', 'Burgenland', '49', '32', 'Male'],
  ['2023', 'Kärnten', '54', '31', 'Female']
];

describe('coordinateProcessor', () => {
  describe('extractUniqueValues', () => {
    it('extracts unique values from a cell selection', () => {
      const selection: CellSelection = {
        startRow: 1,
        endRow: 9,
        startCol: 0,
        endCol: 0,
        selectedCells: [
          { row: 1, col: 0, value: '2023' },
          { row: 2, col: 0, value: '2023' },
          { row: 3, col: 0, value: '2023' },
          { row: 4, col: 0, value: '2023' },
          { row: 5, col: 0, value: '2023' },
          { row: 6, col: 0, value: '2023' },
          { row: 7, col: 0, value: '2023' },
          { row: 8, col: 0, value: '2023' },
          { row: 9, col: 0, value: '2023' }
        ],
        selectionId: 'test-selection'
      };

      const uniqueValues = extractUniqueValues(selection, austrianCsvData);
      expect(uniqueValues).toEqual(['2023']);
    });

    it('handles empty values', () => {
      const selection: CellSelection = {
        startRow: 0,
        endRow: 0,
        startCol: 0,
        endCol: 0,
        selectedCells: [
          { row: 0, col: 0, value: '' },
          { row: 0, col: 1, value: 'Year' }
        ],
        selectionId: 'test-selection'
      };

      const uniqueValues = extractUniqueValues(selection, austrianCsvData);
      expect(uniqueValues).toEqual(['Year']);
    });
  });

  describe('validateDimensionMappings', () => {
    it('validates correct mappings', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 0,
            endCol: 0,
            selectedCells: [],
            selectionId: 'selection-1'
          },
          dimensionType: 'time',
          subType: 'year',
          uniqueValues: ['2023'],
          color: '#2196f3'
        },
        {
          id: 'mapping-2',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 1,
            endCol: 1,
            selectedCells: [],
            selectionId: 'selection-2'
          },
          dimensionType: 'locations',
          subType: 'state',
          uniqueValues: ['Burgenland', 'Kärnten', 'Niederösterreich'],
          color: '#4caf50'
        },
        {
          id: 'mapping-3',
          selection: {
            startRow: 0,
            endRow: 0,
            startCol: 2,
            endCol: 3,
            selectedCells: [],
            selectionId: 'selection-3'
          },
          dimensionType: 'indicator_names',
          uniqueValues: ['KMU mit zumindest grundlegender Digitalisierungsintensität'],
          color: '#ff9800'
        },
        {
          id: 'mapping-4',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 2,
            endCol: 3,
            selectedCells: [],
            selectionId: 'selection-4'
          },
          dimensionType: 'indicator_values',
          uniqueValues: ['49', '54', '51'],
          color: '#e91e63'
        }
      ];

      const result = validateDimensionMappings(mappings);
      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('detects missing indicator_values mapping', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 0,
            endCol: 0,
            selectedCells: [],
            selectionId: 'selection-1'
          },
          dimensionType: 'time',
          subType: 'year',
          uniqueValues: ['2023'],
          color: '#2196f3'
        }
      ];

      const result = validateDimensionMappings(mappings);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('At least one indicator_values mapping is required');
    });

    it('detects duplicate dimension types', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 0,
            endCol: 0,
            selectedCells: [],
            selectionId: 'selection-1'
          },
          dimensionType: 'time',
          subType: 'year',
          uniqueValues: ['2023'],
          color: '#2196f3'
        },
        {
          id: 'mapping-2',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 1,
            endCol: 1,
            selectedCells: [],
            selectionId: 'selection-2'
          },
          dimensionType: 'time',
          subType: 'month',
          uniqueValues: ['2023'],
          color: '#4caf50'
        },
        {
          id: 'mapping-3',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 2,
            endCol: 3,
            selectedCells: [],
            selectionId: 'selection-3'
          },
          dimensionType: 'indicator_values',
          uniqueValues: ['49', '54', '51'],
          color: '#e91e63'
        }
      ];

      const result = validateDimensionMappings(mappings);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Duplicate dimension types found: time');
    });

    it('validates additional_dimension requires custom name', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 2,
            endCol: 3,
            selectedCells: [],
            selectionId: 'selection-1'
          },
          dimensionType: 'indicator_values',
          uniqueValues: ['49', '54', '51'],
          color: '#e91e63'
        },
        {
          id: 'mapping-2',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 4,
            endCol: 4,
            selectedCells: [],
            selectionId: 'selection-2'
          },
          dimensionType: 'additional_dimension',
          uniqueValues: ['Male', 'Female'],
          color: '#9c27b0'
        }
      ];

      const result = validateDimensionMappings(mappings);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Additional dimensions must have a custom name');
    });
  });

  describe('generateDataTuples', () => {
    it('generates correct tuples for Austrian federal states example', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 0,
            endCol: 0,
            selectedCells: [
              { row: 1, col: 0, value: '2023' },
              { row: 2, col: 0, value: '2023' },
              { row: 3, col: 0, value: '2023' },
              { row: 4, col: 0, value: '2023' },
              { row: 5, col: 0, value: '2023' },
              { row: 6, col: 0, value: '2023' },
              { row: 7, col: 0, value: '2023' },
              { row: 8, col: 0, value: '2023' },
              { row: 9, col: 0, value: '2023' }
            ],
            selectionId: 'selection-1'
          },
          dimensionType: 'time',
          subType: 'year',
          uniqueValues: ['2023'],
          color: '#2196f3'
        },
        {
          id: 'mapping-2',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 1,
            endCol: 1,
            selectedCells: [
              { row: 1, col: 1, value: 'Burgenland' },
              { row: 2, col: 1, value: 'Kärnten' },
              { row: 3, col: 1, value: 'Niederösterreich' },
              { row: 4, col: 1, value: 'Oberösterreich' },
              { row: 5, col: 1, value: 'Salzburg' },
              { row: 6, col: 1, value: 'Steiermark' },
              { row: 7, col: 1, value: 'Tirol' },
              { row: 8, col: 1, value: 'Vorarlberg' },
              { row: 9, col: 1, value: 'Wien' }
            ],
            selectionId: 'selection-2'
          },
          dimensionType: 'locations',
          subType: 'state',
          uniqueValues: ['Burgenland', 'Kärnten', 'Niederösterreich', 'Oberösterreich', 'Salzburg', 'Steiermark', 'Tirol', 'Vorarlberg', 'Wien'],
          color: '#4caf50'
        },
        {
          id: 'mapping-3',
          selection: {
            startRow: 0,
            endRow: 0,
            startCol: 2,
            endCol: 3,
            selectedCells: [
              { row: 0, col: 2, value: 'KMU mit zumindest grundlegender Digitalisierungsintensität' },
              { row: 0, col: 3, value: 'Unternehmen mit elektronischem Informationsaustausch' }
            ],
            selectionId: 'selection-3'
          },
          dimensionType: 'indicator_names',
          uniqueValues: ['KMU mit zumindest grundlegender Digitalisierungsintensität', 'Unternehmen mit elektronischem Informationsaustausch'],
          color: '#ff9800'
        },
        {
          id: 'mapping-4',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 2,
            endCol: 3,
            selectedCells: [
              { row: 1, col: 2, value: '49' },
              { row: 1, col: 3, value: '32' },
              { row: 2, col: 2, value: '54' },
              { row: 2, col: 3, value: '31' },
              { row: 3, col: 2, value: '51' },
              { row: 3, col: 3, value: '41' },
              { row: 4, col: 2, value: '53' },
              { row: 4, col: 3, value: '38' },
              { row: 5, col: 2, value: '56' },
              { row: 5, col: 3, value: '42' },
              { row: 6, col: 2, value: '52' },
              { row: 6, col: 3, value: '39' },
              { row: 7, col: 2, value: '55' },
              { row: 7, col: 3, value: '40' },
              { row: 8, col: 2, value: '58' },
              { row: 8, col: 3, value: '45' },
              { row: 9, col: 2, value: '60' },
              { row: 9, col: 3, value: '48' }
            ],
            selectionId: 'selection-4'
          },
          dimensionType: 'indicator_values',
          uniqueValues: ['49', '54', '51', '53', '56', '52', '55', '58', '60', '32', '31', '41', '38', '42', '39', '40', '45', '48'],
          color: '#e91e63'
        }
      ];

      const tuples = generateDataTuples(mappings, austrianCsvData);

      // Should generate 18 tuples (9 states × 2 indicators)
      expect(tuples).toHaveLength(18);

      // Check first tuple
      const firstTuple = tuples[0];
      expect(firstTuple.coordinates.time).toBe('2023');
      expect(firstTuple.coordinates.locations).toBe('Burgenland');
      expect(firstTuple.coordinates.indicator_names).toBe('KMU mit zumindest grundlegender Digitalisierungsintensität');
      expect(firstTuple.value).toBe('49');
      expect(firstTuple.sourceRow).toBe(1);
      expect(firstTuple.sourceCol).toBe(2);

      // Check last tuple
      const lastTuple = tuples[17];
      expect(lastTuple.coordinates.time).toBe('2023');
      expect(lastTuple.coordinates.locations).toBe('Wien');
      expect(lastTuple.coordinates.indicator_names).toBe('Unternehmen mit elektronischem Informationsaustausch');
      expect(lastTuple.value).toBe('48');
      expect(lastTuple.sourceRow).toBe(9);
      expect(lastTuple.sourceCol).toBe(3);
    });

    it('throws error when no indicator_values mapping is provided', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 0,
            endCol: 0,
            selectedCells: [],
            selectionId: 'selection-1'
          },
          dimensionType: 'time',
          subType: 'year',
          uniqueValues: ['2023'],
          color: '#2196f3'
        }
      ];

      expect(() => generateDataTuples(mappings, austrianCsvData))
        .toThrow('No indicator values mapped');
    });

    it('handles additional_dimension mappings', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 2,
            startCol: 2,
            endCol: 3,
            selectedCells: [
              { row: 1, col: 2, value: '49' },
              { row: 1, col: 3, value: '32' },
              { row: 2, col: 2, value: '54' },
              { row: 2, col: 3, value: '31' }
            ],
            selectionId: 'selection-1'
          },
          dimensionType: 'indicator_values',
          uniqueValues: ['49', '32', '54', '31'],
          color: '#e91e63'
        },
        {
          id: 'mapping-2',
          selection: {
            startRow: 1,
            endRow: 2,
            startCol: 4,
            endCol: 4,
            selectedCells: [
              { row: 1, col: 4, value: 'Male' },
              { row: 2, col: 4, value: 'Female' }
            ],
            selectionId: 'selection-2'
          },
          dimensionType: 'additional_dimension',
          customDimensionName: 'Gender',
          uniqueValues: ['Male', 'Female'],
          color: '#9c27b0'
        }
      ];

      const tuples = generateDataTuples(mappings, austrianCsvDataWithGender);
      expect(tuples).toHaveLength(4);
      
      // Check that row 1 tuples get "Male"
      expect(tuples[0].coordinates.Gender).toBe('Male'); // (1,2)
      expect(tuples[1].coordinates.Gender).toBe('Male'); // (1,3)
      
      // Check that row 2 tuples get "Female"
      expect(tuples[2].coordinates.Gender).toBe('Female'); // (2,2)
      expect(tuples[3].coordinates.Gender).toBe('Female'); // (2,3)
    });
  });

  describe('generateTuplePreview', () => {
    it('generates preview with limited tuples', () => {
      const mappings: DimensionMapping[] = [
        {
          id: 'mapping-1',
          selection: {
            startRow: 1,
            endRow: 9,
            startCol: 2,
            endCol: 3,
            selectedCells: [
              { row: 1, col: 2, value: '49' },
              { row: 1, col: 3, value: '32' },
              { row: 2, col: 2, value: '54' },
              { row: 2, col: 3, value: '31' }
            ],
            selectionId: 'selection-1'
          },
          dimensionType: 'indicator_values',
          uniqueValues: ['49', '32', '54', '31'],
          color: '#e91e63'
        }
      ];

      const preview = generateTuplePreview(mappings, austrianCsvData, 2);
      expect(preview).toHaveLength(2);
    });
  });
}); 