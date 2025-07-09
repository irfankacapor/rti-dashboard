export interface IndicatorValuesResponse {
  rows: IndicatorValueRow[];
  dimensionColumns: string[];
  indicatorName: string;
  dataType: string;
}

export interface IndicatorValueRow {
  factId: string;
  dimensions: Record<string, string>;
  value?: number;
  isEmpty: boolean;
}

export interface IndicatorValueUpdate {
  factId: string;
  newValue: number;
}

export interface IndicatorValueEdit {
  factId: string;
  originalValue?: number;
  newValue: number;
  isNew: boolean;
}

export interface NewIndicatorValueRow {
  tempId: string; // Temporary ID for new rows
  dimensions: Record<string, string>;
  value: number;
  isNewRow: true;
}

export interface IndicatorValueCreate {
  dimensions: Record<string, string>;
  value: number;
} 