import {NextIntlClientProvider} from 'next-intl';
import {getMessages} from 'next-intl/server';
import { Providers } from '../providers';
import { AuthProvider } from '@/hooks/useAuth';
import Navbar from '@/components/common/Navbar';

export default async function LocaleLayout(props: {
  children: React.ReactNode;
  params: { locale: string } | Promise<{ locale: string }>;
}) {
  const { children, params } = props;
  const resolvedParams = await params;
  const messages = await getMessages({ locale: resolvedParams.locale });

  return (
    <NextIntlClientProvider messages={messages} locale={resolvedParams.locale}>
      <AuthProvider>
        <Providers>
          <Navbar />
          {children}
        </Providers>
      </AuthProvider>
    </NextIntlClientProvider>
  );
}