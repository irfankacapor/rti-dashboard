import { DataType } from '@/types/dataType';

const API_BASE = process.env.NEXT_PUBLIC_API_URL;

export const dataTypeService = {
  // Get all data types
  getDataTypes: async (): Promise<DataType[]> => {
    const response = await fetch(`${API_BASE}/data-types`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data types: ${response.statusText}`);
    }
    return response.json();
  },

  // Get data type by name
  getDataTypeByName: async (name: string): Promise<DataType | null> => {
    const dataTypes = await dataTypeService.getDataTypes();
    return dataTypes.find(dt => dt.code === name) || null;
  },

  // Get data type ID by name
  getDataTypeIdByName: async (name: string): Promise<number | null> => {
    const dataType = await dataTypeService.getDataTypeByName(name);
    return dataType?.id ? parseInt(dataType.id, 10) : null;
  },

  // Cache for data types to avoid repeated API calls
  _cache: new Map<string, DataType[]>(),

  // Get cached data types
  getCachedDataTypes: async (): Promise<DataType[]> => {
    if (!dataTypeService._cache.has('dataTypes')) {
      const dataTypes = await dataTypeService.getDataTypes();
      dataTypeService._cache.set('dataTypes', dataTypes);
    }
    return dataTypeService._cache.get('dataTypes') || [];
  },

  // Get cached data type ID by name
  getCachedDataTypeIdByName: async (name: string): Promise<number | null> => {
    const dataTypes = await dataTypeService.getCachedDataTypes();
    const dataType = dataTypes.find(dt => dt.code === name);
    return dataType?.id ? parseInt(dataType.id, 10) : null;
  },

  // Clear cache
  clearCache: () => {
    dataTypeService._cache.clear();
  }
}; 