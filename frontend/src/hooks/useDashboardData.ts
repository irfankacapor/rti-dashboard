import { useState, useEffect } from 'react';
import { DashboardArea, DashboardSubarea } from '@/types/dashboard';
import { apiClient } from '@/lib/api';
import { Subarea } from '@/types/subareas';

export const useDashboardData = () => {
  const [areas, setAreas] = useState<DashboardArea[]>([]);
  const [subareas, setSubareas] = useState<DashboardSubarea[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setIsLoading(true);
        setError(null);

        // Fetch areas
        const areasData = await apiClient.get('/areas');

        // Fetch subareas
        const subareasData = await apiClient.get('/subareas');

        // Transform areas data
        const transformedAreas: DashboardArea[] = areasData.map((area: any) => ({
          id: area.id.toString(),
          name: area.name,
          description: area.description,
          code: area.code,
          position: { x: 0, y: 0 }, // Will be calculated by layout engine
          performance: {
            score: 0, // Will be fetched from performance service
            colorCode: '#cccccc'
          },
          subareaCount: area.subareaCount || 0
        }));

        // Transform subareas data
        const transformedSubareas: DashboardSubarea[] = subareasData.map((subarea: Subarea) => ({
          id: subarea.id.toString(),
          name: subarea.name,
          description: subarea.description,
          code: subarea.code,
          areaId: subarea.areaId?.toString() || '',
          areaName: subarea.areaName || '',
          position: { x: 0, y: 0 }, // Will be calculated by layout engine
          performance: {
            score: 0, // Will be fetched from performance service
            colorCode: '#cccccc'
          },
          indicatorCount: subarea.indicatorCount || 0,
          goalCount: 0 // Will be calculated from goal relationships
        }));

        setAreas(transformedAreas);
        setSubareas(transformedSubareas);

        // Fetch performance data for each area
        for (const area of transformedAreas) {
          try {
            const performanceData = await apiClient.get(`/dashboard-data/performance-metrics/${area.id}`);
            setAreas(prev => prev.map(a => 
              a.id === area.id 
                ? { ...a, performance: { score: performanceData.averageScore, colorCode: performanceData.colorCode || '#cccccc' } }
                : a
            ));
          } catch (err) {
            console.warn(`Failed to fetch performance for area ${area.id}:`, err);
          }
        }

      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch dashboard data');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  return { areas, subareas, isLoading, error };
}; 