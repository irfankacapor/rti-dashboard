export function slugify(text: string): string {
  return text
    .toString()
    .normalize('NFKD')
    .replace(/[\u0300-\u036F]/g, '') // Remove accents
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-') // Replace non-alphanumeric with hyphens
    .replace(/^-+|-+$/g, ''); // Remove leading/trailing hyphens
} 