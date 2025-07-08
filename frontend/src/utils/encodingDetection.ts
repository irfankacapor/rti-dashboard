import { DimensionMapping, EncodingIssue, EncodingLocation } from '@/types/csvProcessing';

const ENCODING_FIXES: Record<string, string> = {
  // German umlauts
  '√§': 'ä', '√∂': 'ö', '√º': 'ü', '√°': 'ß',
  // Other European characters  
  '√©': 'é', '√†': 'æ', '√¨': 'è', '√¢': 'â',
  '√≤': 'ò', '√¶': 'ñ', '√´': 'ó', '√π': 'ø',
  // UTF-8 as Latin-1
  'Ã¤': 'ä', 'Ã¶': 'ö', 'Ã¼': 'ü', 'ÃŸ': 'ß',
  'Ã©': 'é', 'Ã¨': 'è', 'Ã¡': 'á', 'Ã³': 'ó'
};

export function detectEncodingIssues(
  csvData: string[][],
  dimensionMappings: DimensionMapping[]
): EncodingIssue[] {
  const issuesMap = new Map<string, EncodingIssue>();
  
  // Process each dimension mapping
  dimensionMappings.forEach(mapping => {
    // Skip indicator values - we only care about text dimensions
    if (mapping.dimensionType === 'indicator_values') return;
    
    // Get all selected cells for this mapping
    mapping.selection.selectedCells.forEach(cell => {
      const cellValue = cell.value;
      if (!cellValue || typeof cellValue !== 'string') return;
      
      // Check for known encoding issues
      Object.entries(ENCODING_FIXES).forEach(([problem, fix]) => {
        if (cellValue.includes(problem)) {
          const issueKey = problem;
          
          if (!issuesMap.has(issueKey)) {
            issuesMap.set(issueKey, {
              problematicText: problem,
              suggestedReplacement: fix,
              issueType: 'KNOWN_ENCODING',
              occurrenceCount: 0,
              locations: []
            });
          }
          
          const issue = issuesMap.get(issueKey)!;
          issue.occurrenceCount++;
          issue.locations.push({
            rowIndex: cell.row,
            colIndex: cell.col,
            dimensionType: mapping.dimensionType,
            originalValue: cellValue,
            previewFixed: cellValue.replaceAll(problem, fix)
          });
        }
      });
      
      // Check for potential issues with regex patterns
      const potentialPatterns = [
        /√[^\s]{1,2}/g,  // √ followed by 1-2 non-space chars
        /Ã[^\s]/g,       // Ã followed by non-space char
        /â€./g           // Smart quotes encoding issues
      ];
      
      potentialPatterns.forEach(pattern => {
        const matches = [...cellValue.matchAll(pattern)];
        matches.forEach(match => {
          const problem = match[0];
          // Skip if we already have a known fix for this
          if (ENCODING_FIXES[problem]) return;
          
          const issueKey = problem;
          if (!issuesMap.has(issueKey)) {
            issuesMap.set(issueKey, {
              problematicText: problem,
              suggestedReplacement: '',
              issueType: 'POTENTIAL_ENCODING',
              occurrenceCount: 0,
              locations: []
            });
          }
          
          const issue = issuesMap.get(issueKey)!;
          issue.occurrenceCount++;
          issue.locations.push({
            rowIndex: cell.row,
            colIndex: cell.col,
            dimensionType: mapping.dimensionType,
            originalValue: cellValue,
            previewFixed: cellValue // User will need to specify replacement
          });
        });
      });
    });
  });
  
  return Array.from(issuesMap.values());
}

export function applyEncodingFixes(
  csvData: string[][],
  userReplacements: Map<string, string>
): string[][] {
  // Deep clone the data
  const fixedData = csvData.map(row => [...row]);
  
  // Apply each replacement across all data
  userReplacements.forEach((replacement, problematicText) => {
    if (!replacement.trim()) return; // Skip empty replacements
    
    for (let row = 0; row < fixedData.length; row++) {
      for (let col = 0; col < fixedData[row].length; col++) {
        if (fixedData[row][col] && typeof fixedData[row][col] === 'string') {
          fixedData[row][col] = fixedData[row][col].replaceAll(problematicText, replacement);
        }
      }
    }
  });
  
  return fixedData;
}

export type { EncodingLocation, EncodingIssue }; 