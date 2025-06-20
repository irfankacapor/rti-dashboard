import { slugify } from './slugify';

describe('slugify', () => {
  it('slugifies a basic string', () => {
    expect(slugify('Hello World')).toBe('hello-world');
  });
  it('trims and lowercases', () => {
    expect(slugify('  Foo Bar  ')).toBe('foo-bar');
  });
  it('removes special characters', () => {
    expect(slugify('A@B#C!')).toBe('a-b-c');
  });
  it('replaces spaces with dashes', () => {
    expect(slugify('foo bar baz')).toBe('foo-bar-baz');
  });
  it('handles empty input', () => {
    expect(slugify('')).toBe('');
  });
}); 