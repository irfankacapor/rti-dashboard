import { generateDataTuples } from './coordinateProcessor';
import { DimensionMapping } from '@/types/csvProcessing';

describe('coordinateProcessor - Matrix CSV Layout', () => {
  // Example CSV data matching the user's description
  const matrixCsvData = [
    ['', '', '', '', '', '', '', ''], // Row 0 (empty)
    ['', 'weiblich', 'weiblich', 'weiblich', 'weiblich', 'männlich', 'männlich', 'männlich', 'männlich'], // Row 1: Gender
    ['', '2023', '2023', '2024', '2024', '2023', '2023', '2024', '2024'], // Row 2: Year
    ['', 'Anzahl', '%', 'Anzahl', '%', 'Anzahl', '%', 'Anzahl', '%'], // Row 3: Indicator Names
    ['', '85', '70', '63', '77', '38', '31', '19', '23'] // Row 4: Values
  ];

  const mappings: DimensionMapping[] = [
    {
      id: 'gender-mapping',
      dimensionType: 'additional_dimension',
      customDimensionName: 'Gender',
      mappingDirection: 'row',
      selection: {
        startRow: 1,
        endRow: 1,
        startCol: 1,
        endCol: 8,
        selectedCells: [
          { row: 1, col: 1, value: 'weiblich' },
          { row: 1, col: 2, value: 'weiblich' },
          { row: 1, col: 3, value: 'weiblich' },
          { row: 1, col: 4, value: 'weiblich' },
          { row: 1, col: 5, value: 'männlich' },
          { row: 1, col: 6, value: 'männlich' },
          { row: 1, col: 7, value: 'männlich' },
          { row: 1, col: 8, value: 'männlich' }
        ],
        selectionId: 'gender-selection'
      },
      color: '#2196f3',
      uniqueValues: ['weiblich', 'männlich']
    },
    {
      id: 'year-mapping',
      dimensionType: 'time',
      subType: 'year',
      mappingDirection: 'row',
      selection: {
        startRow: 2,
        endRow: 2,
        startCol: 1,
        endCol: 8,
        selectedCells: [
          { row: 2, col: 1, value: '2023' },
          { row: 2, col: 2, value: '2023' },
          { row: 2, col: 3, value: '2024' },
          { row: 2, col: 4, value: '2024' },
          { row: 2, col: 5, value: '2023' },
          { row: 2, col: 6, value: '2023' },
          { row: 2, col: 7, value: '2024' },
          { row: 2, col: 8, value: '2024' }
        ],
        selectionId: 'year-selection'
      },
      color: '#4caf50',
      uniqueValues: ['2023', '2024']
    },
    {
      id: 'indicator-names-mapping',
      dimensionType: 'indicator_names',
      mappingDirection: 'row',
      selection: {
        startRow: 3,
        endRow: 3,
        startCol: 1,
        endCol: 8,
        selectedCells: [
          { row: 3, col: 1, value: 'Anzahl' },
          { row: 3, col: 2, value: '%' },
          { row: 3, col: 3, value: 'Anzahl' },
          { row: 3, col: 4, value: '%' },
          { row: 3, col: 5, value: 'Anzahl' },
          { row: 3, col: 6, value: '%' },
          { row: 3, col: 7, value: 'Anzahl' },
          { row: 3, col: 8, value: '%' }
        ],
        selectionId: 'indicator-names-selection'
      },
      color: '#ff9800',
      uniqueValues: ['Anzahl', '%']
    },
    {
      id: 'values-mapping',
      dimensionType: 'indicator_values',
      mappingDirection: 'row',
      selection: {
        startRow: 4,
        endRow: 4,
        startCol: 1,
        endCol: 8,
        selectedCells: [
          { row: 4, col: 1, value: '85' },
          { row: 4, col: 2, value: '70' },
          { row: 4, col: 3, value: '63' },
          { row: 4, col: 4, value: '77' },
          { row: 4, col: 5, value: '38' },
          { row: 4, col: 6, value: '31' },
          { row: 4, col: 7, value: '19' },
          { row: 4, col: 8, value: '23' }
        ],
        selectionId: 'values-selection'
      },
      color: '#e91e63',
      uniqueValues: ['85', '70', '63', '77', '38', '31', '19', '23']
    }
  ];

  it('should correctly process matrix-style CSV with row-based dimension mappings', () => {
    const tuples = generateDataTuples(mappings, matrixCsvData);

    // Should generate 8 tuples (one for each value)
    expect(tuples).toHaveLength(8);

    // Verify the first tuple (Anzahl, weiblich, 2023, 85)
    const firstTuple = tuples[0];
    expect(firstTuple.coordinates.indicator_names).toBe('Anzahl');
    expect(firstTuple.coordinates.Gender).toBe('weiblich');
    expect(firstTuple.coordinates.time).toBe('2023');
    expect(firstTuple.value).toBe('85');
    expect(firstTuple.sourceRow).toBe(4);
    expect(firstTuple.sourceCol).toBe(1);

    // Verify the second tuple (%, weiblich, 2023, 70)
    const secondTuple = tuples[1];
    expect(secondTuple.coordinates.indicator_names).toBe('%');
    expect(secondTuple.coordinates.Gender).toBe('weiblich');
    expect(secondTuple.coordinates.time).toBe('2023');
    expect(secondTuple.value).toBe('70');
    expect(secondTuple.sourceRow).toBe(4);
    expect(secondTuple.sourceCol).toBe(2);

    // Verify the fifth tuple (Anzahl, männlich, 2023, 38)
    const fifthTuple = tuples[4];
    expect(fifthTuple.coordinates.indicator_names).toBe('Anzahl');
    expect(fifthTuple.coordinates.Gender).toBe('männlich');
    expect(fifthTuple.coordinates.time).toBe('2023');
    expect(fifthTuple.value).toBe('38');
    expect(fifthTuple.sourceRow).toBe(4);
    expect(fifthTuple.sourceCol).toBe(5);

    // Verify the last tuple (%, männlich, 2024, 23)
    const lastTuple = tuples[7];
    expect(lastTuple.coordinates.indicator_names).toBe('%');
    expect(lastTuple.coordinates.Gender).toBe('männlich');
    expect(lastTuple.coordinates.time).toBe('2024');
    expect(lastTuple.value).toBe('23');
    expect(lastTuple.sourceRow).toBe(4);
    expect(lastTuple.sourceCol).toBe(8);
  });

  it('should handle mixed row and column mappings', () => {
    // Test with indicator names mapped to a column instead of a row
    const mixedMappings: DimensionMapping[] = [
      {
        id: 'gender-mapping',
        dimensionType: 'additional_dimension',
        customDimensionName: 'Gender',
        mappingDirection: 'row',
        selection: {
          startRow: 1,
          endRow: 1,
          startCol: 1,
          endCol: 8,
          selectedCells: [
            { row: 1, col: 1, value: 'weiblich' },
            { row: 1, col: 2, value: 'männlich' }
          ],
          selectionId: 'gender-selection'
        },
        color: '#2196f3',
        uniqueValues: ['weiblich', 'männlich']
      },
      {
        id: 'indicator-names-mapping',
        dimensionType: 'indicator_names',
        mappingDirection: 'column', // Changed to column mapping
        selection: {
          startRow: 1,
          endRow: 4,
          startCol: 0,
          endCol: 0,
          selectedCells: [
            { row: 1, col: 0, value: 'Indicator A' },
            { row: 2, col: 0, value: 'Indicator B' },
            { row: 3, col: 0, value: 'Indicator C' },
            { row: 4, col: 0, value: 'Indicator D' }
          ],
          selectionId: 'indicator-names-selection'
        },
        color: '#ff9800',
        uniqueValues: ['Indicator A', 'Indicator B', 'Indicator C', 'Indicator D']
      },
      {
        id: 'values-mapping',
        dimensionType: 'indicator_values',
        mappingDirection: 'row',
        selection: {
          startRow: 4,
          endRow: 4,
          startCol: 1,
          endCol: 2,
          selectedCells: [
            { row: 4, col: 1, value: '100' },
            { row: 4, col: 2, value: '200' }
          ],
          selectionId: 'values-selection'
        },
        color: '#e91e63',
        uniqueValues: ['100', '200']
      }
    ];

    const mixedCsvData = [
      ['', 'weiblich', 'männlich'],
      ['Indicator A', '', ''],
      ['Indicator B', '', ''],
      ['Indicator C', '', ''],
      ['Indicator D', '100', '200']
    ];

    const tuples = generateDataTuples(mixedMappings, mixedCsvData);

    expect(tuples).toHaveLength(2);

    // First tuple should have indicator name from column 0, row 4
    const firstTuple = tuples[0];
    expect(firstTuple.coordinates.indicator_names).toBe('Indicator D'); // closest row in mapping
    expect(firstTuple.coordinates.Gender).toBe(''); // closest col in mapping, but mapping does not cover this cell
    expect(firstTuple.value).toBe('100');

    // Second tuple should have indicator name from column 0, row 4
    const secondTuple = tuples[1];
    expect(secondTuple.coordinates.indicator_names).toBe('Indicator D');
    expect(secondTuple.coordinates.Gender).toBe(''); // closest col in mapping, but mapping does not cover this cell
    expect(secondTuple.value).toBe('200');
  });

  it('should handle partial row selections correctly', () => {
    // Test where user selects only part of a row (e.g., columns 2-4 of row 1)
    const partialRowMappings: DimensionMapping[] = [
      {
        id: 'gender-mapping',
        dimensionType: 'additional_dimension',
        customDimensionName: 'Gender',
        mappingDirection: 'row',
        selection: {
          startRow: 1,
          endRow: 1,
          startCol: 2, // Only columns 2-4 selected
          endCol: 4,
          selectedCells: [
            { row: 1, col: 2, value: 'weiblich' },
            { row: 1, col: 3, value: 'weiblich' },
            { row: 1, col: 4, value: 'männlich' }
          ],
          selectionId: 'gender-selection'
        },
        color: '#2196f3',
        uniqueValues: ['weiblich', 'männlich']
      },
      {
        id: 'values-mapping',
        dimensionType: 'indicator_values',
        mappingDirection: 'row',
        selection: {
          startRow: 2,
          endRow: 2,
          startCol: 1, // Values in columns 1-5
          endCol: 5,
          selectedCells: [
            { row: 2, col: 1, value: '100' },
            { row: 2, col: 2, value: '200' },
            { row: 2, col: 3, value: '300' },
            { row: 2, col: 4, value: '400' },
            { row: 2, col: 5, value: '500' }
          ],
          selectionId: 'values-selection'
        },
        color: '#e91e63',
        uniqueValues: ['100', '200', '300', '400', '500']
      }
    ];

    const partialRowCsvData = [
      ['', 'Value1', 'Value2', 'Value3', 'Value4', 'Value5'],
      ['', '', 'weiblich', 'weiblich', 'männlich', ''], // Gender only in cols 2-4
      ['', '100', '200', '300', '400', '500'] // Values in cols 1-5
    ];

    const tuples = generateDataTuples(partialRowMappings, partialRowCsvData);

    expect(tuples).toHaveLength(5);

    // Value in column 1 should get gender from closest selected column (col 2)
    expect(tuples[0].coordinates.Gender).toBe('weiblich');
    expect(tuples[0].value).toBe('100');

    // Value in column 2 should get gender from same column
    expect(tuples[1].coordinates.Gender).toBe('weiblich');
    expect(tuples[1].value).toBe('200');

    // Value in column 3 should get gender from same column
    expect(tuples[2].coordinates.Gender).toBe('weiblich');
    expect(tuples[2].value).toBe('300');

    // Value in column 4 should get gender from same column
    expect(tuples[3].coordinates.Gender).toBe('männlich');
    expect(tuples[3].value).toBe('400');

    // Value in column 5 should get gender from closest selected column (col 4)
    expect(tuples[4].coordinates.Gender).toBe('männlich');
    expect(tuples[4].value).toBe('500');
  });

  it('should handle partial column selections correctly', () => {
    // Test where user selects only part of a column (e.g., rows 2-4 of column 1)
    const partialColMappings: DimensionMapping[] = [
      {
        id: 'indicator-mapping',
        dimensionType: 'indicator_names',
        mappingDirection: 'column',
        selection: {
          startRow: 2, // Only rows 2-4 selected
          endRow: 4,
          startCol: 1,
          endCol: 1,
          selectedCells: [
            { row: 2, col: 1, value: 'Indicator A' },
            { row: 3, col: 1, value: 'Indicator B' },
            { row: 4, col: 1, value: 'Indicator C' }
          ],
          selectionId: 'indicator-selection'
        },
        color: '#ff9800',
        uniqueValues: ['Indicator A', 'Indicator B', 'Indicator C']
      },
      {
        id: 'values-mapping',
        dimensionType: 'indicator_values',
        mappingDirection: 'row',
        selection: {
          startRow: 5,
          endRow: 5,
          startCol: 1, // Values in row 5, columns 1-3
          endCol: 3,
          selectedCells: [
            { row: 5, col: 1, value: '100' },
            { row: 5, col: 2, value: '200' },
            { row: 5, col: 3, value: '300' }
          ],
          selectionId: 'values-selection'
        },
        color: '#e91e63',
        uniqueValues: ['100', '200', '300']
      }
    ];

    const partialColCsvData = [
      ['', 'Header'],
      ['', 'Indicator A'], // Row 1: outside selection
      ['', 'Indicator B'], // Row 2: within selection
      ['', 'Indicator C'], // Row 3: within selection
      ['', 'Indicator D'], // Row 4: within selection
      ['', '100', '200', '300'] // Row 5: values
    ];

    const tuples = generateDataTuples(partialColMappings, partialColCsvData);

    expect(tuples).toHaveLength(3);

    // Value in column 1 should get indicator from closest selected row (row 4)
    expect(tuples[0].coordinates.indicator_names).toBe('Indicator D');
    expect(tuples[0].value).toBe('100');
    // Value in column 2 should get indicator from closest selected row (row 4)
    expect(tuples[1].coordinates.indicator_names).toBe('Indicator D');
    expect(tuples[1].value).toBe('200');

    // Value in column 3 should get indicator from closest selected row (row 4)
    expect(tuples[2].coordinates.indicator_names).toBe('Indicator D');
    expect(tuples[2].value).toBe('300');
  });

  it('should handle user example with partial selections (excluding headers)', () => {
    // Example: User selects only the dimension values, excluding headers or labels
    const userExamplePartialMappings: DimensionMapping[] = [
      {
        id: 'gender-mapping',
        dimensionType: 'additional_dimension',
        customDimensionName: 'Gender',
        mappingDirection: 'row',
        selection: {
          startRow: 1,
          endRow: 1,
          startCol: 1, // Only the gender values, excluding any header
          endCol: 8,
          selectedCells: [
            { row: 1, col: 1, value: 'weiblich' },
            { row: 1, col: 2, value: 'weiblich' },
            { row: 1, col: 3, value: 'weiblich' },
            { row: 1, col: 4, value: 'weiblich' },
            { row: 1, col: 5, value: 'männlich' },
            { row: 1, col: 6, value: 'männlich' },
            { row: 1, col: 7, value: 'männlich' },
            { row: 1, col: 8, value: 'männlich' }
          ],
          selectionId: 'gender-selection'
        },
        color: '#2196f3',
        uniqueValues: ['weiblich', 'männlich']
      },
      {
        id: 'year-mapping',
        dimensionType: 'time',
        subType: 'year',
        mappingDirection: 'row',
        selection: {
          startRow: 2,
          endRow: 2,
          startCol: 1, // Only the year values, excluding any header
          endCol: 8,
          selectedCells: [
            { row: 2, col: 1, value: '2023' },
            { row: 2, col: 2, value: '2023' },
            { row: 2, col: 3, value: '2024' },
            { row: 2, col: 4, value: '2024' },
            { row: 2, col: 5, value: '2023' },
            { row: 2, col: 6, value: '2023' },
            { row: 2, col: 7, value: '2024' },
            { row: 2, col: 8, value: '2024' }
          ],
          selectionId: 'year-selection'
        },
        color: '#4caf50',
        uniqueValues: ['2023', '2024']
      },
      {
        id: 'indicator-names-mapping',
        dimensionType: 'indicator_names',
        mappingDirection: 'row',
        selection: {
          startRow: 3,
          endRow: 3,
          startCol: 1, // Only the indicator names, excluding any header
          endCol: 8,
          selectedCells: [
            { row: 3, col: 1, value: 'Anzahl' },
            { row: 3, col: 2, value: '%' },
            { row: 3, col: 3, value: 'Anzahl' },
            { row: 3, col: 4, value: '%' },
            { row: 3, col: 5, value: 'Anzahl' },
            { row: 3, col: 6, value: '%' },
            { row: 3, col: 7, value: 'Anzahl' },
            { row: 3, col: 8, value: '%' }
          ],
          selectionId: 'indicator-names-selection'
        },
        color: '#ff9800',
        uniqueValues: ['Anzahl', '%']
      },
      {
        id: 'values-mapping',
        dimensionType: 'indicator_values',
        mappingDirection: 'row',
        selection: {
          startRow: 4,
          endRow: 4,
          startCol: 1, // Only the values, excluding any header
          endCol: 8,
          selectedCells: [
            { row: 4, col: 1, value: '85' },
            { row: 4, col: 2, value: '70' },
            { row: 4, col: 3, value: '63' },
            { row: 4, col: 4, value: '77' },
            { row: 4, col: 5, value: '38' },
            { row: 4, col: 6, value: '31' },
            { row: 4, col: 7, value: '19' },
            { row: 4, col: 8, value: '23' }
          ],
          selectionId: 'values-selection'
        },
        color: '#e91e63',
        uniqueValues: ['85', '70', '63', '77', '38', '31', '19', '23']
      }
    ];

    // CSV with headers that are NOT selected
    const userExampleCsvData = [
      ['', 'Gender', 'Gender', 'Gender', 'Gender', 'Gender', 'Gender', 'Gender', 'Gender'], // Row 0: Headers (not selected)
      ['', 'weiblich', 'weiblich', 'weiblich', 'weiblich', 'männlich', 'männlich', 'männlich', 'männlich'], // Row 1: Gender values (selected)
      ['', '2023', '2023', '2024', '2024', '2023', '2023', '2024', '2024'], // Row 2: Year values (selected)
      ['', 'Anzahl', '%', 'Anzahl', '%', 'Anzahl', '%', 'Anzahl', '%'], // Row 3: Indicator names (selected)
      ['', '85', '70', '63', '77', '38', '31', '19', '23'] // Row 4: Values (selected)
    ];

    const tuples = generateDataTuples(userExamplePartialMappings, userExampleCsvData);

    // Should still generate 8 tuples (one for each value)
    expect(tuples).toHaveLength(8);

    // Verify the first tuple (Anzahl, weiblich, 2023, 85)
    const firstTuple = tuples[0];
    expect(firstTuple.coordinates.indicator_names).toBe('Anzahl');
    expect(firstTuple.coordinates.Gender).toBe('weiblich');
    expect(firstTuple.coordinates.time).toBe('2023');
    expect(firstTuple.value).toBe('85');

    // Verify the last tuple (%, männlich, 2024, 23)
    const lastTuple = tuples[7];
    expect(lastTuple.coordinates.indicator_names).toBe('%');
    expect(lastTuple.coordinates.Gender).toBe('männlich');
    expect(lastTuple.coordinates.time).toBe('2024');
    expect(lastTuple.value).toBe('23');
  });
}); 