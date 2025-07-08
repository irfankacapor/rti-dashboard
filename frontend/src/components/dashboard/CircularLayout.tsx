import React, { useState } from 'react';
import { Box, Typography } from '@mui/material';
import { CircularLayoutProps } from '@/types/dashboard';
import { PASTEL_COLORS } from '@/constants';
import { DashboardGoal } from '@/types/dashboard';

const AREA_RADIUS = 120;
const SUBAREA_RADIUS = 80; // at least twice as big
const SVG_WIDTH = 1200;
const SVG_HEIGHT = 800;
const BACKGROUND_COLOR = '#f5f6fa';
const AREA_FILL = '#fff';
const AREA_STROKE = '#222';
const AREA_STROKE_WIDTH = 2;
const SUBAREA_FILL = '#fff';
const SUBAREA_STROKE_WIDTH = 8;

// Color scale legend constants
const LEGEND_MIN = 75;
const LEGEND_MAX = 110;
const LEGEND_WIDTH = 300;
const LEGEND_X = SVG_WIDTH / 2 - LEGEND_WIDTH / 2;
const LEGEND_Y = SVG_HEIGHT - 60;
const LEGEND_HEIGHT = 10;
const LEGEND_TICKS = [75, 90, 100, 110];

function getProgressColor(progress: number) {
  if (progress < 75) return '#e74c3c'; // red
  if (progress < 90) return '#f39c12'; // orange
  if (progress < 100) return '#f7e017'; // yellow
  return '#27ae60'; // green
}

interface Relationships {
  goalToSubareas: { [goalId: string]: string[] };
  subareaToGoals: { [subareaId: string]: string[] };
}

interface CircularLayoutExtendedProps extends CircularLayoutProps {
  goals: DashboardGoal[];
  relationships: Relationships;
}

export const CircularLayout: React.FC<CircularLayoutExtendedProps> = ({
  areas,
  subareas,
  isEditMode,
  highlightedSubareas,
  onSubareaClick,
  onSubareaHover,
  onSubareaLeave,
  goals,
  relationships
}) => {
  // Edit mode: track selected subarea for swapping
  const [swapSelection, setSwapSelection] = useState<string | null>(null);
  const [customOrder, setCustomOrder] = useState<Record<string, string[]>>({}); // areaId -> subareaId[]

  // Place areas in a circle (if >1), otherwise center
  const areaCenters = areas.map((area, i) => {
    const n = areas.length;
    if (n === 1) {
      return { ...area, x: SVG_WIDTH / 2, y: SVG_HEIGHT / 2 };
    }
    const angle = (2 * Math.PI * i) / n;
    const r = 300;
    return {
      ...area,
      x: SVG_WIDTH / 2 + r * Math.cos(angle),
      y: SVG_HEIGHT / 2 + r * Math.sin(angle)
    };
  });

  // Map areaId to center
  const areaIdToCenter = Object.fromEntries(areaCenters.map(a => [a.id, { x: a.x, y: a.y }]));

  // For each area, get subareas in custom order (if set)
  const getOrderedSubareas = (areaId: string) => {
    const all = subareas.filter(s => s.areaId === areaId);
    const order = customOrder[areaId];
    if (!order) return all;
    // Return subareas in custom order, fallback to all
    return order.map(id => all.find(s => s.id === id)).filter(Boolean) as typeof subareas;
  };

  // For each area, place its subareas evenly on the circumference
  const subareaNodes = areaCenters.flatMap(area => {
    const siblings = getOrderedSubareas(area.id);
    const n = siblings.length;
    return siblings.map((sub, i) => {
      const angle = (2 * Math.PI * i) / n - Math.PI / 2; // start at top
      return {
        ...sub,
        x: area.x + AREA_RADIUS * Math.cos(angle),
        y: area.y + AREA_RADIUS * Math.sin(angle),
        angle,
        areaCenter: area,
        index: i,
        total: n
      };
    });
  });

  // Helper: get all goals linked to a subarea
  const getGoalsForSubarea = (subareaId: string) => {
    const goalIds = relationships?.subareaToGoals?.[subareaId] || [];
    return goals.filter(goal => goalIds.includes(goal.id));
  };

  // Edit mode: handle subarea click for swapping
  const handleSubareaEditClick = (subId: string, areaId: string) => {
    if (!isEditMode) return;
    if (!swapSelection) {
      setSwapSelection(subId);
    } else if (swapSelection === subId) {
      setSwapSelection(null);
    } else {
      // Swap subareas in customOrder
      const areaSubs = getOrderedSubareas(areaId).map(s => s.id);
      const idx1 = areaSubs.indexOf(swapSelection);
      const idx2 = areaSubs.indexOf(subId);
      if (idx1 !== -1 && idx2 !== -1) {
        const newOrder = [...areaSubs];
        [newOrder[idx1], newOrder[idx2]] = [newOrder[idx2], newOrder[idx1]];
        setCustomOrder(prev => ({ ...prev, [areaId]: newOrder }));
      }
      setSwapSelection(null);
    }
  };

  // Determine if any subarea has a goal with a targetValue > 0
  const anySubareaHasTarget = subareaNodes.some(sub => {
    const linkedGoals = getGoalsForSubarea(sub.id);
    return linkedGoals.some(goal => goal.targetValue > 0);
  });

  return (
    <Box sx={{ width: '100%', height: '100%', overflow: 'auto', background: BACKGROUND_COLOR }}>
      <svg
        width={SVG_WIDTH}
        height={SVG_HEIGHT}
        style={{ display: 'block', margin: '0 auto', background: BACKGROUND_COLOR }}
      >
        {/* Draw area circles */}
        {areaCenters.map(area => {
          const areaSubareas = getOrderedSubareas(area.id);
          const showNameOutside = areaSubareas.length > 2;
          return (
            <g key={area.id}>
              <circle
                cx={area.x}
                cy={area.y}
                r={AREA_RADIUS}
                fill={AREA_FILL}
                stroke={AREA_STROKE}
                strokeWidth={AREA_STROKE_WIDTH}
                opacity={1}
              />
              {/* Area name: inside or outside */}
              {showNameOutside ? (
                <text
                  x={area.x + AREA_RADIUS + 80}
                  y={area.y}
                  textAnchor="start"
                  alignmentBaseline="middle"
                  fontSize="28px"
                  fontWeight="bold"
                  fill="#111"
                  style={{ pointerEvents: 'none' }}
                >
                  {area.name}
                </text>
              ) : (
                <text
                  x={area.x}
                  y={area.y}
                  textAnchor="middle"
                  dy="0.35em"
                  fontSize="22px"
                  fontWeight="bold"
                  fill="#111"
                >
                  {area.name}
                </text>
              )}
            </g>
          );
        })}

        {/* Draw subareas on circumference */}
        {subareaNodes.map((sub, subIdx) => {
          const isHighlighted = highlightedSubareas.includes(sub.id);
          const isSelected = isEditMode && swapSelection === sub.id;
          const linkedGoals = getGoalsForSubarea(sub.id);
          const hasAnyTarget = linkedGoals.some(goal => goal.targetValue > 0);
          let borderColor: string;
          if (hasAnyTarget) {
            const progress = typeof sub.performance?.score === 'number' ? sub.performance.score : 100;
            borderColor = getProgressColor(progress);
          } else {
            // Use pastel color, cycle by subarea index for variety
            borderColor = PASTEL_COLORS[subIdx % PASTEL_COLORS.length];
          }
          return (
            <g
              key={sub.id}
              style={{ cursor: isEditMode ? 'pointer' : 'default' }}
              onClick={() => isEditMode ? handleSubareaEditClick(sub.id, sub.areaId) : onSubareaClick(sub.id)}
              onMouseEnter={() => onSubareaHover(sub.id)}
              onMouseLeave={onSubareaLeave}
            >
              <circle
                cx={sub.x}
                cy={sub.y}
                r={SUBAREA_RADIUS}
                fill={SUBAREA_FILL}
                stroke={isHighlighted ? '#1976d2' : borderColor}
                strokeWidth={isHighlighted ? SUBAREA_STROKE_WIDTH + 4 : SUBAREA_STROKE_WIDTH}
                opacity={1}
                style={{ 
                  filter: isSelected 
                    ? 'drop-shadow(0 0 8px #1976d2)' 
                    : isHighlighted 
                      ? 'drop-shadow(0 0 12px rgba(25, 118, 210, 0.6))' 
                      : undefined,
                  transition: 'all 0.2s ease'
                }}
              />
              <text
                x={sub.x}
                y={sub.y}
                textAnchor="middle"
                dy="0.35em"
                fontSize="22px"
                fontWeight="bold"
                fill={isHighlighted ? "#1976d2" : "#222"}
                style={{ 
                  filter: isHighlighted ? 'drop-shadow(0 0 4px rgba(25, 118, 210, 0.4))' : undefined,
                  transition: 'all 0.2s ease'
                }}
              >
                {sub.name}
              </text>
            </g>
          );
        })}

        {/* Color scale legend: only show if any subarea has a goal with a target */}
        {anySubareaHasTarget && (
          <g>
            <defs>
              <linearGradient id="progress-gradient" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stopColor="#e74c3c" />
                <stop offset="35%" stopColor="#f39c12" />
                <stop offset="60%" stopColor="#f7e017" />
                <stop offset="100%" stopColor="#27ae60" />
              </linearGradient>
            </defs>
            <rect x={LEGEND_X} y={LEGEND_Y} width={LEGEND_WIDTH} height={LEGEND_HEIGHT} fill="url(#progress-gradient)" rx={3} />
            {LEGEND_TICKS.map((val) => {
              // Proportional position
              const pos = LEGEND_X + ((val - LEGEND_MIN) / (LEGEND_MAX - LEGEND_MIN)) * LEGEND_WIDTH;
              return (
                <g key={val}>
                  <line x1={pos} y1={LEGEND_Y} x2={pos} y2={LEGEND_Y + 15} stroke="#222" strokeWidth={2} />
                  <text x={pos} y={LEGEND_Y + 32} textAnchor="middle" fontSize="16px" fill="#222">{val}</text>
                </g>
              );
            })}
          </g>
        )}
      </svg>
      {isEditMode && (
        <Box sx={{ position: 'absolute', top: 24, right: 32, bgcolor: '#fff', p: 2, borderRadius: 2, boxShadow: 2, zIndex: 10 }}>
          <Typography variant="body2" fontWeight="bold">Edit Mode: Click two subareas to swap their positions</Typography>
          {swapSelection && <Typography variant="body2" color="primary">Selected: {subareaNodes.find(s => s.id === swapSelection)?.name}</Typography>}
        </Box>
      )}
    </Box>
  );
}; 