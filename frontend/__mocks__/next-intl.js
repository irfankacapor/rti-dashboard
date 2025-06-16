// __mocks__/next-intl.js
const mockMessages = {
  Index: {
    title: 'Welcome to RTI Dashboard',
    description: 'A modern dashboard built with Next.js'
  }
};

const useTranslations = (namespace) => {
  return (key, values) => {
    // Try to find the actual message first
    let message;
    if (namespace && mockMessages[namespace] && mockMessages[namespace][key]) {
      message = mockMessages[namespace][key];
    } else if (mockMessages[key]) {
      message = mockMessages[key];
    } else {
      // Fallback to key if message not found
      message = namespace ? `${namespace}.${key}` : key;
    }
    
    // Handle interpolation if values are provided
    if (values && typeof message === 'string') {
      let result = message;
      Object.keys(values).forEach(valueKey => {
        result = result.replace(`{${valueKey}}`, values[valueKey]);
      });
      return result;
    }
    
    return message;
  };
};

const useFormatter = () => ({
  dateTime: (date, options) => {
    if (date instanceof Date) {
      return date.toISOString();
    }
    return String(date);
  },
  number: (number, options) => {
    return String(number);
  },
  relativeTime: (value, unit) => {
    return `${value} ${unit} ago`;
  },
});

const useLocale = () => 'en';

const useMessages = () => ({});

const useNow = () => new Date();

const useTimeZone = () => 'UTC';

const NextIntlClientProvider = ({ children }) => children;

module.exports = {
  useTranslations,
  useFormatter,
  useLocale,
  useMessages,
  useNow,
  useTimeZone,
  NextIntlClientProvider,
  default: {
    useTranslations,
    useFormatter,
    useLocale,
    useMessages,
    useNow,
    useTimeZone,
    NextIntlClientProvider,
  }
};