import {notFound} from 'next/navigation';
import {getRequestConfig} from 'next-intl/server';

// Can be imported from a shared config
export const locales = ['en', 'de'] as const;
export type Locale = (typeof locales)[number];

export default getRequestConfig(async ({requestLocale}) => {
  // `requestLocale` is a Promise and needs to be awaited
  const locale = await requestLocale;
  
  // Handle the case where locale is undefined (e.g., for routes outside [locale] segment)
  if (!locale) {
    // Return a default locale for undefined cases
    return {
      locale: 'en',
      messages: (await import(`../messages/en.json`)).default,
    };
  }
  
  // Ensure that a valid locale is used
  if (!locales.includes(locale as Locale)) {
    notFound();
  }

  return {
    locale,
    messages: (await import(`../messages/${locale}.json`)).default,
  };
});